package com.justai.jaicf.builder.demo

import com.justai.jaicf.activator.catchall.CatchAllActivationRule
import com.justai.jaicf.activator.event.AnyEventActivationRule
import com.justai.jaicf.activator.event.EventByNameActivationRule
import com.justai.jaicf.activator.intent.AnyIntentActivationRule
import com.justai.jaicf.activator.intent.IntentByNameActivationRule
import com.justai.jaicf.activator.regex.RegexActivationRule

/**
 * Appends catch-all activator to this state. Means that any text can activate this state.
 * Requires a [com.justai.jaicf.activator.catchall.CatchAllActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
 *
 * @see com.justai.jaicf.activator.catchall.CatchAllActivator
 * @see com.justai.jaicf.api.BotApi
 */
fun RuleBuilder.catchAll() = rule(CatchAllActivationRule())

/**
 * Appends regex activator to this state. Means that any text that matches to the pattern can activate this state.
 * Requires a [com.justai.jaicf.activator.regex.RegexActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
 *
 * @see com.justai.jaicf.activator.regex.RegexActivator
 * @see com.justai.jaicf.api.BotApi
 */
fun RuleBuilder.regex(pattern: Regex) = rule(RegexActivationRule(pattern.pattern))

/**
 * Appends regex activator to this state. Means that any text that matches to the pattern can activate this state.
 * Requires a [com.justai.jaicf.activator.regex.RegexActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
 *
 * @see com.justai.jaicf.activator.regex.RegexActivator
 * @see com.justai.jaicf.api.BotApi
 */
fun RuleBuilder.regex(pattern: String) = regex(pattern.toRegex())

/**
 * Appends event activator to this state. Means that an event with such name can activate this state.
 * Requires a [com.justai.jaicf.activator.event.EventActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
 *
 * @see com.justai.jaicf.activator.event.EventActivator
 * @see com.justai.jaicf.api.BotApi
 */
fun RuleBuilder.event(event: String) = rule(EventByNameActivationRule(event))

/**
 * Appends any-event activator to this state. Means that any intent can activate this state.
 * Requires a [com.justai.jaicf.activator.event.EventActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
 *
 * @see com.justai.jaicf.activator.event.EventActivator
 * @see com.justai.jaicf.api.BotApi
 */
fun RuleBuilder.anyEvent() = rule(AnyEventActivationRule())

/**
 * Appends intent activator to this state. Means that an intent with such name can activate this state.
 * Requires a [com.justai.jaicf.activator.intent.IntentActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
 *
 * @see com.justai.jaicf.activator.intent.IntentActivator
 * @see com.justai.jaicf.api.BotApi
 */
fun RuleBuilder.intent(intent: String) = rule(IntentByNameActivationRule(intent))

/**
 * Appends any-intent activator to this state. Means that any intent can activate this state.
 * Requires a [com.justai.jaicf.activator.intent.IntentActivator] in the activators' list of your [com.justai.jaicf.api.BotApi] instance.
 *
 * @see com.justai.jaicf.activator.intent.IntentActivator
 * @see com.justai.jaicf.api.BotApi
 */
fun RuleBuilder.anyIntent() = rule(AnyIntentActivationRule())