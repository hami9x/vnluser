vnluser
=======
HackathonVietnam2014 contest
=======

== Project is 4 main components:
- Web interface, built from AngularJS
- Browser exenstion for Chrome PC, to be supported Firefox Android
- Service API, built from flask (python micro-framework)
- Data processing backend: python (NLP keywords suggest) + java (Recommendation)

== Other services:

- Dropbox: User authentication + User data storage + Meta data.
- Redis: Message queue, data stucture storage (for fastest development). In future, migration to apache-kafka (queue) and mongoDB / Google LevelDB (data storage) for scaling and performance.
- Recommendation: lucene, orientDB
- rfx framework https://github.com/mc2ads/rfx-s2-http-server . Reactive Small and Scalable HTTP Server for real-time big data processing

== Installation guilde:

Please refer installation in each component folder.

== Documentation and ideas:
