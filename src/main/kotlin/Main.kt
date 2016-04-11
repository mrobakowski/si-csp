import javafx.geometry.Insets
import javafx.scene.control.TextField
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
    private var _dimQueens = 0
    var dimQueens: Int
        get() = _dimQueens
        set(value) {
            if (value < 0) throw IllegalArgumentException("dimQueens")

            checkerboard.children.clear()
            tiles.clear()
            val width = 500.0 / value
            val height = width

            for (y in 0..value - 1) {
                for (x in 0..value - 1) {
                    val color = if (x % 2 xor y % 2 == 0) Color.BLACK else Color.WHITE
                    val tile = Pane().apply {
                        background = backgroundWithColor(color)
                        prefWidth = width
                        prefHeight = height
                    }
                    tiles += tile
                    checkerboard.add(tile, x, y)
                }
            }
            _dimQueens = value
        }

    private var _sudoku: GridPane by singleAssign()
    val sudoku: GridPane get() = _sudoku
    private val sudokuCells: MutableList<TextField> = arrayListOf()
    private var _dimSudoku = 0
    var dimSudoku: Int
        get() = _dimSudoku
        set(value) {
            if (value == dimSudoku) return
            if (value < 0) throw IllegalArgumentException("dimSudoku")

            sudoku.children.clear()
            sudokuCells.clear()
            val width = 500.0 / value
            val height = width

            for (y in 0..value - 1) {
                for (x in 0..value - 1) {
                    val textField = TextField().apply {
                        prefWidth = width
                        prefHeight = height
                    }
                    sudokuCells += textField
                    sudoku.add(textField, x, y)
                }
            }
            _dimSudoku = value
        }

    private fun backgroundWithColor(color: Color) = Background(BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY))

    init {
        title = "CSP"
        var topRowQueens: HBox? = null
        var topRowSudoku: HBox? = null

        with(root) {
            tabpane {
                tab("n-Queens", VBox()) {
                    topRowQueens = hbox {
                        val dimens = textfield {
                            hboxConstraints { margin = Insets(5.0); }
                        }
                        button("Generate!") {
                            hboxConstraints { margin = Insets(5.0) }
                            onMouseClicked = javafx.event.EventHandler<MouseEvent> {
                                dimQueens = dimens.text.toInt()
                                val progInd = topRowQueens?.progressIndicator { prefHeight = 5.0 }

                                background {
                                    nQueens(dimQueens, printToStd = false)
                                } ui {
                                    if (it != null) {
                                        for ((x, y) in it) {
                                            tiles[(y - 1) * dimQueens + (x - 1)].background = backgroundWithColor(javafx.scene.paint.Color.RED)
                                        }
                                    }
                                    topRowQueens?.children?.remove(progInd)
                                }
                            }
                        }
                    }
                    _checkerboard = gridpane()
                }.apply { isClosable = false }

                tab("Sudoku", VBox()) {
                    topRowSudoku = hbox {
                        val dimens = textfield {
                            hboxConstraints { margin = Insets(5.0); }
                        }
                        button("Solve") {
                            hboxConstraints { margin = Insets(5.0) }
                            onMouseClicked = javafx.event.EventHandler<MouseEvent> {
                                dimSudoku = dimens.text.toInt().let { it * it }
                                val progInd = topRowSudoku?.progressIndicator { prefHeight = 5.0 }

                                background {
                                    val sudokuValues: MutableMap<Pair<Int, Int>, Int> = mutableMapOf()

                                    sudokuCells.forEachIndexed { i, it ->
                                        if (!it.text.isNullOrBlank()) {
                                            val x = it.text.toInt()
                                            if (x < 0 || x > dimSudoku) throw IllegalArgumentException("Invalid sudoku value")
                                            sudokuValues[i % dimSudoku to i / dimSudoku] = x
                                        }
                                    }

                                    sudoku(dimSudoku, sudokuValues)
                                } ui {
                                    it?.forEachIndexed { i, it ->
                                        sudokuCells[i].text = it.toString()
                                    }
                                    topRowSudoku?.children?.remove(progInd)
                                }
                            }
                        }
                    }
                    _sudoku = gridpane()
                }.apply { isClosable = false }
            }

        }

        dimQueens = 9
        dimSudoku = 9

        //        primaryStage.isResizable = false
        //        primaryStage.width = 500.0
        //        primaryStage.height = 597.0
    }
}