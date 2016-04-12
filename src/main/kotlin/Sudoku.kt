import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

fun sudoku(
        dim: Int,
        initialCond: Map<Pair<Int, Int>, Int>,
        forwardCheck: Boolean = true,
        useVariableChoiceHeuristic: Boolean = true,
        depth: Depth = Depth(0)
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

    val solution = s.solve(depth)?.map { it.variable.label to it.value }?.toMap() ?: return null
    val res = arrayListOf<Int>()
    for (y in 0..dim - 1) {
        for (x in 0..dim - 1) {
            res += solution[x to y]!!
        }
    }

    return res
}

fun loadSudoku(path: String): Pair<Int, Map<Pair<Int, Int>, Int>> {
    val lines = Files.readAllLines(Paths.get(path))
    val dim = lines[0].toInt().let { it * it }
    val res = mutableMapOf<Pair<Int, Int>, Int>()
    for (y in 0..dim - 1) {
        val line = lines[y + 1]
        val cells = line.split(" ")
        for (x in 0..dim - 1) {
            val value = cells[x].toInt()
            if (value != 0) res += (x to y) to value
        }
    }
    return dim to res
}