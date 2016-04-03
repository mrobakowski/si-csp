import javafx.scene.control.Label
import javafx.scene.layout.VBox
import tornadofx.App
import tornadofx.View

class HelloWorldApp : App() {
    override val primaryView = HelloWorldView::class

}

class HelloWorldView : View() {
    override val root = VBox(Label("Hello World"))
    init {
        title = "Hello world"
    }
}