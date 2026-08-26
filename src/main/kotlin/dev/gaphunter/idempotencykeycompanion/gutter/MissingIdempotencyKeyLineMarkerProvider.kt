package dev.gaphunter.idempotencykeycompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.idempotencykeycompanion.detect.JavaEndpointFinder
import dev.gaphunter.idempotencykeycompanion.detect.KotlinEndpointFinder
import dev.gaphunter.idempotencykeycompanion.model.MissingIdempotencyKeyHit
import dev.gaphunter.idempotencykeycompanion.review.ReviewPrompt

/**
 * Warning icon on the method name of any Spring MVC POST/PUT endpoint
 * with no `@RequestHeader` parameter that looks like an idempotency
 * key -- both finders already hand back a real name-identifier leaf
 * (`PsiMethod.nameIdentifier`/`KtNamedFunction.nameIdentifier`), so no
 * extra leaf-descent is needed here (`SDK_GOTCHAS.md` §20).
 */
class MissingIdempotencyKeyLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "Missing idempotency key"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaEndpointFinder.findAll(file)
            "kotlin" -> KotlinEndpointFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val hitsByElement = hits.associateBy { it.methodNameElement }
        for (element in elements) {
            val hit = hitsByElement[element] ?: continue
            result.add(buildMarker(hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(hit: MissingIdempotencyKeyHit): LineMarkerInfo<PsiElement> {
        val tooltip = "This ${hit.kind} endpoint has no @RequestHeader parameter for an idempotency key -- retries/duplicate submits aren't protected against"
        return LineMarkerInfo(
            hit.methodNameElement,
            hit.methodNameElement.textRange,
            IdempotencyKeyIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}
