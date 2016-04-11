import java.util.*

fun sudoku(
        dim: Int,
        initialCond: Map<Pair<Int, Int>, Int>,
        forwardCheck: Boolean = true,
        useVariableChoiceHeuristic: Boolean = true
): List<Int>? {
    val s = solver<Pair<Int, Int>, Int> {
        val dimSqrt = Math.sqrt(dim.toDouble()).toInt()

        // variables
        val domain = 1..dim
        for (y in 0..dim - 1) {
            for (x in 0..dim - 1) {
                variables += (x to y) withDomain domain
            }
        }

        // initial bindings
        initialCond.mapKeysTo(initialBindings) { Variable(it.key) }

        // constraints
        for (y in 0..dim - 1) {
            for (x in 0..dim - 1) {
                // add constraints in square
                val minXIndexInSquare = (x / dimSqrt) * dimSqrt
                val maxXIndexInSquare = minXIndexInSquare + dimSqrt - 1
                val minYIndexInSquare = (y / dimSqrt) * dimSqrt
                val maxYIndexInSquare = minYIndexInSquare + dimSqrt - 1
                if (y < maxYIndexInSquare) for (otherY in (y + 1)..maxYIndexInSquare) {
                    for (otherX in minXIndexInSquare..maxXIndexInSquare) {
                        if (otherX == x) continue
                        constraints += constraint(x to y, otherX to otherY) { bindA, bindB ->
                            bindA.value != bindB.value
                        }
                    }
                }

                // add constraints in row
                if (x < dim - 1) for (otherX in (x + 1)..(dim - 1)) {
                    constraints += constraint(x to y, otherX to y) { bindA, bindB ->
                        bindA.value != bindB.value
                    }
                }

                // add constraints in column
                if (y < dim - 1) for (otherY in (y + 1)..(dim - 1)) {
                    constraints += constraint(x to y, x to otherY) { bindA, bindB ->
                        bindA.value != bindB.value
                    }
                }
            }
        }

        if (forwardCheck) {
            shrinkDomains {
                val shrunkDomains = HashMap(domains)

                for (variable in it.affectedVariables) {
                    val varDomain = HashSet(domains[variable] ?: continue)
                    varDomain -= it.value
                    shrunkDomains[variable] = varDomain
                }

                shrunkDomains
            }
        }

        if (useVariableChoiceHeuristic) {
            chooseVariable { it.map { it to domains[it]?.size }.minBy { it.second ?: Int.MAX_VALUE }?.first!! }
//            chooseVariable { it.minBy {  } }
        }

        chooseValue { variable, domain -> domain.min()!! }
    }

    val solution = s.solve()?.map { it.variable.label to it.value }?.toMap() ?: return null
    val res = arrayListOf<Int>()
    for (y in 0..dim - 1) {
        for (x in 0..dim - 1) {
            res += solution[x to y]!!
        }
    }

    return res
}