package com.justai.jaicf.examples.helloworld.channel

import com.justai.jaicf.activator.caila.caila
import com.justai.jaicf.builder.ScenarioBuilder
import com.justai.jaicf.builder.generic3.generic
import com.justai.jaicf.builder.generic3.intent
import com.justai.jaicf.channel.alexa.alexa
import com.justai.jaicf.channel.telegram.telegram
import com.justai.jaicf.model.scenario.Scenario

object TestScenario : Scenario() {
    fun init(a: ScenarioBuilder.() -> Unit) { this.apply(a)}

    init {
        state("a") {
            generic {
                telegram.caila {
                    alexa {

                    }
                }
                telegram {

                    caila {
                        //

                    }

                    caila {
                        42
                    }
                }

                caila {

                }

                intent {
                    caila {

                    }

                    telegram {

                    }


                }

                telegram {
                }

                alexa {

                }
            }
        }
    }
}