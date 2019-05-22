package uk.gov.tna.tdr.fileformat

import com.amazonaws.services.s3.model.S3Object
import com.softwaremill.sttp.{multipart, sttp, _}
import io.circe.Error
import io.circe.generic.auto._
import io.circe.parser.decode



trait FileFormatSender[A] {
  def runFileChecksAndSendToApi(value: A): Unit
}

object FileFormatSenderInstances {
  implicit val responseSender: FileFormatSender[S3Object] = (value: S3Object) => {
    import uk.gov.tna.tdr.graphql.GraphqlSenderInstances._
    import uk.gov.tna.tdr.graphql.GraphqlSyntax._


    def getSiegfriedResponse = {
      implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()
      val request = sttp
        .multipartBody(multipart("file", value.getObjectContent).fileName(value.getKey))
        .post(uri"http://localhost:5138/identify?format=json")

      val response = request.send()
      response
    }

    def processJson(json: String): Unit = {


      val either: Either[Error, Response] = decode[Response](json)

      either match {
        case Right(siegfriedResponse) => siegfriedResponse.sendToApi
        case Left(error)     => println(error)
      }
    }

    val response = getSiegfriedResponse
    response.body match {
      case Right(json) => processJson(json)
      case Left(s)     => println(s)
    }

  }
}

object FileFormatSyntax {

  implicit class FileFormatWriterOps[A](value: A) {
    def runFileChecksAndSendToApi(implicit w: FileFormatSender[A]): Unit = w.runFileChecksAndSendToApi(value)
  }

}