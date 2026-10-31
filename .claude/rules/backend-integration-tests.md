---
paths:
  - "backend/src/test/java/**/*IntegrationTests.java"
  - "backend/src/test/java/**/*Client.java"
  - "backend/src/test/java/**/*Assertions.java"
---

# Backend: integration tests writing style

## Structure

- One `<Feature>IntegrationTests` class per endpoint/use case, in the feature package (`user/MeIntegrationTests.java`), extending `AbstractIntegrationTest`.
- HTTP calls live in `<Domain>Client` classes (`UserClient`, `SecurityClient`), built on `RestClient` and registered in `AbstractIntegrationTest`. Tests never use `restClient` directly.
- Each endpoint has an `xxxOk` method (returns `ResponseEntity<SuccessDto>`) and an `xxxKo` method (returns `ResponseEntity<ErrorDto>`). Add a session-less `xxxKo` overload for the unauthenticated case. Authenticated calls take `String sessionCookie` as first parameter.
- Response checks live in `<Domain>Assertions` (`assertXxxOk`) and `SharedAssertions.assertKo`, as `public static` methods that return the body when there is one. Tests do not repeat status/body field checks inline.
- Helper classes are `final` with a private no-args constructor (`@NoArgsConstructor(access = AccessLevel.PRIVATE)`), or `@RequiredArgsConstructor` for clients.

## Test class layout

Order: `private static final` constants, `@Autowired` fields, state fields, `@BeforeEach`, `@AfterEach`, private helpers, then tests.

- Constants: one `EMAIL` unique per test class (`"me@example.com"`), plus named constants for every other literal (`PASSWORD`, `WRONG_PASSWORD`, `PASSWORD_CHAR`). No magic literals in test bodies.
- Setup creates users through the API (`userClient.createUserOk`), not through the repository. The repository is only used to flip state the API cannot (`enableUser()`), to clean up, and to assert persistence.
- `@AfterEach` always runs `userRepository.deleteAll()` so classes stay independent.
- Methods are named `beforeEach` / `afterEach`, and are package-private.

## Test methods

- Name: `should<Outcome>_when<Condition>`, e.g. `shouldReturn401_whenNotAuthenticated`, `shouldStoreHashedPassword_whenRequestIsValid`. Use `shouldReturn<status>` for pure status/error cases and a behavior verb otherwise. Combine with `And` when a failure case also checks that state is untouched (`shouldReturn400AndKeepPassword_whenOldPasswordIsWrong`).
- Body is arrange / act / assert with no blank lines or `// given/when/then` comments, except where a test has several distinct act+assert phases, which are separated by one blank line.
- Build the request in a local `request` variable, call the client, store the result in `response`, then assert. Declare the explicit type (`ResponseEntity<UserDto> response`), not `var`.
- One behavior per test. Do not bundle several unrelated assertions into one test; add another test instead.
- Use AssertJ (`assertThat`) only. Extract any repeated multi-line assertion into the `*Assertions` classes.
- Error cases use `assertKo(response, HttpStatus.X, ROUTE_CONSTANT[, expectedMessage])`. Pass the message (from the exception's `ERROR_MSG`) when the message is part of the contract, omit it for bean-validation 400s and 401s.
- Use route constants from `Routes` and exception constants (`InvalidCredentialsException.ERROR_MSG`), never hardcoded paths or messages.
- Invalid-input variants use `@ParameterizedTest` with `@NullAndEmptySource` and `@ValueSource`. Length boundaries use `PASSWORD_CHAR.repeat(MIN_LENGTH - 1)` and `repeat(MAX_LENGTH + 1)` with the production constants.
- When the effect must be verified beyond the response, read it back (`userRepository.findByEmail(...)`, a follow-up login, a follow-up `me` call) rather than trusting the response alone.

## Coverage per endpoint

At minimum: the success case, each business error (with its status and message), each validated field (null, blank, too short, too long, malformed), and the unauthenticated case for secured endpoints.

## Formatting

- 2-space indent, imports ordered as in existing files (static first, then `org.bkd`, then third-party), no wildcard imports.
- Fluent `RestClient` chains put each call on its own line, aligned under the first `.`.
