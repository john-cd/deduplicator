package deduplicator.utils

object Utils {

  // loan pattern
  def use[A <: {def close() : Unit}, B](resource: A)(code: A ⇒ B): B =
    try
      code(resource)
    finally
      if (resource != null)
        resource.close()


  // EXCEPTION HANDLING

  // Examples:
  // https://ted-gao.blogspot.com/2012/05/exception-handling-in-scala.html
  //  http://www.scala-lang.org/api/2.12.0/scala/util/control/Exception%24.html

  // functional exception handling: http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html

  //    def checksum(path: String): Try[String] = {
  //      Try(Paths.get(path).normalize).filter(_.exists && !_.isDirectory).flatmap(file => doSomething(file))
  //    }


  // Custom exception template
  class CustomException(message: String, nestedException: Throwable) extends Exception(message, nestedException) {
    def this() = this("", null)

    def this(message: String) = this(message, null)

    def this(nestedException: Throwable) = this("", nestedException)
  }

}