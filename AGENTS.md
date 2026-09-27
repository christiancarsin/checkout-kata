# Checkout Kata - Project Guidelines

## Project Overview
A checkout system kata implemented in Kotlin with Java 25, following TDD practices with emphasis on domain-driven design, cohesion, and modularity.

## Technology Stack
- **Language**: Kotlin 2.3 (JVM target 25)
- **Runtime**: Java 25
- **Build**: Maven (`pom.xml`)
- **Testing**: JUnit 5, AssertJ

- **Architecture**: Simple layered architecture (domain, application) with a port/adapter split for persistence

## Commands
- `mvn test` — run the unit test suite
- `mvn verify` — run all quality gates (tests, packaging)

## Core Principles

### 1. Domain-Centric Design
- **Business logic belongs in domain models**, not in services or controllers
- Operations that can be performed atomically on a single domain class MUST be implemented there
- Services/controllers only orchestrate - they don't contain business rules
- Domain models are rich (behavior + data), not anemic

### 2. Cohesion & Modularity
- High cohesion: related functionality grouped together
- Low coupling: modules communicate through well-defined interfaces
- Each module has a single responsibility
- Favor composition over inheritance

### 3. Test-Driven Development (TDD)
- **Red-Green-Refactor** cycle mandatory
- Write failing test first, then implementation, then refactor
- Tests drive the design of domain models
- Unit tests for domain logic, integration tests for module boundaries
- Test files mirror source structure: `src/test/kotlin/...`

## Project Structure
```
src/
├── main/kotlin/com/checkout/
│   ├── domain/
│   │   ├── model/        # Domain entities and value objects
│   │   └── port/         # Outbound ports (PricingRuleRepository)
│   ├── application/
│   │   └── service/      # Application services (orchestration only)
│   └── adapter/
│       └── repository/   # Port implementations (in-memory pricing rules)
└── test/kotlin/com/checkout/   # Tests mirror the main structure

```
Directory names mirror the package declaration (`com.checkout.*`).

## Coding Standards

### Kotlin Best Practices
- Use `data class` for value objects, `class` for entities with identity
- Prefer `val` over `var` (immutability by default); return new instances instead of mutating
- Use sealed classes for closed hierarchies (e.g., Result types)
- Extension functions for domain behavior that doesn't require state
- Coroutines for async operations
- Explicit nullability (`Type?` vs `Type`)
- Properties instead of Java-style getters (`cart.items`, not `cart.getItems()`)

### Domain Model Rules
```kotlin
// GOOD: Logic in domain model
data class Cart(private val contents: Map<Sku, Int> = emptyMap()) {
    init {
        require(contents.values.all { it > 0 }) { "Cart quantities must be positive" }
    }

    operator fun get(sku: Sku): Int = contents.getOrDefault(sku, 0)

    fun addItem(sku: Sku, quantity: Int = 1): Result<Cart> = ...
    fun calculateTotal(pricingRules: Map<Sku, PricingRule>): Result<Money> = ...
}

// AVOID: Anemic model + service with logic
class CartService {
    fun addItem(cart: Cart, sku: Sku, quantity: Int): Cart { ... }
}
```
- Invariants belong in `init { require(...) }` so every construction path is validated
- Fail fast with `require` at construction/configuration time, return `Result.Failure`
  for errors that callers are expected to handle at runtime

### Testing Standards
- Test file naming: `*Test.kt` for unit, `*IntegrationTest.kt` for integration
- Use `given/when/then` or `arrange/act/assert` structure
- Test behavior, not implementation
- Domain tests: pure unit tests, no mocks needed
- Application tests: stub the ports (anonymous implementations), test orchestration
- Assert on exact expected values - an assertion must fail when the behaviour breaks
- Cover order/edge variants (e.g. same total for every scan order)

## Development Workflow

### TDD Cycle
1. **Red**: Write a failing test for the desired behavior
2. **Green**: Write minimal code to pass the test
3. **Refactor**: Improve design while keeping tests green
4. **Commit**: Small, atomic commits with descriptive messages

### Domain-First Approach
1. Identify domain concepts (entities, value objects)
2. Write tests for domain behavior
3. Implement domain models
4. Build application services around domain

## Quality Gates
Run `mvn verify` before considering work done. It enforces:
- **Tests**: all tests must pass (surefire runs `**/*Test`)
- **No compiler warnings**: the Kotlin compiler runs with `-Werror`

## Common Patterns

### Result Type for Operations
```kotlin
sealed interface Result<out T> {
    data class Success<T>(val value: T) : Result<T>
    data class Failure(val error: DomainError) : Result<Nothing>
}
```


## Decision Log
- **2026-09-27**: Project initialized with Kotlin/Java 25, TDD, domain-centric architecture
- **2026-09-27**: Quality gates made executable: fixed surefire includes (tests were silently
  not running under `mvn test`), added `-Werror`
- **2026-09-27**: Sources moved to `src/*/kotlin/com/checkout/**` so directories match packages
- **2026-09-27**: Promotions split into `priceLine` (single item line) and `priceBundle`
  (cross-SKU); `Cart.calculateTotal` prices bundles first, then remaining lines, so totals
  no longer depend on scan order
- **2026-09-27**: `Checkout` and `CheckoutSession` are immutable - `scan` returns a new instance
- **2026-09-27**: Invalid SKU input returns `Result.Failure(InvalidSku)` instead of throwing
- **2026-09-27**: `Money` is non-negative, `pounds()` rounds instead of truncating,
  `toString()` uses `Locale.UK`
- **2026-09-27**: Removed unused dependencies (MockK, junit-jupiter-params), the dead
  `checkout-kata.iml`, and empty placeholder directories
