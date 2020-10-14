package com.justai.jaicf.builder.demo

import com.justai.jaicf.hook.BotHook
import com.justai.jaicf.hook.BotHookAction
import com.justai.jaicf.model.activation.ActivationRule
import com.justai.jaicf.model.scenario.ScenarioModel
import com.justai.jaicf.model.state.State
import com.justai.jaicf.model.transition.Transition
import kotlin.reflect.KClass

class ScenarioModelBuilder {
    private val handlers = mutableMapOf<KClass<out BotHook>, MutableList<BotHookAction<in BotHook>>>()
    private val states = mutableMapOf<String, State>()
    private val transitions = mutableListOf<Transition>()
    private val dependencies = mutableListOf<ScenarioModel>()

    fun <T : BotHook> registerHandler(klass: KClass<T>, action: (T) -> Unit) {
        handlers.getOrDefault(klass, mutableListOf()).add(action as BotHookAction<in BotHook>)
    }

    fun registerState(state: State) {
        val registered = states.putIfAbsent(state.path.toString(), state)
        require(registered == null) { "Duplicated declaration of state with path: ${state.path}" }
    }

    fun registerTransition(transition: Transition) {
        transitions += transition
    }

    fun registerDependency(other: ScenarioModel) {
        dependencies += other
    }

    fun build(): ScenarioModel {
        val model = ScenarioModel().also {
            it.states += states
            it.transitions += transitions
            it.hooks += handlers
        }

        return dependencies.fold(model, ScenarioModel::plus)
    }
}

fun ScenarioModelBuilder.registerTransition(fromState: String, toState: String, rule: ActivationRule) {
    registerTransition(Transition(fromState, toState, rule))
}
