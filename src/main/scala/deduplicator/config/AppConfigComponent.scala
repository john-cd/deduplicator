package deduplicator.config

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

trait AppConfigComponent {

  val appConfigService: AppConfigService

  /** Load config settings from application.conf (checking first against reference.conf)
    * https://lightbend.github.io/config/latest/api/com/typesafe/config/Config.html	
    */
  class AppConfigService extends LazyLogging {
    //-Dconfig.resource=production.conf for overriding

    private val conf = ConfigFactory.load()

    // This verifies that the Config is sane and has our
    // reference config. Importantly, we specify the "deduplicator"
    // path so we only validate settings that belong to this
    // app. Otherwise, we might throw mistaken errors about
    // settings we know nothing about.
    conf.checkValid(ConfigFactory.defaultReference(), "deduplicator")

    private val appConf = conf.getConfig("deduplicator") // will throw if not present  / wrong type
    val workers = appConf.getInt("workers") // etc...
    private val db = appConf.getConfig("db")
    val dbConnectionString = db.getString("connection-string")
    val dbUsername = db.getString("username")
    val dbPassword = db.getString("password")
  }

}


/* to make a config optional:
 
implicit class RichConfig(val underlying: Config) extends AnyVal {
  def getOptionalBoolean(path: String): Option[Boolean] = if (underlying.hasPath(path)) {
     Some(underlying.getBoolean(path))
  } else {
     None
  }
}
*/