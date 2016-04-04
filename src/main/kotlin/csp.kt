data class Variable<L : Any>(val label: L)
data class Binding<L : Any, V : Any>(val variable: Variable<L>, val value: V)
data class Constraint<L : Any, V : Any>(
        val varA: Variable<L>,
        val varB: Variable<L>,
        val constraintPred: (Binding<L, V>, Binding<L, V>) -> Boolean
) {
    fun isSatisfied(bindings: Map<Variable<L>, Binding<L, V>>): Boolean {
        return constraintPred(bindings[varA] ?: return true, bindings[varB] ?: return true)
    }
}

class CspSolver<L : Any, V : Any> {
    var variables: Set<Variable<L>> = hashSetOf()
    var domain: Set<V> = hashSetOf()
    var constraints: Set<Constraint<L, V>> = hashSetOf()

    private var _initialBindings: MutableMap<Variable<L>, Binding<L, V>> = hashMapOf()
    var initialBindings: Set<Binding<L, V>>
        get() = _initialBindings.values.toSet()
        set(value) {  value.map { it.variable to it }.toMap(_initialBindings) }

    private val _constraintMap: MutableMap<Variable<L>, Set<Constraint<L, V>>> = hashMapOf()
    interface ConstraintMap<L : Any, V : Any> {
        operator fun get(v: Variable<L>): Set<Constraint<L, V>>
    }
    val constraintMap = object : ConstraintMap<L, V> {
        override fun get(v: Variable<L>): Set<Constraint<L, V>> = if (v in _constraintMap) {
            _constraintMap[v]!!
        } else {
            _constraintMap[v] = constraints.filter { it.varA == v || it.varB == v }.toSet()
            _constraintMap[v]!!
        }
    }

    private var domainShrinker: CspSolver<L, V>.(Binding<L, V>, Map<Variable<L>, Set<V>>) -> Map<Variable<L>, Set<V>> =
            { binding, domains -> domains.filterKeys { it != binding.variable } }

    fun shrinkDomains(shrinker: CspSolver<L, V>.(Binding<L, V>, Map<Variable<L>, Set<V>>) -> Map<Variable<L>, Set<V>>) {
        domainShrinker = shrinker
    }

    private var variableChooser: CspSolver<L, V>.(Map<Variable<L>, Binding<L, V>>, Set<Variable<L>>) -> Variable<L> =
            { bound, unbound -> unbound.first() }

    fun chooseVariable(chooser: CspSolver<L, V>.(Map<Variable<L>, Binding<L, V>>, Set<Variable<L>>) -> Variable<L>) {
        variableChooser = chooser
    }

    private var valueChooser: CspSolver<L, V>.(Map<Variable<L>, Binding<L, V>>, Variable<L>, Set<V>) -> V =
            { existingBindings, variable, domain -> domain.first() }

    fun chooseValue(chooser: CspSolver<L, V>.(Map<Variable<L>, Binding<L, V>>, Variable<L>, Set<V>) -> V) {
        valueChooser = chooser
    }

    fun solve(): Set<Binding<L, V>>? {
        val unboundVariables = variables.filter { it !in _initialBindings }.toSet()
        return solve(_initialBindings, unboundVariables, unboundVariables.map {it to domain}.toMap())
    }

    private fun solve(
            bindings: Map<Variable<L>, Binding<L, V>>,
            unboundVariables: Set<Variable<L>>,
            domains: Map<Variable<L>, Set<V>>): Set<Binding<L, V>>? {
        val variableToBind = this.variableChooser(bindings, unboundVariables)
        val domain = domains[variableToBind].let { if (it == null || it.isEmpty()) return null; it.toMutableSet() }
        val unboundVariablesWithoutCurrent = unboundVariables - variableToBind
        while (domain.isNotEmpty()) {
            val value = this.valueChooser(bindings, variableToBind, domain)
            domain -= value
            val binding = Binding(variableToBind, value)
            val bindingsWithCurrent = bindings + (variableToBind to binding)
            if (constraintMap[variableToBind].all { it.isSatisfied(bindingsWithCurrent) }) {
                val shrunkDomains = this.domainShrinker(binding, domains)
                val solution = solve(bindingsWithCurrent, unboundVariablesWithoutCurrent, shrunkDomains)
                if (solution != null) return solution
            }
        }
        return null
    }
}

fun <L : Any, V : Any> solver(init: CspSolver<L, V>.() -> Unit) = CspSolver<L, V>().apply(init)
