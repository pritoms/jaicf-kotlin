package com.justai.jaicf.builder.generic3

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ScenarioBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@DslMarker
annotation class TypeTokenDsl

interface ContextTypeAware<A: ActivatorContext, B: BotRequest, R: Reactions>

class ContextTypeToken<A: ActivatorContext, B: BotRequest, R: Reactions> : ContextTypeAware<A, B, R>

//@TypeTokenDsl
class GenericActionContext<A: ActivatorContext, B: BotRequest, R: Reactions>(
    override val context: BotContext,
    override val activator: A,
    override val request: B,
    override val reactions: R
): ActionContext(context, activator, request, reactions), ContextTypeAware<A, B, R> {
    inline operator fun <reified AC: A, reified BC: B, reified RC: R, T> ContextTypeToken<AC, BC, RC>.invoke(action: GenericActionContext<AC, BC, RC>.() -> T): T? {
        val activator = this@GenericActionContext.activator as? AC ?: return null
        val request = this@GenericActionContext.request as? BC ?: return null
        val reactions = this@GenericActionContext.reactions as? RC ?: return null
        return GenericActionContext(this@GenericActionContext.context, activator, request, reactions).run(action)
    }
}

inline fun ScenarioBuilder.StateBuilder.generic(crossinline action: GenericActionContext<ActivatorContext, BotRequest, Reactions>.() -> Unit) {
    action { GenericActionContext(context, activator, request, reactions).run(action) }
}

val <B: BotRequest, R: Reactions> ContextTypeAware<in IntentActivatorContext, B, R>.intent
    get() = ContextTypeToken<IntentActivatorContext, B, R>()
