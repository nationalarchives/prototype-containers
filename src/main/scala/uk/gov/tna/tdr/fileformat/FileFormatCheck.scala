package uk.gov.tna.tdr.fileformat

import com.amazonaws.services.s3.model.S3Object
import com.softwaremill.sttp.{multipart, sttp, _}
import com.typesafe.scalalogging.Logger
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import uk.gov.tna.tdr.graphql.GraphqlSyntax._
import uk.gov.tna.tdr.graphql.GraphqlSenderInstances._

trait FileFormatSender[A] {
  def runFileChecksAndSendToApi(value: A): Unit
}



object FileFormatSenderInstances {
  implicit val fileFormatSender: FileFormatSender[S3Object] =
    (value: S3Object) => {

      val logger = Logger("File Format Check")

      def decodeAndSend = (body: String) => {
        val either: Either[circe.Error, Response] = decode[Response](body)
        either match {
          case Right(siegfriedResponse) => siegfriedResponse.sendToGraphqlServer
          case Left(error)              => println(error)
        }
      }

      implicit val backend: SttpBackend[Id, Nothing] =
        HttpURLConnectionBackend()
      logger.info(s"Checking file format for ${value.getKey}")
      val request = sttp
        .multipartBody(
          multipart("file", value.getObjectContent).fileName(value.getKey))
        .post(uri"http://localhost:8080/identify?format=json")

      val response = request.send()

      response.body match {
        case Right(body) => decodeAndSend(body)
        case Left(error) => println(error)
      }

    }
}

object FileFormatSyntax {

  implicit class FileFormatWriterOps[A](value: A) {
    def runFileChecksAndSendToApi(implicit w: FileFormatSender[A]): Unit =
      w.runFileChecksAndSendToApi(value)
  }

}
