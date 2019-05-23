package uk.gov.tna.tdr.messages

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.typesafe.scalalogging.Logger

object ReceiveMessages extends App {

  val logger = Logger("Receive Messages")

  val queueName =
    "https://sqs.eu-west-2.amazonaws.com/247222723249/tdr-file-uploads"

  val sqs = AmazonSQSClientBuilder
    .standard()
    .withRegion("eu-west-2")
    .build()

  logger.info("Looking for messages")

  private var result: ReceiveMessageResult = sqs.receiveMessage(queueName)
  val messageProcessor: ProcessMessages = new ProcessMessages
  while (!result.getMessages.isEmpty) {
    logger.info(s"Found ${result.getMessages.size()} messages")
    result.getMessages.forEach(message =>
      messageProcessor.processMessage(message))
    logger.info("Processed messages, looking for more")
    result = sqs.receiveMessage(queueName)
  }
  logger.info("Done")
}
