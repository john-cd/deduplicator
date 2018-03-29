package deduplicator.registry

import deduplicator.cli._
import deduplicator.config._
import deduplicator.dao._
import deduplicator.hash._
import deduplicator.io._

import scala.concurrent.ExecutionContext.Implicits.global

object ComponentRegistry extends AppConfigComponent
  with CommandLineComponent
  with IOServiceComponent
  with HashComponent
  with DatabaseServiceComponent
  with MigrationComponent
  with DaoServiceComponent {
  override val commandLineService = new CommandLineService
  override val appConfigService = new AppConfigService
  // io
  override val fileSystemWalker = new FileSystemWalker
  override val fileAsyncIO = new FileAsyncIO
  //
  override val hashService = new HashService
  override val databaseService = new H2DatabaseService
  override val migrationService = new MigrationService
  override val daoService = new DaoService
}
