data class Variable<L : Any>(val label: L)

data class Binding<L : Any, V : Any>(val variable: Variable<L>, val value: V) {
    override fun toString() = "`${variable.label}` = $value"

    constructor(kvp: Map.Entry<Variable<L>, V>) : this(kvp.key, kvp.value)
}

val <L : Any, V : Any> Map<Variable<L>, V>.bindings: Iterable<Binding<L, V>> get() = this.map { Binding(it.key, it.value) }
fun <L : Any, V : Any> Map<Variable<L>, V>.binding(v: Variable<L>): Binding<L, V>? {
    return Binding(v, this[v] ?: return null)
}

data class Constraint<L : Any, V : Any>(
        val varA: Variable<L>,
        val varB: Variable<L>,
        val constraintPred: (Binding<L, V>, Binding<L, V>) -> Boolean
) {
    fun isSatisfied(bindings: Map<Variable<L>, V>): Boolean {
        return constraintPred(Binding(varA, bindings[varA] ?: return true), Binding(varB, bindings[varB] ?: return true))
    }
}

data class Context<L : Any, V : Any>(val domains: Map<Variable<L>, Set<V>>, val bindings: Map<Variable<L>, V>, val constraints: CspSolver.ConstraintMap<L, V>)

class CspSolver<L : Any, V : Any> {
    val variables: MutableMap<Variable<L>, Set<V>> = mutableMapOf() // map of variable to domain
    val constraints: MutableSet<Constraint<L, V>> = hashSetOf()
    val initialBindings: MutableMap<Variable<L>, V> = hashMapOf()

    private val _constraintMap: MutableMap<Variable<L>, Set<Constraint<L, V>>> = hashMapOf()

    interface ConstraintMap<L : Any, V : Any> {
        operator fun get(v: Variable<L>): Set<Constraint<L, V>>
    }

    private val constraintMap = object : ConstraintMap<L, V> {
        override fun get(v: Variable<L>): Set<Constraint<L, V>> = if (v in _constraintMap) {
            _constraintMap[v]!!
        } else {
            _constraintMap[v] = constraints.filter { it.varA == v || it.varB == v }.toSet()
            _constraintMap[v]!!
        }
    }

    private var domainShrinker: Context<L, V>.(Binding<L, V>) -> Map<Variable<L>, Set<V>> =
            { binding -> domains.filterKeys { it != binding.variable } }

    fun shrinkDomains(shrinker: Context<L, V>.(Binding<L, V>) -> Map<Variable<L>, Set<V>>) {
        domainShrinker = shrinker
    }

    private var variableChooser: Context<L, V>.(Set<Variable<L>>) -> Variable<L> =
            { it.first() }

    fun chooseVariable(chooser: Context<L, V>.(Set<Variable<L>>) -> Variable<L>) {
        variableChooser = chooser
    }

    private var valueChooser: Context<L, V>.(Variable<L>, Set<V>) -> V =
            { variable, domain -> domain.first() }

    fun chooseValue(chooser: Context<L, V>.(Variable<L>, Set<V>) -> V) {
        valueChooser = chooser
    }

    fun solve(): Set<Binding<L, V>>? {
        var unboundVariableToDomainMap = variables.filterKeys { it !in initialBindings }

        // initial domains shrinkage
        for (b in initialBindings.bindings) {
            unboundVariableToDomainMap = domainShrinker(
                    Context(unboundVariableToDomainMap, initialBindings, constraintMap),
                    b
            )
        }

        return solve(initialBindings, unboundVariableToDomainMap.keys, unboundVariableToDomainMap)
    }

    private fun solve(
            bindings: Map<Variable<L>, V>,
            unboundVariables: Set<Variable<L>>,
            domains: Map<Variable<L>, Set<V>>
    ): Set<Binding<L, V>>? {
        val variableToBind = variableChooser(Context(domains, bindings, constraintMap), unboundVariables)
        val domain = domains[variableToBind].let { if (it == null || it.isEmpty()) return null; it.toMutableSet() }
        val unboundVariablesWithoutCurrent = unboundVariables - variableToBind
        while (domain.isNotEmpty()) {
            val value = valueChooser(Context(domains, bindings, constraintMap), variableToBind, domain)
            domain -= value
            val bindingsWithCurrent = bindings + (variableToBind to value)
            if (constraintMap[variableToBind].all { it.isSatisfied(bindingsWithCurrent) }) {
                if (unboundVariablesWithoutCurrent.isEmpty())
                    return bindingsWithCurrent.entries.map { Binding(it) }.toSet()
                val shrunkDomains = domainShrinker(
                        Context(domains.filterKeys { it != variableToBind }, bindingsWithCurrent, constraintMap),
                        variableToBind bind value
                )
                val solution = solve(bindingsWithCurrent, unboundVariablesWithoutCurrent, shrunkDomains)
                if (solution != null) return solution
            }
        }
        return null
    }
}

fun <L : Any, V : Any> solver(init: CspSolver<L, V>.() -> Unit) = CspSolver<L, V>().apply(init)
infix fun <L : Any, V : Any> L.withDomain(domain: Iterable<V>) = Variable(this) to domain.toSet()
infix fun <L : Any, V : Any> Variable<L>.bind(value: V) = Binding(this, value)
fun <L : Any, V : Any> constraint(varA: L, varB: L, constraintPred: (Binding<L, V>, Binding<L, V>) -> Boolean) =
        Constraint(Variable(varA), Variable(varB), constraintPred)
