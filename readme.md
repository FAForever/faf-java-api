# Spring Boot based FAF-API Prototype
 
 This is a prototype of a Spring Boot based API application for Forged Alliance Forever.
 
## How to run from source

1. Clone the repository
1. Import the project into IntelliJ
1. Configure your JDK 8 if you haven't already
1. Make sure you have the _IntelliJ Lombok plugin_ installed
1. Launch `FafApiApplication`
 
## How to run from binary
 
 Either check out the source code and execute the run configuration `FafApiApplication`, or run it directly
 from the published Docker image like so:
 
```
docker run --name faf-api \
  -e DATABASE_ADDRESS=192.168.99.100:3306 \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=banana \
  -e DATABASE_NAME=faf_lobby \
  -d micheljung/faf-api:0.0.1-SNAPSHOT
```

To run in production, you probably want to create an environment file (e.g. `env.list`):

```
DATABASE_ADDRESS=stable_faf-db_1
DATABASE_USERNAME=faf_lobby
DATABASE_PASSWORD=password
DATABASE_NAME=faf_lobby
API_PROFILE=prod
JWT_SECRET=<your secret>
```

And run with:
```
docker run --name faf-api \
  --env-file ./env.list \
  -d micheljung/faf-api:0.0.1-SNAPSHOT
```

## Sample routes

* [List event definitions](http://localhost:8080/data/event_definition)
* [List 5 maps with more than 8 players](http://localhost:8080/data/map_version?filter=(maxPlayers=gt=8)&page[size]=5)
* [List UI mods, sorted by last updated ascending](http://localhost:8080/data/mod_version?filter=(type=='UI')&sort=-updateTime)
* List players: [http://localhost:8080/data/player](http://localhost:8080/data/player)
* List player events: [http://localhost:8080/data/player_event](http://localhost:8080/data/player_event)
* List replays: [http://localhost:8080/data/map](http://localhost:8080/data/map)
* API documentation: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Technology Stack

This project uses:

* Java 8 as the programming language
* [Spring Boot](https://projects.spring.io/spring-boot/) as a framework
* [Elide](http://elide.io/) to serve [JSON-API](http://jsonapi.org/) conform data
* [Gradle](https://gradle.org/) as a build automation tool
* [Docker](https://www.docker.com/) to deploy and run the application
* [Swagger](http://swagger.io/) as an API documentation tool
