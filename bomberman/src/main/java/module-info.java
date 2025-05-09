module bomberman { 
    requires javafx.controls;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.desktop;

    opens src.bomberman to javafx.graphics, javafx.fxml;
    opens src.bomberman.graphics to javafx.graphics; 
    opens src.bomberman.entities to javafx.graphics;

}
