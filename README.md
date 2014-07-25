#HipChalytics

##Short Summary
HipChalytics is a realtime processing platform for processing HipChat messages using Storm as the processing framework. Implementors can hook up their own models into the topology for coming up with metrics. Those metrics can then be accessed through a REST API that HipChalytics provides. This project also contains [HuBot](https://github.com/hipchat/hubot-hipchat) coffee scripts that hit the endpoints in HipChalytics.

##Building the Project
To build the example you can just run

`mvn clean package`

##Author
Giannis Neokleous

www.giann.is
