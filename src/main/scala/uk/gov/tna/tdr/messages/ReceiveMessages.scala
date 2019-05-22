package uk.gov.tna.tdr.messages

import com.amazonaws.services.s3.event.S3EventNotification
import com.amazonaws.services.s3.{AmazonS3ClientBuilder, model}
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.{Message, ReceiveMessageResult}
import com.softwaremill.sttp._
import uk.gov.tna.tdr.fileformat.FileFormatSenderInstances._
import uk.gov.tna.tdr.fileformat.FileFormatSyntax._
import uk.gov.tna.tdr.viruscheck.VirusCheckSenderInstances._
import uk.gov.tna.tdr.viruscheck.VirusCheckSyntax._

import scala.collection.JavaConverters._

object ReceiveMessages extends App {

  implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()

  val queueName =
    "https://sqs.eu-west-2.amazonaws.com/247222723249/tdr-file-uploads"

  val sqs = AmazonSQSClientBuilder
    .standard()
    .withRegion("eu-west-2")
    .build()
  val s3 = AmazonS3ClientBuilder
    .standard()
    .withRegion("eu-west-2")
    .build()

  def runChecks(key: String) = {
    println(s"running checks on $key")
    val obj: model.S3Object = s3.getObject("tdr-files", key)
    obj.runFileChecksAndSendToApi
    obj.runVirusCheckAndSendToApi

  }

  def getKeys(message: Message): Set[String] = {
    val event: S3EventNotification =
      S3EventNotification.parseJson(message.getBody)
    return event.getRecords.asScala
      .map(record => record.getS3.getObject.getKey)
      .toSet
  }

  val processMessage = (message: Message) => {
    val keyArr: Set[String] = getKeys(message)
    keyArr.foreach(s => runChecks(s))
  }


  println("Looking for messages")

  private var result: ReceiveMessageResult = sqs.receiveMessage(queueName)

  while (!result.getMessages.isEmpty) {
    println(s"Found ${result.getMessages.size()} messages")
    result.getMessages.forEach(message => processMessage(message))
    result = sqs.receiveMessage(queueName)
  }
}
