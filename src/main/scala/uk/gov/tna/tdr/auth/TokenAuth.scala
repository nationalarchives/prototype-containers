package uk.gov.tna.tdr.auth

import java.nio.charset.StandardCharsets
import java.util.Base64

import com.softwaremill.sttp.sttp
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.circe.parser.decode

class TokenAuth {

  case class AuthResponse(`access_token`: String)

  def getToken(): AuthResponse = {
    implicit val backend: SttpBackend[Id, Nothing] = HttpURLConnectionBackend()

    val clientId: String = sys.env("AWS_COGNITO_CLIENT_ID")
    val clientSecret: String = sys.env("AWS_COGNITO_CLIENT_SECRET")

    val authorisation: String = Base64.getEncoder.encodeToString(
      s"$clientId:$clientSecret".getBytes(StandardCharsets.UTF_8))

    val request = sttp
      .body(Map("grant_type" -> "client_credentials"))
      .headers(Map("Authorization" -> s"Basic $authorisation"))
      .post(uri"https://tdr.auth.eu-west-2.amazoncognito.com/oauth2/token")

    val response = request.send()
    return response.body match {
      case Right(json) => decode[AuthResponse](json).getOrElse(null)
      case Left(s)     => println(s); null
    }
  }
}
