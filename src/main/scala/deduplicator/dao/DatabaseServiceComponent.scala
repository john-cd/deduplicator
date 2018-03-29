package deduplicator.dao

import deduplicator.config.AppConfigComponent

trait DatabaseService {
  val dbDriver: String
  val connectionString: String
  val username: String
  val password: String
}

trait DatabaseServiceComponent {
  this: AppConfigComponent =>

  val databaseService: DatabaseService

  class H2DatabaseService extends DatabaseService {
    override val dbDriver: String = "org.h2.Driver"
    override val connectionString: String = appConfigService.dbConnectionString
    override val username: String = appConfigService.dbUsername
    override val password: String = appConfigService.dbPassword
  }

}
