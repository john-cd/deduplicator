package deduplicator.utils

/**
  * Custom exception template
  *
  * @see [[https://ted-gao.blogspot.com/2012/05/exception-handling-in-scala.html]]
  * @see [[http://www.scala-lang.org/api/2.12.0/scala/util/control/Exception%24.html]]
  * @see functional exception handling: [[http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html]]
  * @param message message
  * @param nestedException nested Exception
  */
class CustomException(message: String, nestedException: Throwable) extends Exception(message, nestedException) {
  def this() = this("", null)

  def this(message: String) = this(message, null)

  def this(nestedException: Throwable) = this("", nestedException)
}
