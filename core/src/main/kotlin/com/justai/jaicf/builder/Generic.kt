package com.justai.jaicf.builder

import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions
import kotlin.reflect.KClass

inline fun <reified A: ActivatorContext, reified AT: ActivatorType<A>, reified Req: BotRequest, reified React: Reactions, reified CH: ChannelType<Req, React>> ActionContext.generic(
    activatorType: AT,
    channelType: CH,
    noinline l: GenericActionContext<A, Req, React>.() -> Unit
): Unit {
    GenericActionContext(context, activator, request, reactions).apply {
        activatorType { channelType { l() } }
    }
}

inline fun <reified A: ActivatorContext,  reified Req: BotRequest, reified React: Reactions, reified ACH: ActivatorAndChannel<A, Req, React>> ActionContext.generic(
    activatorAndChannel: ACH,
    noinline l: GenericActionContext<A, Req, React>.() -> Unit
): Unit {
    GenericActionContext(context, activator, request, reactions).apply {
        activatorAndChannel { l() }
    }
}

inline fun <
        reified A: ActivatorContext,
        reified AT: ActivatorType<A>,
        reified Req: BotRequest,
        reified React: Reactions,
        reified CH: ChannelType<Req, React>
        > ScenarioBuilder.StateBuilder.genericAction(
    activatorType: AT,
    channelType: CH,
    noinline l: GenericActionContext<A, Req, React>.() -> Unit
) {
    action { generic(activatorType, channelType, l) }
}

inline fun <
        reified A: ActivatorContext,
        reified Req: BotRequest,
        reified React: Reactions,
        reified ACH: ActivatorAndChannel<A, Req, React>
        > ScenarioBuilder.StateBuilder.genericAction(
    activatorAndChannel: ACH,
    noinline l: GenericActionContext<A, Req, React>.() -> Unit
) {
    action { generic(activatorAndChannel, l) }
}

inline fun <
        reified Req: BotRequest,
        reified React: Reactions,
        reified CH: ChannelType<Req, React>
        > ScenarioBuilder.StateBuilder.genericAction(
    channelType: CH,
    noinline l: GenericActionContext<ActivatorContext, Req, React>.() -> Unit
) {
    action { generic(object :
        ActivatorType<ActivatorContext> {}, channelType, l) }
}

inline fun <
        reified A: ActivatorContext,
        reified AT: ActivatorType<A>
        > ScenarioBuilder.StateBuilder.genericAction(
    activatorType: AT,
    noinline l: GenericActionContext<A, BotRequest, Reactions>.() -> Unit
) {
    action { generic(activatorType, object :
        ChannelType<BotRequest, Reactions> {}, l) }
}

fun ScenarioBuilder.StateBuilder.genericAction(
    l: GenericActionContext<ActivatorContext, BotRequest, Reactions>.() -> Unit
) {
    action { generic(object :
        ActivatorType<ActivatorContext> {}, object :
        ChannelType<BotRequest, Reactions> {}, l) }
}

class GenericActionContext<A: ActivatorContext, Req: BotRequest, React: Reactions>(
    override val context: BotContext,
    override val activator: A,
    override val request: Req,
    override val reactions: React
) : ActionContext(context, activator, request, reactions) {
    inline operator fun <reified Req: BotRequest, reified React: Reactions, R> ChannelType<Req, React>.invoke(l: GenericActionContext<A, Req, React>.() -> R): R? {
        val req = request as BotRequest
        val react = reactions as Reactions
        return if (req is Req && react is React) {
            GenericActionContext(context, activator, req, react).run(l)
        } else {
            null
        }
    }

    inline operator fun <reified A: ActivatorContext, R> ActivatorType<A>.invoke(l: GenericActionContext<A, Req, React>.() -> R): R? {
        val a = activator as ActivatorContext
        return if (a is A) {
            GenericActionContext(context, a, request, reactions).run(l)
        } else {
            null
        }
    }

    inline operator fun <reified A: ActivatorContext, reified Req: BotRequest, reified React: Reactions, R> ActivatorAndChannel<A, Req, React>.invoke(l: GenericActionContext<A, Req, React>.() -> R): R? {
        val a = activator as ActivatorContext
        val req = request as BotRequest
        val react = reactions as Reactions
        return if (a is A && req is Req && react is React) {
            GenericActionContext(context, a, req, react).run(l)
        } else {
            null
        }
    }

    inline operator fun <reified A: ActivatorContext, reified Req: BotRequest, reified React: Reactions, R> Pair<ActivatorType<A>, ChannelType<Req, React>>.invoke(l: GenericActionContext<A, Req, React>.() -> R): R? {
        val a = activator as ActivatorContext
        val req = request as BotRequest
        val react = reactions as Reactions
        return if (a is A && req is Req && react is React) {
            GenericActionContext(context, a, req, react).run(l)
        } else {
            null
        }
    }
}



interface ChannelType<Req: BotRequest, React: Reactions>
interface ActivatorType<A: ActivatorContext>
interface ActivatorAndChannel<A: ActivatorContext, Req: BotRequest, React: Reactions>