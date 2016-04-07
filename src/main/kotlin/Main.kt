
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

class HelloWorldApp : App() {
    override val primaryView = HelloWorldView::class

}

class HelloWorldView : View() {
    override val root = VBox()

    init {
        title = "CSP"

        with(root) {
            hbox {
                button("Generate!") {
                    hboxConstraints { margin = Insets(5.0) }
                }
                textfield {
                    hboxConstraints { margin = Insets(5.0);  }
                    useMaxWidth = true
                }
                background = Background(BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets(0.0)))
            }
        }
    }
}