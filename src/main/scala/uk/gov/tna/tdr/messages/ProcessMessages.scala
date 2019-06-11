package uk.gov.tna.tdr.messages

import java.util.Objects

import com.amazonaws.services.s3.event.S3EventNotification
import com.amazonaws.services.s3.{AmazonS3ClientBuilder, model}
import com.amazonaws.services.sqs.model.Message
import com.typesafe.scalalogging.Logger
import uk.gov.tna.tdr.fileformat.FileFormatSenderInstances._
import uk.gov.tna.tdr.fileformat.FileFormatSyntax._
import uk.gov.tna.tdr.viruscheck.VirusCheckSenderInstances._
import uk.gov.tna.tdr.viruscheck.VirusCheckSyntax._
import uk.gov.tna.tdr.checksum.ChecksumCheckSyntax._
import uk.gov.tna.tdr.checksum.ChecksumCheckSenderInstances._

import scala.collection.JavaConverters._

class ProcessMessages {
  val logger = Logger("Process Messages")

  val s3 = AmazonS3ClientBuilder
    .standard()
    .withRegion("eu-west-2")
    .build()

  def runChecks(key: String) = {
    logger.info(s"running checks on $key")
    val obj: model.S3Object = s3.getObject("tdr-files", key)
    obj.runFileChecksAndSendToApi
//    obj.runVirusCheckAndSendToApi
//    obj.runChecksumCheckAndSendToApi
  }

  def getKeys(message: Message): Set[String] = {
    val event: S3EventNotification =
      S3EventNotification.parseJson(message.getBody)
    if (event != null) {
      logger.info(s"Found ${event.getRecords.size()} records in the message")

      return event.getRecords.asScala
        .map(record => record.getS3.getObject.getKey)
        .toSet
    }
    return Set()
  }

  val processMessage = (message: Message) => {
    val keyArr: Set[String] = getKeys(message)
    keyArr.foreach(s => runChecks(s))
  }
}
