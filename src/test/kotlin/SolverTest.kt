import org.junit.Test

enum class Color { red, green, blue }
class Test {
    @Test fun testSolver() {

        val s: CspSolver<String, Color> = solver {
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
        }

        val solution = s.solve()

        if (solution == null) println("no solutions found")
        else solution.forEach(::println)
    }
}