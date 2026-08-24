# Idempotency Key Companion

Gutter warning icon on any Java/Kotlin Spring MVC `@PostMapping`/
`@PutMapping` (or `@RequestMapping(method = ...)`) endpoint method with
no `@RequestHeader` parameter for an idempotency key — the classic
footgun where a retried or double-submitted POST/PUT creates a
duplicate side effect (a duplicate order, a duplicate charge) because
nothing in the handler recognizes "I already saw this exact request".

## Why it exists

Idempotency keys are a well-known, standard pattern (Stripe, PayPal,
and most serious payment/order APIs require one) for exactly this
class of bug, but nothing flags an endpoint that's missing one — it's
a code-review-only discipline today, easy to forget on a new endpoint.

## Why built this way

- **100% static PSI analysis** — matches Spring annotations by simple
  name only, so it works whether the real Spring jar is on the
  classpath or not, same contract as every other framework-annotation
  detector in this catalog.
- **Java and Kotlin, one shared design.** The Kotlin side reads
  `KtAnnotationEntry` directly (never the Kotlin Analysis API), same
  K1/K2-neutral discipline already proven this session in
  `n-plus-one-query-companion`.

## v0.1 scope — stated honestly, not exhaustively

Spring MVC only (not JAX-RS or other frameworks). The idempotency-key
parameter is recognized by its `@RequestHeader` name containing
"idempotency" (case-insensitive) — a team using a differently-named
header convention isn't covered yet.

## Usage

Open any Java/Kotlin Spring controller. A POST/PUT endpoint with no
idempotency-key header parameter shows a warning icon on the method
name.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
