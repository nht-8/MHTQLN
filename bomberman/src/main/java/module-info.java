// src/main/java/module-info.java
module bomberman { // Đặt tên module của bạn (ví dụ: bomberman hoặc uet.oop.bomberman)
    requires javafx.controls;
    requires javafx.media;
    requires javafx.graphics;
    requires javafx.fxml;

    // Mở các package chứa Node và tài nguyên cho JavaFX
    // Sửa lại tên package cho đúng với project của bạn
    opens src.bomberman to javafx.graphics, javafx.fxml; // << THÊM javafx.fxml VÀO ĐÂY
    opens src.bomberman.graphics to javafx.graphics; // Nếu có Node trong package này
    opens src.bomberman.entities to javafx.graphics; // Nếu có Node trong package này

    // Nếu bạn đặt tài nguyên ở package khác, cần opens package đó, ví dụ:
    // opens images to javafx.graphics; // Nếu ảnh nằm trong package 'images' (ít phổ biến)
}