package dev.gaphunter.idempotencykeycompanion.model

/** The 2 real HTTP methods this plugin flags -- POST/PUT are the classic "unsafe, non-idempotent by default" cases a real idempotency-key header protects against retries/duplicate submits. GET/DELETE/PATCH are out of v0.1 scope, documented honestly. */
enum class EndpointMethodKind { POST, PUT }

/** One Spring endpoint method found without an idempotency-key header parameter -- the whole finding. */
data class MissingIdempotencyKeyHit(val methodNameElement: com.intellij.psi.PsiElement, val kind: EndpointMethodKind)
