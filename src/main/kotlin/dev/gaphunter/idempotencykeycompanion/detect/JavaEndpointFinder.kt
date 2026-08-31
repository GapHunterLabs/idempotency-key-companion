package dev.gaphunter.idempotencykeycompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import dev.gaphunter.idempotencykeycompanion.model.EndpointMethodKind
import dev.gaphunter.idempotencykeycompanion.model.MissingIdempotencyKeyHit

/**
 * Finds Java Spring MVC endpoint methods (`@PostMapping`/`@PutMapping`,
 * or `@RequestMapping(method = RequestMethod.POST/PUT)`) with no
 * `@RequestHeader` parameter that looks like an idempotency key.
 * Matches annotations by simple name only, same "works whether the real
 * Spring jar is on the classpath or not" contract as every other
 * framework-annotation detector in this catalog.
 */
object JavaEndpointFinder {

    private val MAPPING_ANNOTATIONS_BY_KIND = mapOf(
        "PostMapping" to EndpointMethodKind.POST,
        "PutMapping" to EndpointMethodKind.PUT,
    )

    fun findAll(file: PsiFile): List<MissingIdempotencyKeyHit> {
        val hits = mutableListOf<MissingIdempotencyKeyHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethod(method: PsiMethod) {
                super.visitMethod(method)
                val kind = endpointKindOf(method) ?: return
                if (hasIdempotencyHeaderParam(method)) return
                val nameIdentifier = method.nameIdentifier ?: return
                hits += MissingIdempotencyKeyHit(nameIdentifier, kind)
            }
        })
        return hits
    }

    private fun endpointKindOf(method: PsiMethod): EndpointMethodKind? {
        for (annotation in method.modifierList?.annotations.orEmpty()) {
            val simpleName = annotation.nameReferenceElement?.referenceName ?: continue
            MAPPING_ANNOTATIONS_BY_KIND[simpleName]?.let { return it }
            if (simpleName == "RequestMapping") {
                val methodAttrText = annotation.findAttributeValue("method")?.text ?: continue
                if (methodAttrText.contains("POST")) return EndpointMethodKind.POST
                if (methodAttrText.contains("PUT")) return EndpointMethodKind.PUT
            }
        }
        return null
    }

    private fun hasIdempotencyHeaderParam(method: PsiMethod): Boolean {
        for (parameter in method.parameterList.parameters) {
            for (annotation in parameter.modifierList?.annotations.orEmpty()) {
                val simpleName = annotation.nameReferenceElement?.referenceName ?: continue
                if (simpleName != "RequestHeader") continue
                val headerName = requestHeaderNameText(annotation) ?: parameter.name
                if (looksLikeIdempotencyHeaderName(headerName)) return true
            }
        }
        return false
    }

    private fun requestHeaderNameText(annotation: PsiAnnotation): String? {
        val value = annotation.findAttributeValue("value") ?: annotation.findAttributeValue("name")
        return (value as? PsiLiteralExpression)?.value as? String
    }

    /**
     * Matches both real English spellings teams actually use for this
     * header -- "idempotency" (Stripe's own header name, most common)
     * and "idempotence" (the other standard spelling, seen in some
     * in-house API conventions). Substring match, case-insensitive,
     * same as before.
     */
    private fun looksLikeIdempotencyHeaderName(headerName: String?): Boolean {
        if (headerName == null) return false
        return headerName.contains("idempotency", ignoreCase = true) ||
            headerName.contains("idempotence", ignoreCase = true)
    }
}
