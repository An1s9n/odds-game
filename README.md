# odds-game

Reactive Kotlin + Spring Boot 3 application sample

This application exposes REST API of a simple odds-based game. The game allows players to bet on a randomly generated
number and win or lose based on how far from randomly generated number their bet is

## getting started

1. Ensure you have at least JDK 17 and Maven 3 installed on your machine
2. Run `git clone git@github.com:An1s9n/odds-game.git` to clone this repository and `cd` into it
3. Run `mvn test` to ensure the application builds successfully
4. Run `mvn spring-boot:run` to boot the application
5. Go to [swagger-ui](http://localhost:8080/api/v1/swagger-ui.html) and you are ready to explore the API!

## game rules

After registration each plyaer receives certain amount of credits which can be customized
via `app.game.registration-credits` configuration property (see [application.yml](src/main/resources/application.yml)).
Player can place a bet on any number within game range which can be adjusted with `app.game.range` configuration
properties. Of course player must have enough credits for bet. When bet is done, random number is generated and prize is
defined based on how far from randomly generated number player's bet is. Prize-defining rules can be changed with help
of `app.game.offset-to-prize-fun` configuration property. Each player can also request his bets, wallet transactions and
current balance. It is also possible to view top players ranked by total winnings. For more details see REST API docs
on [swagger-ui](http://localhost:8080/api/v1/swagger-ui.html)


## some implementation details

* The application is completely non-blocking: web layer is built on Spring WebFlux and data access performed with help
  of Spring Data R2DBC
* Application uses in-memory H2 database for simplicity
* To avoid concurrent game requests from the same player optimistic locking mechanism is used
* To authenticate players simple JWTs are used. Player obtains token after registration and is expected to pass it
  in `Authorization` header with all requests requiring authentication
* Tests are JUnit 5 based, Mockk is used for mocking
