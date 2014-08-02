vnluser
=======
HackathonVietnam2014 contest
=======

== Documentation and ideas:
https://github.com/phaikawl/vnluser/wiki

# Ideas:

- Users might store their interested information into cloud when they're surfing the Internet. Newspaper snippet, video, picture, product details, code, quote ... into their secret cloud storage.
- Sharing their interesting with other people by check to public their saved.
- Connecting people with the same interesting by recommendation.
- The final goal is a knowledge social network.

# Project is 4 main components (self written in challenge):

- Web interface, built from AngularJS
- Browser extension for Chrome PC.
- Service API, built from flask (python micro-framework)
- Data processing backend: python (NLP keywords suggest) + java (Recommendation)


## Implementaion
- Nguyen Thanh Hai:
Angular view template render: 
https://github.com/phaikawl/vnluser/tree/master/web/app/static/views/home
Home controller for web static view: https://github.com/phaikawl/vnluser/blob/master/web/app/static/js/home/home-controller.js
- Nguyen Van Hon:
Chrome extension (main files)
https://github.com/phaikawl/vnluser/blob/master/plugins/chrome/info-node-editor.js
https://github.com/phaikawl/vnluser/blob/master/plugins/chrome/info-node-editor.html
- Le Kien Truc:
API, dropbox authenticate, uploading, redis push to backend analytics: https://github.com/phaikawl/vnluser/blob/master/chk_api/chk_api.py

- Nguyen Tan Trieu:
Keywords suggestion (NLP) https://github.com/phaikawl/vnluser/blob/master/backend/keywords.py
User recommendation & Search (submodule and lib is not included due to slow network)
https://github.com/phaikawl/vnluser/tree/master/backend/netty-s2-http-server/src-processors/sample/save2dropbox
https://github.com/mc2ads/rfx-s2-http-server/blob/master/src-processors/sample/save2dropbox/business/SearchEngineLucene.java
https://github.com/mc2ads/rfx-s2-http-server/blob/master/src-processors/sample/save2dropbox/business/UserRecommender.java

# Other services:

- Dropbox: User authentication + User data storage + Meta data.
- Redis: Message queue, data stucture storage (for fastest development). In future, migration to apache-kafka (queue) and mongoDB / Google LevelDB (data storage) for scaling and performance.
- Recommendation: lucene, orientDB
- rfx framework https://github.com/mc2ads/rfx-s2-http-server . Reactive Small and Scalable HTTP Server for real-time big data processing

# Installation guilde:

Please refer installation in each component folder.

# Architecture:

https://farm4.staticflickr.com/3836/14805014945_595ce71fe7_o.png

The strong point of this architecture is scalibility. Each component could be scale individually. API call via message queue could be use for load balancing/ data sharding / asynchronous calling.

# Usecase:

- User selects text. Right click on context menu. Select saved. 
- User shall be able to list their saved items and view later.
- User shall be able to views other public saved items and recommendations. 
- User shall be able to saved other public items and recommendations.

# Todo:

- Scalability on Amazon Web Service.
- Spam detection.
- Users interaction.
- supported Firefox Android / mobile devices.
- Future with Apple CloudKit


# Refereer:
Micro Social Network (Interest Graph)
https://github.com/antirez/retwis
http://redis.io/topics/twitter-clone
http://stackoverflow.com/questions/7150306/help-with-a-microblog-twitter-clone-database-structure

Topic , Hashtags, Item
http://mikehillyer.com/articles/managing-hierarchical-data-in-mysql/
http://docs.mongodb.org/manual/tutorial/model-tree-structures/
http://stevenloria.com/finding-important-words-in-a-document-using-tf-idf/
http://textblob.readthedocs.org/en/dev/classifiers.html
https://github.com/graus/thesis


Recommendation Engine
http://www.slideshare.net/graphdevroom/works-with-persistent-graphs-using-orientdb
http://markorodriguez.com/2011/09/22/a-graph-based-movie-recommender-engine

Theme, CSS public page, manage saved items
https://github.com/dizzyn/responsive-infinite-scroll
http://demoinfinite.appspot.com/
