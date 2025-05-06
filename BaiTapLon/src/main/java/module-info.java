<<<<<<< HEAD
module BTL { // Hoặc tên module bạn đã đặt
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Đảm bảo có dòng này

    // Thay 'com.mygame' bằng package thực tế chứa các lớp Java của bạn
    opens com.mygame to javafx.fxml;
    exports com.mygame;
=======
module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
>>>>>>> main
}