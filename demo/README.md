# Demo data for screenshots

`OrderController.java` — `createOrder` is missing an idempotency key
(should show a warning), `createPayment` has one correctly (should not).

## How to get the screenshot

1. `./gradlew runIde` from `idempotency-key-companion`, open this
   `demo/` folder as the project.
2. Full Screen, open `OrderController.java` — a warning icon should
   appear on `createOrder` but not on `createPayment`.
3. Screenshot with both methods and the icon contrast visible, save
   into `idempotency-key-companion/docs/screenshots/`. Close the
   sandbox.
