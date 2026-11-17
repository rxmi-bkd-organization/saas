---
paths:
  - "backend/src/test/java/**"
---

# Backend: test naming conventions

## Classes

- Integration tests: `<UseCase>IntegrationTests`, one per endpoint or use case. Mobile variants add `Mobile` before the suffix.
- Architecture tests: `ArchitectureTests`.
- Always plural `Tests`, never `Test` or `IT`. The only `...Test` class is the base class `AbstractIntegrationTest`.
- Test helpers: `<Domain>Client`, `<Domain>Assertions`, in the same package as the tests that use them.

## Integration test methods

- Format: `should<Outcome>_when<Condition>`, in camelCase on each side of the underscore.
- `<Outcome>` is `Return<status>` for pure status/error cases, otherwise a behavior verb.
- Join several outcomes with `And` when a failure case also checks that state is untouched.
- `<Condition>` describes the input or state, not the implementation.
- Reuse the same condition wording across classes for the same case (valid request, not authenticated, disabled user, invalid/blank field, too short, too long).
- Parameterized tests keep the same name and take the parameter as argument.
- Lifecycle methods are named `beforeEach` and `afterEach`.

## Architecture test methods

- ArchUnit rules use snake_case sentences: `<subject>_should_<rule>`.
- Private helpers use camelCase.

## Other

- No `@DisplayName`: the method name is the description.
- Constants in tests are `UPPER_SNAKE_CASE`.
