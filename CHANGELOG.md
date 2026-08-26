<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Idempotency Key Companion Changelog

## [Unreleased]

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct real findings, a one-time
  notification asks whether to rate the plugin on Marketplace, with a
  permanent "Don't ask again" option. Standard mechanism used
  catalog-wide since 2026-08-24 (`CONSTITUTION.md` §7.2), rolled out
  to this plugin now.

## [0.1.0]

### Added

- Gutter warning icon on any Java/Kotlin Spring MVC `@PostMapping`/
  `@PutMapping`/`@RequestMapping(method = ...)` endpoint with no
  `@RequestHeader` parameter for an idempotency key.
- 100% static PSI analysis, Java and Kotlin, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/idempotency-key-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/idempotency-key-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/idempotency-key-companion/commits/0.1.0
