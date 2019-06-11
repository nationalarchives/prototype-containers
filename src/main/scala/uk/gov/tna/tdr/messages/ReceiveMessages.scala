package uk.gov.tna.tdr.messages

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger

class ReceiveMessages {

  def receiveMessages(): Unit = {
    val config: Config = ConfigFactory.load()

    val logger = Logger("Receive Messages")

    val endpoint = config.getString("aws.endpoint")
    val queueName = config.getString("aws.queueurl")
    val queueUrl = s"$endpoint/$queueName"

    val sqs = AmazonSQSClientBuilder
      .standard()
      .withEndpointConfiguration(new EndpointConfiguration(endpoint, "eu-west-2"))
      .build()

    logger.info("Looking for messages")


    var result: ReceiveMessageResult = sqs.receiveMessage(queueUrl)
    val messageProcessor: ProcessMessages = new ProcessMessages
    while (!result.getMessages.isEmpty) {
      logger.info(s"Found ${result.getMessages.size()} messages")
      result.getMessages.forEach(message => {
        messageProcessor.processMessage(message)
        sqs.deleteMessage(queueUrl, message.getReceiptHandle)
      })
      logger.info("Processed messages, looking for more")
      result = sqs.receiveMessage(queueUrl)
    }
    logger.info("Done")
  }

}
