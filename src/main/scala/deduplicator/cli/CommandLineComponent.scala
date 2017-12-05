package deduplicator.cli

import scopt.OptionParser

// https://github.com/scopt/scopt

trait CommandLineComponent {

  val commandLineService: CommandLineService

  case class CommandLineConfig(paths: Seq[String] = Seq(), recursive: Boolean = false)

  class CommandLineService {

    def parse(args: Seq[String]): Option[CommandLineConfig] = parser.parse(args, CommandLineConfig())

    private lazy val parser = new OptionParser[CommandLineConfig]("deduplicator") {
      head("deduplicator", "0.x")

      opt[Unit]('r', "recursive").action((_, c) => c.copy(recursive = true)).text("Recurse through all children folders")

      arg[String]("<file>...").optional().withFallback(() => ".").unbounded().action((x, c) =>
        c.copy(paths = c.paths :+ x)).text("Optional file or directory path(s)")

      note("Will use the current directory if no paths are specified.")
      //  help("help").text("prints this usage text")
      // checkConfig( c => if (c.recursive && c.paths.forall( p => ... ) failure("...") else success )
    }
  }

}


