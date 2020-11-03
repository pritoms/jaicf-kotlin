package com.justai.jaicf.channel

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ScenarioBuilder
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions

/**
 * A base interface for every channel that receives a requests to the JAICF bot.
 * Should process a request using a provided [BotApi] implementation.
 * Every channel should create it's own instances of [com.justai.jaicf.api.BotRequest], [com.justai.jaicf.api.BotResponse], [com.justai.jaicf.reactions.Reactions] and pass it to the bot engine process method once a new request was received.
 * There SDK already contains ready to use helper subclasses of this interface that describes a contract for different types of channels.
 *
 * @property botApi a bot engine that should process requests to this channel
 *
 * @see BotApi
 * @see ConsoleChannel
 * @see com.justai.jaicf.api.BotRequest
 * @see com.justai.jaicf.api.BotResponse
 * @see com.justai.jaicf.reactions.Reactions
 * @see com.justai.jaicf.channel.http.HttpBotChannel
 * @see com.justai.jaicf.channel.jaicp.JaicpBotChannel
 */
interface BotChannel {
    val botApi: BotApi
}

inline fun <reified A: ActivatorContext, reified AT: ActivatorType<A>, reified Req: BotRequest, reified React: Reactions, reified CH: ChannelType<Req, React>> ActionContext.generic(activatorType: AT, channelType: CH, noinline l: GenericActionContext<A, Req, React>.() -> Unit): Unit {
    GenericActionContext(context, activator, request, reactions).apply {
        activatorType { channelType { l() } }
    }
}

inline fun <reified A: ActivatorContext,  reified Req: BotRequest, reified React: Reactions, reified ACH: ActivatorAndChannel<A, Req, React>> ActionContext.generic(activatorAndChannel: ACH, noinline l: GenericActionContext<A, Req, React>.() -> Unit): Unit {
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
    action { generic(object : ActivatorType<ActivatorContext> {}, channelType, l) }
}

inline fun <
        reified A: ActivatorContext,
        reified AT: ActivatorType<A>
        > ScenarioBuilder.StateBuilder.genericAction(
    activatorType: AT,
    noinline l: GenericActionContext<A, BotRequest, Reactions>.() -> Unit
) {
    action { generic(activatorType, object : ChannelType<BotRequest, Reactions> {}, l) }
}

fun ScenarioBuilder.StateBuilder.genericAction(
    l: GenericActionContext<ActivatorContext, BotRequest, Reactions>.() -> Unit
) {
    action { generic(object : ActivatorType<ActivatorContext> {}, object : ChannelType<BotRequest, Reactions> {}, l) }
}


//fun ScenarioBuilder.StateBuilder.genericAction(l: GenericActionContext<ActivatorContext, BotRequest, Reactions>.() -> Unit) {
//    action { generic(l) }
//}

class GenericActionContext<A: ActivatorContext, Req: BotRequest, React: Reactions>(
    override val context: BotContext,
    override val activator: A,
    override val request: Req,
    override val reactions: React
) : ActionContext(context, activator, request, reactions) {
    inline operator fun <reified ReqL: BotRequest, reified ReactL: Reactions, R> ChannelType<ReqL, ReactL>.invoke(l: GenericActionContext<A, ReqL, ReactL>.() -> R): R? {
        val req = request as BotRequest
        val react = reactions as Reactions
        return if (req is ReqL && react is ReactL) {
            GenericActionContext(context, activator, req, react).run(l)
        } else {
            null
        }
    }

    inline operator fun <reified AL: ActivatorContext, R> ActivatorType<AL>.invoke(l: GenericActionContext<AL, Req, React>.() -> R): R? {
        val a = activator as ActivatorContext
        return if (a is AL) {
            GenericActionContext(context, a, request, reactions).run(l)
        } else {
            null
        }
    }

    inline operator fun <reified AL: ActivatorContext, reified ReqL: BotRequest, reified ReactL: Reactions, R> ActivatorAndChannel<AL, ReqL, ReactL>.invoke(l: GenericActionContext<AL, ReqL, ReactL>.() -> R): R? {
        val a = activator as ActivatorContext
        val req = request as BotRequest
        val react = reactions as Reactions
        return if (a is AL && req is ReqL && react is ReactL) {
            GenericActionContext(context, a, req, react).run(l)
        } else {
            null
        }
    }

    inline operator fun <reified AL: ActivatorContext, reified Req: BotRequest, reified React: Reactions, R> Pair<ActivatorType<AL>, ChannelType<Req, React>>.invoke(l: GenericActionContext<AL, Req, React>.() -> R): R? {
        val a = activator as ActivatorContext
        val req = request as BotRequest
        val react = reactions as Reactions
        return if (a is AL && req is Req && react is React) {
            GenericActionContext(context, a, req, react).run(l)
        } else {
            null
        }
    }

//    inline operator fun <reified AL: ActivatorContext, reified Req: BotRequest, reified React: Reactions, R> Pair<ActivatorType<AL>, ChannelType<Req, React>>.invoke(l: GenericActionContext<AL, Req, React>.() -> R): R? {
//        val a = activator as ActivatorContext
//        val req = request as BotRequest
//        val react = reactions as Reactions
//        return if (a is AL && req is Req && react is React) {
//            GenericActionContext(context, a, req, react).run(l)
//        } else {
//            null
//        }
//    }
}

inline fun <
        reified A: ActivatorContext,
        reified Req: BotRequest,
        reified React: Reactions,
        R>
        GenericActionContext<*, *, *>.with(l: GenericActionContext<A, Req, React>.() -> R): R?
{
    val a = activator as? A
    val req = request as? Req
    val react = reactions as? React
    return if (a != null && req != null && react != null) {
        GenericActionContext(context, a, req, react).l()
    } else {
        null
    }
}

interface ChannelType<Req: BotRequest, React: Reactions>
interface ActivatorType<A: ActivatorContext>
interface ActivatorAndChannel<A: ActivatorContext, Req: BotRequest, React: Reactions>
