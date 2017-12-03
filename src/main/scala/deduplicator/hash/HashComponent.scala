package deduplicator.hash

import java.io.{File, FileInputStream, InputStream}
import java.security.{DigestInputStream, MessageDigest}

import org.apache.commons.codec.binary.Hex


trait HashComponent {

  val hashService: Hasher

  object Hasher {
    private lazy val md = MessageDigest.getInstance("MD5")
    def apply() = new Hasher
  }
  
  class Hasher {
    import Hasher._
	
    def checksum(filepath: String): String = {
	 checksum(new FileInputStream(filepath))
	}
	
	def checksum(stream: InputStream): String = {
      val buffer = new Array[Byte](8192)
      md.reset()
      val dis = new DigestInputStream(stream, md)
      try {
          while (dis.read(buffer) != -1) {}
	  }
      finally {
        dis.close()
      }
	  new String(Hex.encodeHex(md.digest()))  // or: md.digest.map("%02x".format(_)).mkString
	  // note: do not intern the String! 
    }
  }  
}


//trait Hasher {
//  def hash(data: String): String
//
//  protected def getDigest(algorithm: String, data: String) = {
//    val crypt = MessageDigest.getInstance(algorithm)
//    crypt.reset()
//    crypt.update(data.getBytes("UTF-8"))
//    crypt
//  }
//}
//
//class Sha1Hasher extends Hasher {
//  override def hash(data: String): String = new String(Hex.encodeHex(getDigest("SHA-1", data).digest()))
//}
//
//class Sha256Hasher extends Hasher {
//  override def hash(data: String): String = new String(Hex.encodeHex(getDigest("SHA-256", data).digest()))
//}
//
//class Md5Hasher extends Hasher {
//  override def hash(data: String): String = new String(Hex.encodeHex(getDigest("MD5", data).digest()))
//}

