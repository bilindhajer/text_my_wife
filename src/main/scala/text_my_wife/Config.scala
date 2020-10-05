package text_my_wife

case class Config(
   twilioAccountSID: String = "",
   twilioAuthToken: String = "",
   paperQuotesAPIKey: String = "",
   fromNum: String = "",
   toNum: String = ""
)