
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import tornadofx.*

class HelloWorldApp : App() {
    override val primaryView = HelloWorldView::class
}

class HelloWorldView : View() {
    override val root = VBox()
    private var _checkerboard: GridPane by singleAssign()
    val checkerboard: GridPane get() = _checkerboard
    private val tiles: MutableList<Pane> = arrayListOf()

    var _dim = 0

    var dim: Int
        get() = _dim
        set(value) {
            if (value < 0) throw IllegalArgumentException("dim")

            checkerboard.children.clear()
            tiles.clear()
            val width = 500.0 / value
            val height = width

            for (y in 0..value-1) {
                for (x in 0..value-1) {
                    val color = if (x%2 xor y%2 == 0) Color.BLACK else Color.WHITE
                    val tile = Pane().apply {
                        background = backgroundWithColor(color)
                        prefWidth = width
                        prefHeight = height
//                        textFill = Color.RED
//                        alignment = Pos.CENTER
//                        font = Font.font(font.family, FontWeight.BOLD, 20.0)
                    }
                    tiles += tile
                    checkerboard.add(tile, x, y)
                }
            }
            _dim = value
        }

    private fun backgroundWithColor(color: Color) = Background(BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY))

    init {
        title = "CSP"
        var topRow: HBox? = null

        with(root) {
            topRow = hbox {
                val dimens = textfield {
                    hboxConstraints { margin = Insets(5.0); }
                }
                button("Generate!") {
                    hboxConstraints { margin = Insets(5.0) }
                    onMouseClicked = EventHandler<MouseEvent> {
                        dim = dimens.text.toInt()
                        val progInd = topRow?.progressIndicator()

                        background {
                            nQueens(dim, printToStd = false)
                        } ui {
                            if (it != null) {
                                for ((x, y) in it) {
                                    tiles[(y - 1) * dim + (x - 1)].background = backgroundWithColor(Color.RED)
                                }
                            }
                            topRow?.children?.remove(progInd)
                        }
                    }
                }
            }
            _checkerboard = gridpane()
        }

        dim = 10
    }
}