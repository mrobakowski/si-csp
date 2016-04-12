import org.junit.Test
import java.util.*
import kotlin.system.measureTimeMillis

enum class Color { red, green, blue }
class Test {
    @Test fun testSolver() {
        val s = solver<String, Color> {
            val colors = setOf(Color.red, Color.green, Color.blue)

            val WA = "Western Australia"
            val NT = "Northern Territory"
            val SA = "Southern Australia"
            val Q = "Queensland"
            val NSW = "New South Wales"
            val V = "Victoria"
            val T = "Tasmania"

            variables += setOf(
                    WA  withDomain colors,
                    NT  withDomain colors,
                    SA  withDomain colors,
                    Q   withDomain colors,
                    NSW withDomain colors,
                    V   withDomain colors,
                    T   withDomain colors
            )

            val colorsNotEqual = { bindingA: Binding<String, Color>, bindingB: Binding<String, Color> ->
                bindingA.value != bindingB.value
            }

            constraints += setOf(
                    constraint(WA, NT, colorsNotEqual),
                    constraint(WA, SA, colorsNotEqual),
                    constraint(NT, SA, colorsNotEqual),
                    constraint(NT, Q, colorsNotEqual),
                    constraint(SA, Q, colorsNotEqual),
                    constraint(SA, NSW, colorsNotEqual),
                    constraint(SA, V, colorsNotEqual),
                    constraint(Q, NSW, colorsNotEqual),
                    constraint(NSW, V, colorsNotEqual),
                    constraint(V, T, colorsNotEqual)
            )

            shrinkDomains { binding ->
                val newDomains: MutableMap<Variable<String>, Set<Color>> = HashMap(domains)
                for (v in binding.affectedVariables) {
                    newDomains[v] = (domains[v] ?: continue) - binding.value
                }
                newDomains
            }
        }

        val solution = s.solve()

        if (solution == null) println("no solutions found")
        else solution.forEach(::println)
    }


    @Test
    fun testQueens() {
        val sizes = listOf(8, 12, 16, 20, 24, 28)

        println("forward-check, heuristics")
        sizes.forEach {
            var depth = Depth(0)
            val bench = measureTimeMillis {
                depth = Depth(0)
                for (i in 1..2) nQueens(it, forwardCheck = true, useVariableChoiceHeuristic = true, printToStd = false, depth = depth)
            } / 2.0
            println("$it\t$bench\t${depth.i}")
        }

        println("forward-check, no heuristics")
        sizes.forEach {
            var depth = Depth(0)
            val bench = measureTimeMillis {
                depth = Depth(0)
                for (i in 1..2) nQueens(it, forwardCheck = true, useVariableChoiceHeuristic = false, printToStd = false, depth = depth)
            } / 2.0
            println("$it\t$bench\t${depth.i}")
        }

        println("backtrack, heuristics")
        (sizes - 28).forEach {
            var depth = Depth(0)
            val bench = measureTimeMillis {
                depth = Depth(0)
                for (i in 1..2) nQueens(it, forwardCheck = false, useVariableChoiceHeuristic = true, printToStd = false, depth = depth)
            } / 2.0
            println("$it\t$bench\t${depth.i}")
        }

        println("backtrack, no heuristics")
        (sizes - 28).forEach {
            var depth = Depth(0)
            val bench = measureTimeMillis {
                depth = Depth(0)
                for (i in 1..2) nQueens(it, forwardCheck = false, useVariableChoiceHeuristic = false, printToStd = false, depth = depth)
            } / 2.0
            println("$it\t$bench\t${depth.i}")
        }
    }

    @Test
    fun testSudoku() {
        val sudokus = listOf(
                loadSudoku("sudoku/plik2-1.txt"),
                loadSudoku("sudoku/plik2-2.txt"),
                loadSudoku("sudoku/plik2-3.txt"),
                loadSudoku("sudoku/plik3-1.txt"),
                loadSudoku("sudoku/plik3-2.txt")//,
//                loadSudoku("sudoku/plik4-1.txt")
        )

        sudokus.forEach {
            val (dim, sudoku) = it
            println("fc+h")
            var depth = Depth(0)
            var bench = measureTimeMillis {
                sudoku(dim, sudoku, depth = depth)
            }
            println("$bench\t${depth.i}")

            println("fc")
            depth = Depth(0)
            bench = measureTimeMillis {
                sudoku(dim, sudoku, useVariableChoiceHeuristic = false, depth = depth)
            }
            println("$bench\t${depth.i}")

            println("bt+h")
            depth = Depth(0)
            bench = measureTimeMillis {
                sudoku(dim, sudoku, forwardCheck = false, useVariableChoiceHeuristic = true, depth = depth)
            }
            println("$bench\t${depth.i}")

            println("bt")
            depth = Depth(0)
            bench = measureTimeMillis {
                sudoku(dim, sudoku, forwardCheck = false, useVariableChoiceHeuristic = false, depth = depth)
            }
            println("$bench\t${depth.i}")
        }
    }
}