package com.justai.jaicf.builder

import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

open class ContextTypeToken<A : ActivatorContext, B : BotRequest, R : Reactions>(
    val activatorType: KClass<out A>,
    val requestType: KClass<out B>,
    val reactionsType: KClass<out R>
) {

    inline fun <reified AC : A> withActivator(): ContextTypeToken<AC, B, R> =
        ContextTypeToken(AC::class, requestType, reactionsType)

    inline fun <reified BC : B> withRequest(): ContextTypeToken<A, BC, R> =
        ContextTypeToken(activatorType, BC::class, reactionsType)

    inline fun <reified RC : R> withReactions(): ContextTypeToken<A, B, RC> =
        ContextTypeToken(activatorType, requestType, RC::class)

    companion object {
        val DEFAULT = contextTypeToken<ActivatorContext, BotRequest, Reactions>()
    }
}

interface ContextTypeAware<A : ActivatorContext, B : BotRequest, R : Reactions> {
    val contextTypeToken: ContextTypeToken<A, B, R>
}

inline fun <reified A : ActivatorContext, reified B : BotRequest, reified R : Reactions> contextTypeToken(): ContextTypeToken<A, B, R> =
    ContextTypeToken(A::class, B::class, R::class)

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@DslMarker
annotation class TypeTokenDsl

class GenericActionContext2<A : ActivatorContext, B : BotRequest, R : Reactions>(
    override val context: BotContext,
    override val activator: A,
    override val request: B,
    override val reactions: R,
    override val contextTypeToken: ContextTypeToken<A, B, R>
) : ActionContext(context, activator, request, reactions), ContextTypeAware<A, B, R> {

    operator fun <A1: A, B1: B, R1: R, T> ContextTypeToken<A1, B1, R1>.invoke(action: GenericActionContext2<A1, B1, R1>.() -> T): T? {
        val activator = activatorType.safeCast(this@GenericActionContext2.activator) ?: return null
        val request = requestType.safeCast(this@GenericActionContext2.request) ?: return null
        val reactions = reactionsType.safeCast(this@GenericActionContext2.reactions) ?: return null
        return GenericActionContext2(this@GenericActionContext2.context, activator, request, reactions, this).run(action)
    }

}

fun ScenarioBuilder.StateBuilder.generic(action: GenericActionContext2<ActivatorContext, BotRequest, Reactions>.() -> Unit) {
    action {
        GenericActionContext2(context, activator, request, reactions, ContextTypeToken.DEFAULT).run(action)
    }
}

fun <A: ActivatorContext, B: BotRequest, R: Reactions> ScenarioBuilder.StateBuilder.generic(
    contextTypeToken: GenericActionContext2<ActivatorContext, BotRequest, Reactions>.() -> ContextTypeToken<A, B, R>,
    action: GenericActionContext2<A,  B,  R>.() -> Unit
) {
    generic {
        val typeToken = contextTypeToken()
        typeToken(action)
    }
}


