package uk.gov.tna.tdr.messages


object ContainersApp extends App {
  val receiveMessages: ReceiveMessages = new ReceiveMessages()
  receiveMessages.receiveMessages()

}
