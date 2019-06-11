import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest._
import uk.gov.tna.tdr.messages.ReceiveMessages

class ExampleSpec extends FunSpec with Matchers with  BeforeAndAfterAll {

  override def beforeAll {
    val conf: Config = ConfigFactory.load()
    val endpoint = conf.getString("aws.endpoint")
    val queue = conf.getString("aws.queueurl")
    val queueUrl = s"$endpoint/$queue"
    val sqs = AmazonSQSClientBuilder
      .standard()
      .withEndpointConfiguration(new EndpointConfiguration(conf.getString("aws.endpoint"), "eu-west-2"))
      .build()
    if(sqs.listQueues().getQueueUrls.size() == 0) {
      sqs.createQueue("test")
    }

    val messages = sqs.receiveMessage(queueUrl).getMessages
    if(messages.size() > 0) {
      messages.forEach(message => {
        sqs.deleteMessage(queueUrl, message.getReceiptHandle)
      })
    }

    val msg = scala.io.Source.fromResource("S3Message.json").getLines().mkString("\n")
    sqs.sendMessage(queueUrl, msg)

  }


  describe("test") {
    it("does something") {
      val receive: ReceiveMessages = new ReceiveMessages()
      receive.receiveMessages()
    }
  }





}