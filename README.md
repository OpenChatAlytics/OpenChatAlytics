#ChatAlytics

##Short Summary
ChatAlytics is a realtime platform for processing HipChat and Slack messages using Storm as the processing framework. Implementors can connect their own models into the topology for coming up with metrics. Those metrics can then be accessed through a REST API that ChatAlytics provides. This project also contains [HuBot](https://github.com/hipchat/hubot-hipchat) coffee scripts that hit the endpoints in ChatAlytics.

##Building the Project
To build the example you can just run

`mvn clean package`

You may need to also download the caseless models from the [Stanford Core NLP](http://nlp.stanford.edu/software/corenlp.shtml) website. The default one used in the config is named: english.all.3class.distsim.crf.ser.gz.

##Author
Giannis Neokleous

www.giann.is
