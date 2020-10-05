
text_my_wife
--------

A lightweight scala application to send your loved one a SMS message with the 'quote of the day'

```
sbt clean package assembly
java -cp text_my_wife.App -jar /path/to/jar/text_my_wife-assembly-0.1.jar \
  --twilioAccountSID <twilio_account_sid> \
  --twilioAuthToken <twilio_auth_token> \
  --paperQuotesUser <paper_quotes_user> \
  --paperQuotesKey <paper_quotes_key> \
  --fromNum <from_num> \
  --toNum <to_num>
```