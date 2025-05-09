package src.bomberman.core;

public enum GameState {
    PLAYING,             // Đang chơi level
    LEVEL_TRANSITIONING, // Đang hiển thị màn hình chuyển level
    GAME_OVER,           // Đã thua
    GAME_WON             // (Tùy chọn) Đã thắng tất cả level
}

