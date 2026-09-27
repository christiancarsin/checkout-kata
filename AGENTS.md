# Checkout Kata - Project Guidelines

## Project Overview
A checkout system kata implemented in Kotlin with Java 25, following TDD practices with emphasis on domain-driven design, cohesion, and modularity.

## Technology Stack
- **Language**: Kotlin (latest stable)
- **Runtime**: Java 25
- **Testing**: JUnit 5, MockK, AssertJ
- **Architecture**: Simple layered architecture (domain, application)

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
├── main/kotlin/
│   ├── domain/           # Core domain models (entities, value objects, domain services)
│   │   ├── model/        # Domain entities and value objects
│   │   └── service/      # Domain services (cross-entity logic)
│   └── application/      # Use cases / application services
│       └── service/      # Application services (orchestration)
└── test/kotlin/          # Tests mirroring main structure
```

## Coding Standards

### Kotlin Best Practices
- Use `data class` for value objects, `class` for entities with identity
- Prefer `val` over `var` (immutability by default)
- Use sealed classes for closed hierarchies (e.g., Result types)
- Extension functions for domain behavior that doesn't require state
- Coroutines for async operations
- Explicit nullability (`Type?` vs `Type`)

### Domain Model Rules
```kotlin
// GOOD: Logic in domain model
data class Cart(private val items: List<CartItem> = emptyList()) {
    fun addItem(product: Product, quantity: Int): Cart {
        require(quantity > 0) { "Quantity must be positive" }
        // ... business logic here
    }
    
    fun calculateTotal(): Money = items.sumOf { it.subtotal }
}

// AVOID: Anemic model + service with logic
class CartService {
    fun addItem(cart: Cart, product: Product, quantity: Int): Cart { ... }
}
```

### Testing Standards
- Test file naming: `*Test.kt` for unit, `*IntegrationTest.kt` for integration
- Use `given/when/then` or `arrange/act/assert` structure
- Test behavior, not implementation
- Domain tests: pure unit tests, no mocks needed
- Application tests: mock ports, test orchestration

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
- All tests must pass
- No compiler warnings
- Static analysis: detekt (Kotlin linter)

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