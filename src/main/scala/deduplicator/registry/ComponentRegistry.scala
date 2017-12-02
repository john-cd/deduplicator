package deduplicator.registry

import deduplicator.cli._
import deduplicator.config._
import deduplicator.io.IOServiceComponent
import deduplicator.dao._
import deduplicator.actors._ //{ActorFactory, ActorFactoryComponent}
import deduplicator.hash._


object ComponentRegistry extends AppConfigComponent
  with CommandLineComponent
  with IOServiceComponent
  with HashComponent
  with DatabaseServiceComponent
  with MigrationComponent
  with DaoServiceComponent
  with ActorFactoryComponent
{
  override val commandLineService: CommandLineService = new CommandLineService
  override val appConfigService: ComponentRegistry.AppConfigService = new AppConfigService
  override val ioService: ComponentRegistry.IOService = new IOService
  override val hashService: Hasher = new Md5Hasher 
  override val databaseService: DatabaseService = new H2DatabaseService
  override val migrationService: ComponentRegistry.MigrationService = new MigrationService
  override val daoService: DaoService = new DaoServiceImpl
  override val actorFactory: ActorFactory = new ActorFactoryImpl
}