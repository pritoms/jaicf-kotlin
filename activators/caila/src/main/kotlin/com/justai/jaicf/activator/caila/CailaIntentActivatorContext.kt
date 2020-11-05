package com.justai.jaicf.activator.caila

import com.justai.jaicf.activator.caila.dto.CailaAnalyzeResponseData
import com.justai.jaicf.activator.caila.dto.CailaInferenceResultData
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ActivatorType
import com.justai.jaicf.builder.ContextTypeAware
import com.justai.jaicf.builder.ContextTypeToken
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions

data class CailaIntentActivatorContext(
    val result: CailaAnalyzeResponseData,
    val intentData: CailaInferenceResultData
) : IntentActivatorContext(
    intent = intentData.intent.name,
    confidence = intentData.confidence.toFloat()
), java.io.Serializable {

    val topIntent = intentData.intent

    var slots = intentData.slots?.map { it.name to it.value }?.toMap() ?: emptyMap()

    val entities = result.entitiesLookup.entities
}

val ActivatorContext.caila
    get() = this as? CailaIntentActivatorContext

object CailaActivatorType : ActivatorType<CailaIntentActivatorContext>
//val caila = CailaActivatorType

val <B: BotRequest, R: Reactions> com.justai.jaicf.builder.generic3.ContextTypeAware<in CailaIntentActivatorContext, B, R>.caila
    get() = com.justai.jaicf.builder.generic3.ContextTypeToken<CailaIntentActivatorContext, B, R>()