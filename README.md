# checkout-kata

A supermarket checkout that calculates the total price of scanned items, with
promotions for multi-priced items, buy-n-get-one-free and cross-SKU meal deals.

Implemented in Kotlin on the JVM (bytecode target 25), with TDD and a
domain-centric design. See `AGENTS.md` for the project guidelines.

## Pricing rules

| SKU | Unit price | Promotion |
|-----|------------|-----------|
| A   | 50p        | 3 for £1.30 |
| B   | 75p        | 2 for £1.25 |
| C   | 25p        | Buy 3, get 1 free |
| D   | 150p       | D and E meal deal for £3 |
| E   | 200p       | D and E meal deal for £3 |

Items may be scanned in any order - the total is the same for every scan order.

## Usage

```kotlin
val service = CheckoutService(InMemoryPricingRuleRepository())

val session = service.startCheckout()
    .scan('A').getOrThrow()
    .scan('A').getOrThrow()
    .scan('A').getOrThrow()

session.total().getOrThrow()   // 130p (3 for £1.30)
```

Pricing rules can be supplied per transaction, either explicitly:

```kotlin
service.startCheckout(pricingRules)
```

or through the `PricingRuleRepository` port when no rules are passed.

## Build

```bash
mvn test     # run the unit test suite
mvn verify   # quality gates: tests, packaging
```

## Layout

```
src/main/kotlin/com/checkout/
├── domain/model      # Cart, Checkout, Money, Sku, PricingRule, Promotion, Result
├── domain/port       # PricingRuleRepository (outbound port)
├── application/service  # CheckoutService, CheckoutSession (orchestration)
└── adapter/repository   # InMemoryPricingRuleRepository
src/test/kotlin/com/checkout/  # tests mirror the main structure
```
