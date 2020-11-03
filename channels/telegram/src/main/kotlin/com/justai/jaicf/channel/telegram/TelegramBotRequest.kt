package com.justai.jaicf.channel.telegram

import com.github.kotlintelegrambot.entities.Contact
import com.github.kotlintelegrambot.entities.Location
import com.github.kotlintelegrambot.entities.Message
import com.justai.jaicf.activator.event.EventActivatorContext
import com.justai.jaicf.activator.intent.IntentActivatorContext
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.api.EventBotRequest
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.builder.ScenarioBuilder
import com.justai.jaicf.channel.*
import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.context.BotContext
import com.justai.jaicf.reactions.Reactions

object TelegramChannelType : ChannelType<TelegramBotRequest, TelegramReactions>
val telegram: TelegramChannelType = TelegramChannelType

object IntentActivatorType : ActivatorType<IntentActivatorContext>
val intent = IntentActivatorType

object EventActivatorType : ActivatorType<EventActivatorContext>
val event = IntentActivatorType

val BotRequest.telegram
    get() = this as? TelegramBotRequest

val TelegramBotRequest.location
    get() = this as? TelegramLocationRequest

val TelegramBotRequest.contact
    get() = this as? TelegramContactRequest

interface TelegramBotRequest: BotRequest {
    val message: Message

    val chatId: Long
        get() = message.chat.id
}

data class TelegramTextRequest(
    override val message: Message
): TelegramBotRequest, QueryBotRequest(
    clientId = message.chat.id.toString(),
    input = message.text!!
)

data class TelegramQueryRequest(
    override val message: Message,
    val data: String
): TelegramBotRequest, QueryBotRequest(
    clientId = message.chat.id.toString(),
    input = data
)

data class TelegramLocationRequest(
    override val message: Message,
    val location: Location
): TelegramBotRequest, EventBotRequest(
    clientId = message.chat.id.toString(),
    input = TelegramEvent.LOCATION
)

data class TelegramContactRequest(
    override val message: Message,
    val contact: Contact
): TelegramBotRequest, EventBotRequest(
    clientId = message.chat.id.toString(),
    input = TelegramEvent.CONTACT
)