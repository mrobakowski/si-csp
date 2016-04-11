fun sudoku(dim: Int, initialCond: Map<Pair<Int, Int>, Int>): List<Int>? {
    val s = solver<Pair<Int, Int>, Int> {
        val dimSqrt = Math.sqrt(dim.toDouble()).toInt()
        val domain = 1..dim
        for (y in 0..dim - 1) {
            for (x in 0..dim - 1) {
                variables += (x to y) withDomain domain
            }
        }
        initialCond.mapKeysTo(initialBindings) { Variable(it.key) }

        for (y in 0..dim - 1) {
            for (x in 0..dim - 1) {
                // add constraints in square
                val squareX = (x / dimSqrt) * dimSqrt
                val squareY = (y / dimSqrt) * dimSqrt
                for (otherY in (y + 1)..(squareY + dimSqrt - 1)) {
                    for (otherX in squareX..(squareX + dimSqrt - 1)) {
                        if (otherX == x) continue
                        constraints += constraint(x to y, otherX to otherY) { bindA, bindB ->
                            bindA.value != bindB.value
                        }
                    }
                }

                // add constraints in row

                // add constraints in column
            }
        }
    }


    return null
}