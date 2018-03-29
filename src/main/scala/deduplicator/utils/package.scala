package deduplicator

import scala.collection.generic.CanBuildFrom
import scala.concurrent._
import scala.language.reflectiveCalls

//noinspection SpellCheckingInspection
package object utils {

  // loan pattern
  def use[A <: {def close() : Unit}, B](resource: A)(code: A ⇒ B): B =
    try
      code(resource)
    finally
      if (resource != null)
        resource.close()


  // Combinators for Futures

  /**
    * Takes a list of Futures and converts it to a Future of list of results,
    * removing all failed Futures
    *
    * @param in  traversable of Futures
    * @param cbf implicit that allows building M[A]
    * @param ec  implicit execution context for all Futures
    * @tparam A type of the Futures' results
    * @tparam M type of traversable
    * @return a future traversable of results
    */
  //noinspection ScalaUnnecessaryParentheses
  def successSequence[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])(implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]], ec: ExecutionContext): Future[M[A]] = {
    in.foldLeft(Future.successful(cbf(in))) { // starting with a Future of Builder of M[A]
      (fr, fa) =>
        (for (r <- fr; a <- fa) yield (r += a)) recoverWith { // add a future from "in" to the builder
          case _ => fr // if a is failed, ignore it
        }
    } map (_.result()) // convert builder to final collection
  }

  /**
    * Takes a list of Futures and converts it to
    * (1) a Future list of results, for all successful Futures
    * (2) a sequence of exceptions, for all failed Futures
    *
    * @param in  traversable of Futures
    * @param cbf implicit that allows building M[A]
    * @param ec  implicit execution context for all Futures
    * @tparam A type of the Futures' results
    * @tparam M type of traversable
    * @return a future traversable of results and a sequence of exceptions
    */
  def splitSequence[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])(implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]], ec: ExecutionContext): Future[(M[A], Seq[Throwable])] = {
    in.foldLeft(Future.successful(cbf(in), Seq[Throwable]())) {
      case (fr, fa) =>
        (for ((r, ff) <- fr; a <- fa) yield (r += a, ff)) recoverWith {
          case t: Throwable => for ((r, ff) <- fr) yield (r, ff :+ t)
        }
    } map {
      case (ss, ff) => (ss.result(), ff)
    }
  }

  // OR

  //  implicit class FutureCompanionOps(val f: Future.type) extends AnyVal {
  //
  //    def allFailedAsTrys[T](fItems: /* future items */ List[Future[T]]): Future[List[Try[T]]] = {
  //      allAsTrys(fItems).map(_.filter(_.isFailure))
  //    }
  //
  //    /** Given a list of futures `fs`, returns the future holding the list of Try's of the futures from `fs`.
  //      * The returned future is completed only once all of the futures in `fs` have been completed.
  //      */
  //    def allAsTrys[T](fItems: /* future items */ List[Future[T]]): Future[List[Try[T]]] = {
  //      val listOfFutureTrys: List[Future[Try[T]]] = fItems.map(futureToFutureTry)
  //      Future.sequence(listOfFutureTrys)      // could also use: Future.traverse(list)(fut => fut)
  //    }
  //
  //    def futureToFutureTry[T](f: Future[T]): Future[Try[T]] = {
  //      f.map(Success(_)).recover { case x => Failure(x) }  // converts a future result into a future Try
  //    }
  //
  //    def allSucceededAsTrys[T](fItems: /* future items */ List[Future[T]]): Future[List[Try[T]]] = {
  //      allAsTrys(fItems).map(_.filter(_.isSuccess))
  //    }
  //  }

}
