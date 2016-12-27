# Spring Boot based FAF-API Prototype
 
 This is a prototype of a Spring Boot based API application for Forged Alliance Forever.
 
## How to run
 
 Either check out the source code and execute the run configuration `FafApiApplication`, or run it directly
 from the published Docker image like so:
 
```
docker run --name faf-api \
  -e DATABASE_ADDRESS=192.168.99.100:3306 \
  -e DATABASE_USERNAME=root \
  -e DATABASE_PASSWORD=banana \
  -e DATABASE_NAME=faf_lobby \
  -d micheljung/faf-api:latest
```

To run in production, you probably want to create an environment file (e.g. `env.list`):

```
DATABASE_ADDRESS=stable_faf-api_1
DATABASE_USERNAME=faf_lobby
DATABASE_PASSWORD=password
DATABASE_NAME=faf_lobby
API_PROFILE=prod
```

And run with:
```
docker run --name faf-api \
  --env-file ./env.list \
  -d micheljung/faf-api:latest
```

## Technology Stack

This project uses:

* Java 8 as the programming language
* [Spring Boot](https://projects.spring.io/spring-boot/) as a framework
* [Elide](http://elide.io/) to serve [JSON-API](http://jsonapi.org/) conform data
* [Gradle](https://gradle.org/) as a build automation tool
* [Docker](https://www.docker.com/) to deploy and run the application
* [Swagger](http://swagger.io/) as an API documentation tool
