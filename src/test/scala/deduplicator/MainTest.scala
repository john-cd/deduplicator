package deduplicator

/*
trait TestEnvironment extends AppConfigComponent
  with IOServiceComponent
  with DatabaseServiceComponent
  with MigrationComponent
  with DaoServiceComponent
  with MockitoSugar {

  // use the test configuration file.
  override val appConfigService: AppConfigService = spy(new AppConfigService)
  // override the path here to use the test resources.
  when(appConfigService.configPath).thenReturn(this.getClass.getResource("/").getPath)
  
  override val ioService: IOService = mock[IOService]
  override val databaseService: DatabaseService = mock[DatabaseService]
  override val migrationService: MigrationService = mock[MigrationService]
  override val daoService: DaoService = mock[DaoService]
} 
*/