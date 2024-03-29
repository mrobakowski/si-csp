import java.util.*

fun nQueens(
        n: Int,
        forwardCheck: Boolean = true,
        printToStd: Boolean = true,
        useVariableChoiceHeuristic: Boolean = true,
        depth: Depth = Depth(0)
): List<Pair<Int, Int>>? {
    val s = solver<Int, Int> { // queen's x coord as variable label and y coord as its value
        val domain = 1..n
        for (queen in 1..n) {
            variables += queen withDomain domain
        }

        for (queenA in 1..n) {
            if (queenA != n) for (queenB in (queenA + 1)..n) {
                constraints += constraint(queenA, queenB) { queenBindingA, queenBindingB ->
                    val xA = queenBindingA.variable.label
                    val yA = queenBindingA.value
                    val xB = queenBindingB.variable.label
                    val yB = queenBindingB.value

                    if (xA == xB || yA == yB || (xA + yA) == (xB + yB) || (xA - yA) == (xB - yB))
                        false
                    else
                        true
                }
            }
        }

        if (forwardCheck) {
            shrinkDomains {
                val (variable, value) = it
                val queenX = variable.label
                val queenY = value

                val shrunkDomains = HashMap(domains)

                for (otherQueen in it.affectedVariables) {
                    val otherQueenX = otherQueen.label
                    val otherQueenDomain = HashSet(domains[otherQueen] ?: continue)

                    otherQueenDomain -= queenY
                    otherQueenDomain -= queenX + queenY - otherQueenX
                    otherQueenDomain -= queenY - queenX + otherQueenX

                    shrunkDomains[otherQueen] = otherQueenDomain
                }

                shrunkDomains
            }
        }

        chooseValue { variable, domain -> domain.min()!! }
        if (useVariableChoiceHeuristic) {
            chooseVariable { it.map { it to domains[it]?.size }.minBy { it.second ?: Int.MAX_VALUE }?.first!! }
        }
    }

    val solution = s.solve(depth)?.map { Pair(it.variable.label, it.value) }

    if (solution == null) {
        if (printToStd) println("No solutions for n=$n")
        return null
    }

    if (printToStd) {
        println(depth)
        for (i in 1..4 * n + 1) print('-')
        println()
        for (y in 1..n) {
            for (x in 1..n) {
                if (Pair(x, y) in solution) print("| Q ")
                else print("|   ")
            }
            println('|')
            for (i in 1..4 * n + 1) print('-')
            println()
        }
    }
    return solution
}