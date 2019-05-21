package uk.gov.tna.tdr.messages

import com.amazonaws.services.s3.event.S3EventNotification
import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.{Message, ReceiveMessageResult}
import com.amazonaws.services.s3.{AmazonS3ClientBuilder, model}

import scala.collection.JavaConverters._

import fi.solita.clamav._

object ReceiveMessages extends App {
  val queueName = "https://sqs.eu-west-1.amazonaws.com/247222723249/tdr-file-uploads"

  val sqs = AmazonSQSClientBuilder.standard()
      .withRegion("eu-west-1")
      .build()
  val s3 = AmazonS3ClientBuilder.standard()
      .withRegion("eu-west-1")
      .build()

  def runChecks(key: String) = {
    println(s"running checks on ${key}")
    val obj: model.S3Object = s3.getObject("tna-tdr-files", key)
    println(virusCheck(obj))



  }

  def virusCheck(obj: model.S3Object): Boolean = {
    println("checking for viruses")
    val input: S3ObjectInputStream = obj.getObjectContent
    Thread.sleep(10000)
    val client = new ClamAVClient("localhost", 3310)
    var reply: Array[Byte] = new Array[Byte](10)
    try {
      reply = client.scan(input)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    println("virus scan complete")
    return ClamAVClient.isCleanReply(reply)
  }

  def getKeys(message: Message): Set[String] = {
    val event: S3EventNotification = S3EventNotification.parseJson(message.getBody)
    return event.getRecords.asScala
      .map(record => record.getS3.getObject.getKey).toSet

  }

  val processMessage = (message: Message) => {
    val keyArr: Set[String] = getKeys(message)
    keyArr.foreach(s => runChecks(s))

    //    sqs.deleteMessage(queueName, message.getReceiptHandle)
  }
  println("Looking for messages")
  private var result: ReceiveMessageResult = sqs.receiveMessage(queueName)

  while (!result.getMessages.isEmpty) {
    println(s"Found ${result.getMessages.size()} messages")
    result.getMessages.forEach(message => processMessage(message))
    result = sqs.receiveMessage(queueName)
  }
}
