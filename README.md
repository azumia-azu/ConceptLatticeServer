# ConceptLatticeServer Setup

* setup redis server

by docker-compose
```yaml
version: '3'
services:
  redis:
    image: redis
    container_name: docker_redis
    volumes:
      - ./static/datadir:/data
      - ./static/conf/redis.conf:/usr/local/etc/redis/redis.conf
      - ./static/logs:/logs
    ports:
      - 6379:6379
```

* build the project in your idea
Entry : `ConceptLatticeServerApplication.main`

* open 127.0.0.1:8080/swagger-ui.html for Api Documents

