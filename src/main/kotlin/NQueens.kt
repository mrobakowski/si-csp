fun nQueens(n: Int) {
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
    }
}