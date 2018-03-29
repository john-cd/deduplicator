package deduplicator

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

package object utils {

  // loan pattern
  def use[A <: {def close() : Unit}, B](resource: A)(code: A ⇒ B): B =
    try
      code(resource)
    finally
      if (resource != null)
        resource.close()




  implicit class FutureCompanionOps(val f: Future.type) extends AnyVal {

    def allFailedAsTrys[T](fItems: /* future items */ List[Future[T]]): Future[List[Try[T]]] = {
      allAsTrys(fItems).map(_.filter(_.isFailure))
    }

    /** Given a list of futures `fs`, returns the future holding the list of Try's of the futures from `fs`.
      * The returned future is completed only once all of the futures in `fs` have been completed.
      */
    def allAsTrys[T](fItems: /* future items */ List[Future[T]]): Future[List[Try[T]]] = {
      val listOfFutureTrys: List[Future[Try[T]]] = fItems.map(futureToFutureTry)
      Future.sequence(listOfFutureTrys)
    }

    def futureToFutureTry[T](f: Future[T]): Future[Try[T]] = {
      f.map(Success(_)).recover { case x => Failure(x) }
    }

    def allSucceededAsTrys[T](fItems: /* future items */ List[Future[T]]): Future[List[Try[T]]] = {
      allAsTrys(fItems).map(_.filter(_.isSuccess))
    }
  }


}
