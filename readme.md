# FAF API

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/9ac3915f155c4bfb869b8fe123b6832e)](https://www.codacy.com/gh/FAForever/faf-java-api/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FAForever/faf-java-api&amp;utm_campaign=Badge_Grade)
[![Build](https://github.com/FAForever/faf-java-api/actions/workflows/build.yaml/badge.svg)](https://github.com/FAForever/faf-java-api/actions/workflows/build.yaml)
[![Coverage Status](https://coveralls.io/repos/github/FAForever/faf-java-api/badge.svg?branch=develop)](https://coveralls.io/github/FAForever/faf-java-api?branch=develop)
 
This is the official FAForever API. Amongst others, the API offers the following functionality:
- OAuth 2.0 compliant authentication server
- User management (registration, password reset, etc.)
- JSON-API compliant REST API for lots of community data (i.e. map, mod & replay vault, game statistics, etc.)
- Leaderboards
- Clan management
- Vault upload functionality
- Internal FAF featured mod deployments
- Challonge proxy
 
## How to run

### Setup database

The application requires a database scheme in the right version. To create this database please checkout the project [faf-stack](https://github.com/FAForever/faf-stack), open a shell terminal (git bash on Windows) and run the script `scripts/init-db.sh`. This will setup the database in the latest version and configure the users for you.

### From source

In order to run the application from source code:

1. Clone the repository
1. Import the project into IntelliJ. For some reason, IntelliJ deletes launch configurations after import. Please revert such deleted files first (Version Control (Alt+F9) -> Local Changes)
1. Configure your JDK if you haven't already
1. Make sure you have the _IntelliJ Lombok plugin_ installed
1. Set up a [FAF database](https://github.com/FAForever/db).
1. Launch `FafApiApplication`
 
### From binary

Given the number of required configuration values, it's easiest to run the API using [faf-stack](https://github.com/FAForever/faf-stack):

    docker-compose up -d faf-java-api

## Database dependency

Due to potential issues with Hibernate, the application checks on startup if the flyway migration version matches the expected version and otherwise fails to boot.

* To increment the db version you need to change it in two places:
  * application.yml: `faf-api.database.schema-version` (just an integer of the version)
  * .github/workflows/build.yaml: Global variable `FAF_DB_VERSION` (git tag of the db release, usually prefixed with a `v`)
* In some cases it might make sense to override the required version on the server. Use the env variable `DATABASE_SCHEMA_VERSION`
 

## Sample routes

* [API documentation](http://localhost:8010)
* [List event definitions](http://localhost:8010/data/event)
* [List 5 maps with more than 8 players](http://localhost:8010/data/mapVersion?filter=(maxPlayers=gt=8)&page[size]=5)
* [List UI mods, sorted by last updated ascending](http://localhost:8010/data/modVersion?filter=(type=='UI')&sort=-updateTime)
* [List all players](http://localhost:8010/data/player)
* [List events of players](http://localhost:8010/data/playerEvent)
* [List replays](http://localhost:8010/data/game?filter=(endTime=isnull=true))

## Technology Stack

This project uses:

* Java 11 as the programming language
* [Spring Boot](https://projects.spring.io/spring-boot/) as a framework
* [Hibernate ORM](http://hibernate.org/orm/) as ORM mapper
* [Elide](http://elide.io/) with [RSQL filters](http://elide.io/pages/guide/08-filters.html#rsql) to serve [JSON-API](http://jsonapi.org/) conform data
* [Gradle](https://gradle.org/) as a build automation tool
* [Docker](https://www.docker.com/) to deploy and run the application
* [Swagger](http://swagger.io/) as an API documentation tool
