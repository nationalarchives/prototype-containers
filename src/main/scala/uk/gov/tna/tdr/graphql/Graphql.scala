package uk.gov.tna.tdr.graphql

import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp.{HttpURLConnectionBackend, Id, SttpBackend, sttp, _}
import org.json4s.native.Serialization
import uk.gov.tna.tdr.auth.TokenAuth
import uk.gov.tna.tdr.fileformat.Response
import uk.gov.tna.tdr.viruscheck.VirusCheckResult

trait GraphqlSender[A] {
  def sendToApi(value: A): Unit
}

object GraphqlSenderInstances {
  case class GraphqlQuery(query: String)

  def sendToGraphqlServer(query: String) = {
    val tokenAuth = new TokenAuth

    implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
    val graphqlQuery = GraphqlQuery(query)

    implicit val serialization: Serialization.type =
      org.json4s.native.Serialization

    val request = sttp
      .body(graphqlQuery)
      .contentType("application/json")
      .headers(Map("Authorization" -> s"Bearer ${tokenAuth.getToken()}"))
      .post(
        uri"https://hudcqpobs7.execute-api.eu-west-2.amazonaws.com/dev/graphql")

    request.send()
  }

  implicit val responseSender: GraphqlSender[Response] = (value: Response) => {
    val query: String = s"some json from $value"

    sendToGraphqlServer(query) //This won't work, make it a properly formatted graphql request
  }

  implicit val virusCheckSender: GraphqlSender[VirusCheckResult] = (value: VirusCheckResult) => {
    val query: String = s"some json from $value"
    sendToGraphqlServer(query) //This won't work, make it a properly formatted graphql request
  }
}

object GraphqlSyntax {

  implicit class GraphqlWriterOps[A](value: A) {
    def sendToApi(implicit w: GraphqlSender[A]): Unit = w.sendToApi(value)
  }

}
