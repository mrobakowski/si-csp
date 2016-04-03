import java.util.*

data class Variable<L, V>(val label: L, var value: V? = null) {
    override fun equals(other: Any?): Boolean {
        return other != null && other is Variable<*, *> && label == other.label
    }

    override fun hashCode(): Int {
        return Objects.hashCode(label)
    }
}

data class Constraint<L, V>(
        val varA: Variable<L, V>,
        val varB: Variable<L, V>,
        val constraintPred: (Variable<L, V>, Variable<L, V>) -> Boolean
) {
    fun isSatisfied() = constraintPred(varA, varB)
}


class CspSolver<L, V> {
    var variables: Set<Variable<L, V>> = hashSetOf()
    var domain: Set<V> = hashSetOf()
    var constraints: Set<Constraint<L, V>> = hashSetOf()

    private var domainShrinker: CspSolver<L, V>.() -> Unit = {} // TODO: better default?
    fun shrinkDomains(shrinker: CspSolver<L, V>.() -> Unit) {
        domainShrinker = shrinker
    }

    private var variableChooser: (Set<Variable<L, V>>) -> Variable<L, V> = { it.first() }
    fun chooseVariable(chooser: (Set<Variable<L, V>>) -> Variable<L, V>) {
        variableChooser = chooser
    }

    private var valueChooser: (Set<V>) -> V = { it.first() }
    fun chooseValue(chooser: (Set<V>) -> V) {
        valueChooser = chooser
    }

    fun solve(): MutableList<Variable<L, V>>? {
        // TODO: implement

        return null
    }
}

fun <L, V> solver(init: CspSolver<L, V>.() -> Unit) = CspSolver<L, V>().apply(init)