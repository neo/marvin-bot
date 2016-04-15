# Notes from code review 4/15/2016 - MikeG

* Missing getting started guide/prerequisites - as a developer who is interested in contributing to Marvin, I would like a *Getting Started* guide that leads me through the process to go from cloning repo to running Marvin and the test suite so that I minimize the amount of time I need to spend getting up and running.

* Pulling in dependencies - I personally try to minimize the dependencies that I pull in unless they really add value. For example, SlackController uses `@GetJson` from `org.springframework.composed`. After what happened in the Node community with `left-pad`, I am even more conservative about this.

* Constructor injection of dependencies - Yes Spring makes it easy to @Autowire private fields and yes most, if not all, of the guides do this but I find constructor injection a much better default pattern to use. Not a huge deal with controllers because they are intimately tied to Spring but becomes more of an issue in non-controller classes like `RemoteApiService`. Using constructor injection makes them easier to use outside of the Spring Framework.

* @Value annotation - the Spring Boot team is moving more towards [@ConfigurationProperties](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-typesafe-configuration-properties). For one off values, @Value is OK. If you have a bunch of @Values that could go into an object, use `@ConfigurationProperties`.

* Throwing `Exception` - better to throw specific exceptions. Better documentation of what could go wrong.

* @RequestParams - better to us an object that represents the request. Prevents [primitive obsession](https://sourcemaking.com/refactoring/smells/primitive-obsession)

* Slack controller - `index` method is pretty long. Makes it hard to test because your MockMVC tests need to test all the conditional logic. Moving this into smaller object makes it easy to test.

* Lombok - use at your own risk. I go back and forth on it's use because of the magic code generation. Can cause issues in IntelliJ if you don't configure the annotation pre-processor. This is one reason I love Kotlin and it's compact default constructor syntax.

* Dead code - It adds noise that I have to parse to see if I need to look at it.


