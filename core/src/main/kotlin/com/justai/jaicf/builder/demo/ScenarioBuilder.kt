package com.justai.jaicf.builder.demo

import com.justai.jaicf.context.ActionContext
import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.model.ActionAdapter
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.state.StatePath
import kotlin.reflect.KClass

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
annotation class ScenarioMarker

/**
 * Builds and returns [ScenarioModel].
 */
@ScenarioMarker
fun Scenario(body: ScenarioBuilder.() -> Unit) = ScenarioBuilder().apply(body).build()

open class Scenario : ScenarioBuilder() {
    val model by lazy { build() }
}

/**
 * Builds a [ScenarioModel] that represents a dialog scenario.
 */
@ScenarioMarker
open class ScenarioBuilder {
    private val scenario = ScenarioModelBuilder()
    private val root = StateBuilder(StatePath.root(), scenario = scenario)

    /**
     * The starting point of a scenario building, builds the root state of the scenario.
     * Every conversation starts in the root state,
     *
     * @param body a code block that builds the root state.
     *
     * @see StateBuilder
     */
    @ScenarioMarker
    fun root(body: StateBuilder.() -> Unit) = root.run(body)

    /**
     * Registeres a [BotHook] of the certain type with specified action.
     *
     * @param klass class instance of the concrete [BotHook] implementation.
     * @param action an action that will be executed when corresponding event is occurred.
     *
     * @see BotHook
     * @see com.justai.jaicf.hook.BotHookHandler
     * @see com.justai.jaicf.BotEngine
     */
    fun <T : BotHook> handle(klass: KClass<T>, action: DummyBuilder.(T) -> Unit) {
        scenario.registerHandler(klass) { DummyBuilder.action(it) }
    }

    /**
     * Reified version of a [ScenarioBuilder.handle]
     */
    inline fun <reified T : BotHook> handle(noinline action: DummyBuilder.(T) -> Unit) = handle(T::class, action)

    /**
     * Links given dependencies to the current [ScenarioModel].
     * Means that scenarios specified as dependencies will be accessible from the current model.
     *
     * @param scenarios dependencies this scenario depends on
     */
    fun dependsOn(vararg scenarios: ScenarioModel) {
        scenarios.forEach(scenario::registerDependency)
    }

    internal fun build(): ScenarioModel = scenario.apply { registerState(root.build()) }.build()
}

/**
 * Builds a state of the scenario.
 *
 * Using this builder one can recursively build a state, specifiying its children,
 * and define transitions from or to this state.
 *
 * Building should be done recursively from the root state.
 *
 * @see [ScenarioBuilder.root]
 */
@ScenarioMarker
class StateBuilder(
    private val path: StatePath,
    private val noContext: Boolean = false,
    private val modal: Boolean = false,
    private val scenario: ScenarioModelBuilder,
    parent: StateBuilder? = null
) {
    private var action: ((ActionContext) -> Unit)? = null
    private val parent: StateBuilder = parent ?: this

    /**
     * Registeres transition with specified [ActivationRule]s from this  (currently building) state
     * to [toState]. Means that [toState] becomes available for
     * transition to it from the this state if user's request matches specified rule.
     *
     * @param toState a state that becomes available for transition from this state.
     * @param body a code block that builds a transition rule.
     *
     * @see RuleBuilder
     * @see com.justai.jaicf.activator.Activator
     */
    fun transition(toState: String, body: RuleBuilder.() -> Unit) {
        val rules = RuleBuilder().apply(body).build()
        rules.forEach { scenario.registerTransition(path.toString(), path.resolve(toState).toString(), it) }
    }

    /**
     * Registeres transition with specified [ActivationRule]s from the [fromState]
     * to this (currently building) state. Means that this state becomes available for
     * transition to it from the [fromState] if user's request matches specified rule.
     *
     * If [fromState] is not specified, than parent state will be used.
     * Parent of the root state is root state itself.
     *
     * @param fromState a state this state becomes available from.
     * @param body a code block that builds a transition rule.
     *
     * @see RuleBuilder
     * @see com.justai.jaicf.activator.Activator
     */
    fun activators(fromState: String = parent.path.toString(), body: RuleBuilder.() -> Unit) {
        val rules = RuleBuilder().apply(body).build()
        rules.forEach { scenario.registerTransition(path.resolve(fromState).toString(), path.toString(), it) }
    }

    /**
     * Appends global activators for this state. Means that this state can be activated from any point of scenario.
     */
    fun globalActivators(body: RuleBuilder.() -> Unit) = activators(fromState = "/", body = body)

    /**
     * An action that should be executed once this state was activated.
     * @param body a code block of the action
     */
    fun action(body: @ScenarioMarker ActionContext.() -> Unit) {
        action = body
    }

    /**
     * Appends a child state to the current state.
     * Means that the current state will be used as a parent for this child state during building.
     *
     * @param name a name of the state. Could be plain text or contains slashes to define a state path
     * @param noContext indicates if this state should not to change the current dialogue's context
     * @param modal indicates if this state should process the user's request in modal mode ignoring all other states
     * @param body a code block of the state that contains activators, action and inner states definitions
     *
     * @see StateBuilder.activators
     */
    @ScenarioMarker
    fun state(
        name: String,
        noContext: Boolean = false,
        modal: Boolean = false,
        body: StateBuilder.() -> Unit
    ) {
        val state = StateBuilder(path.resolve(name), noContext, modal, scenario, this).apply(body).build()
        scenario.registerState(state)
    }

    /**
     * Appends a fallback state to the current state.
     * Means that this fallback state will be activated if no one transition
     * can be made from the current state on user's request.
     * The current dialogue's context won't be changed.
     *
     * ```
     * fallback {
     *   reactions.say("Sorry, I didn't get it...")
     * }
     * ```
     *
     * @param action an action block that will be executed
     */
    @ScenarioMarker
    fun fallback(action: @ScenarioMarker ActionContext.() -> Unit) = state("fallback", noContext = true) {
        activators { catchAll() }
        action(action)
    }

    internal fun build(): State = State(path, noContext, modal, action?.let(::ActionAdapter))
}

/**
 * Builds a rule for transition from one state to another.
 *
 * @see StateBuilder.transition
 * @see StateBuilder.activators
 */
@ScenarioMarker
class RuleBuilder {
    private val rules = mutableListOf<ActivationRule>()

    /**
     * Appends given [rule] to this builder.
     */
    fun rule(rule: ActivationRule) {
        rules += rule
    }

    internal fun build(): List<ActivationRule> = rules
}

/**
 * Is used for disabling outer scope in lambdas without receiver (e.g. [ScenarioBuilder.handle])
 */
@ScenarioMarker
object DummyBuilder







val A = Scenario {
    root {
        state("abc") {

        }

        state("asdsa") {

        }
    }
}