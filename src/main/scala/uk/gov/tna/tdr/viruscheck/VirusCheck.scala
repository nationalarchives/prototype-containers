package uk.gov.tna.tdr.viruscheck

import com.amazonaws.services.s3.model.S3Object
import fi.solita.clamav.ClamAVClient
import uk.gov.tna.tdr.graphql.GraphqlSenderInstances._
import uk.gov.tna.tdr.graphql.GraphqlSyntax._

trait VirusCheckSender[A] {
  def runVirusCheckAndSendToApi(value: A): Unit
}

case class VirusCheckResult(key: String, clean: Boolean)

object VirusCheckSenderInstances {
  implicit val responseSender: VirusCheckSender[S3Object] = (value: S3Object) => {
    println("checking for viruses")
    Thread.sleep(10000)
    val client = new ClamAVClient("localhost", 3310)
    var reply: Array[Byte] = new Array[Byte](10)
    try {
      reply = client.scan(value.getObjectContent)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    println("virus scan complete")
    val virusCheckResult: VirusCheckResult = VirusCheckResult(value.getKey, ClamAVClient.isCleanReply(reply))
    virusCheckResult.sendToApi

  }
}

object VirusCheckSyntax {

  implicit class VirusCheckWriterOps[A](value: A) {
    def runVirusCheckAndSendToApi(implicit w: VirusCheckSender[A]): Unit = w.runVirusCheckAndSendToApi(value)
  }

}