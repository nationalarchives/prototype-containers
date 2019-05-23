package uk.gov.tna.tdr.checksum

import java.security.MessageDigest

import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.util.IOUtils
import com.typesafe.scalalogging.Logger
import uk.gov.tna.tdr.graphql.GraphqlSyntax._
import uk.gov.tna.tdr.graphql.GraphqlSenderInstances._

trait ChecksumCheckSender[A] {
  def runChecksumCheckAndSendToApi(value: A): Unit
}

case class ChecksumCheckResult(key: String, checksum: String)

object ChecksumCheckSenderInstances {
  implicit val checksumSender: ChecksumCheckSender[S3Object] =
    (value: S3Object) => {
      val logger = Logger("Checksum Check")
      logger.info(s"calculating checksum for ${value.getKey}")
      val bytes = IOUtils.toByteArray(value.getObjectContent)
      val checksumArr: Array[Byte] =
        MessageDigest.getInstance("SHA-256").digest(bytes)
      val checksum: String =
        checksumArr.map("%02X".format(_)).mkString.toLowerCase
      val result: ChecksumCheckResult =
        ChecksumCheckResult(value.getKey, checksum)
      result.sendToGraphqlServer
    }
}

object ChecksumCheckSyntax {

  implicit class ChecksumCheckWriterOps[A](value: A) {
    def runChecksumCheckAndSendToApi(implicit w: ChecksumCheckSender[A]): Unit =
      w.runChecksumCheckAndSendToApi(value)
  }

}
