import java.util.*

data class Variable<L, V>(val label: L, var value: V? = null, val domain: MutableSet<V>)

class CspSolver<L, V> {
    var numVariables = 0
    var variables: MutableList<Variable<L, V>> = ArrayList()

    private var domainShrinker: CspSolver<L, V>.() -> Unit = {}
    fun shrinkDomains(shrinker: CspSolver<L, V>.() -> Unit) {
        domainShrinker = shrinker
    }

    private var variableChooser: (List<Variable<L, V>>) -> Variable<L, V> = { it.first() }
    fun chooseVariable(chooser: (List<Variable<L, V>>) -> Variable<L, V>) {
        variableChooser = chooser
    }

    private var valueChooser: (List<V>) -> V = { it.first() }
    fun chooseValue(chooser: (List<V>) -> V) {
        valueChooser = chooser
    }

    fun solve(): MutableList<Variable<L, V>>? {


        return null
    }
}

fun <L, V> solver(init: CspSolver<L, V>.() -> Unit) = CspSolver<L, V>().apply(init)