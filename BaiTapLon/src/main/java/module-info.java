module src.baitaplon {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires com.almasb.fxgl.all;

    opens src.baitaplon to javafx.fxml;
    exports src.baitaplon;
}