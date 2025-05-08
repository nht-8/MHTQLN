module src.bomberman {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires com.almasb.fxgl.all;

    opens src.bomberman to javafx.fxml;
    exports src.bomberman;
}