module bomberman { 
    requires javafx.controls;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;

    opens src.bomberman to javafx.graphics, javafx.fxml; // << THÊM javafx.fxml VÀO ĐÂY
    opens src.bomberman.graphics to javafx.graphics; // Nếu có Node trong package này
    opens src.bomberman.entities to javafx.graphics; // Nếu có Node trong package này

}
