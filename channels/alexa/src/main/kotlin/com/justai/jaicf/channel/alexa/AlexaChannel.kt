package com.justai.jaicf.channel.alexa

import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.util.JacksonSerializer
import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequest
import com.justai.jaicf.builder.ActivatorAndChannel
import com.justai.jaicf.builder.ContextTypeAware
import com.justai.jaicf.builder.ContextTypeToken
import com.justai.jaicf.builder.TypeTokenDsl
import com.justai.jaicf.channel.alexa.activator.AlexaIntentActivatorContext
import com.justai.jaicf.channel.http.HttpBotRequest
import com.justai.jaicf.channel.http.HttpBotResponse
import com.justai.jaicf.channel.http.asJsonHttpBotResponse
import com.justai.jaicf.channel.jaicp.JaicpCompatibleBotChannel
import com.justai.jaicf.channel.jaicp.JaicpCompatibleChannelFactory
import com.justai.jaicf.context.ActivatorContext
import com.justai.jaicf.reactions.Reactions

class AlexaChannel(
    override val botApi: BotApi
) : JaicpCompatibleBotChannel {

    private val serializer = JacksonSerializer()
    private val skill = AlexaSkill.create(botApi)

    override fun process(request: HttpBotRequest): HttpBotResponse? {
        val botRequest = serializer.deserialize(request.receiveText(), RequestEnvelope::class.java)
        val botResponse = skill.invoke(botRequest, request)
        return serializer.serialize(botResponse).asJsonHttpBotResponse()
    }

    companion object : JaicpCompatibleChannelFactory {
        override val channelType = "jaicp_alexa"
        override fun create(botApi: BotApi) = AlexaChannel(botApi)
    }
}

interface AlexaActivatorAndChannelType :
    ActivatorAndChannel<AlexaIntentActivatorContext, AlexaBotRequest, AlexaReactions>

//val alexa = object : AlexaActivatorAndChannelType {}

val com.justai.jaicf.builder.generic3.ContextTypeAware<in AlexaIntentActivatorContext, in AlexaBotRequest, in AlexaReactions>.alexa
    get() = com.justai.jaicf.builder.generic3.ContextTypeToken<AlexaIntentActivatorContext, AlexaBotRequest, AlexaReactions>()