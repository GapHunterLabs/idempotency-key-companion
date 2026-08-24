package dev.gaphunter.idempotencykeycompanion.detect

import com.intellij.psi.PsiFile
import dev.gaphunter.idempotencykeycompanion.model.EndpointMethodKind
import dev.gaphunter.idempotencykeycompanion.model.MissingIdempotencyKeyHit
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaEndpointFinder] -- same annotation-simple-name matching, no Kotlin Analysis API, K1/K2-neutral. */
object KotlinEndpointFinder {

    private val MAPPING_ANNOTATIONS_BY_KIND = mapOf(
        "PostMapping" to EndpointMethodKind.POST,
        "PutMapping" to EndpointMethodKind.PUT,
    )

    fun findAll(file: PsiFile): List<MissingIdempotencyKeyHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<MissingIdempotencyKeyHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                super.visitNamedFunction(function)
                val kind = endpointKindOf(function) ?: return
                if (hasIdempotencyHeaderParam(function)) return
                val nameIdentifier = function.nameIdentifier ?: return
                hits += MissingIdempotencyKeyHit(nameIdentifier, kind)
            }
        })
        return hits
    }

    private fun endpointKindOf(function: KtNamedFunction): EndpointMethodKind? {
        for (entry in function.annotationEntries) {
            val simpleName = entry.shortName?.asString() ?: continue
            MAPPING_ANNOTATIONS_BY_KIND[simpleName]?.let { return it }
            if (simpleName == "RequestMapping") {
                val methodArgText = argumentText(entry, "method") ?: continue
                if (methodArgText.contains("POST")) return EndpointMethodKind.POST
                if (methodArgText.contains("PUT")) return EndpointMethodKind.PUT
            }
        }
        return null
    }

    private fun hasIdempotencyHeaderParam(function: KtNamedFunction): Boolean {
        for (parameter in function.valueParameters) {
            for (entry in parameter.annotationEntries) {
                val simpleName = entry.shortName?.asString() ?: continue
                if (simpleName != "RequestHeader") continue
                val headerName = argumentText(entry, "value") ?: argumentText(entry, "name") ?: parameter.name
                if (headerName?.contains("idempotency", ignoreCase = true) == true) return true
            }
        }
        return false
    }

    /** Reads a named argument's raw expression text, or (if [name] is "value") falls back to the first positional argument -- `@RequestHeader("Idempotency-Key")` has no argument name at all. */
    private fun argumentText(entry: KtAnnotationEntry, name: String): String? {
        val named = entry.valueArguments.firstOrNull { it.getArgumentName()?.asName?.asString() == name }
        if (named != null) return named.getArgumentExpression()?.text
        if (name != "value") return null
        return entry.valueArguments.firstOrNull { it.getArgumentName() == null }?.getArgumentExpression()?.text
    }
}
