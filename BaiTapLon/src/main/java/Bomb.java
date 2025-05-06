package org.example.demo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

public class Bomb {
    private final double x,y; private final int gridX,gridY,size;
    private final List<Image> animationFrames; private int currentFrameIndex=0; private double animationTimer=0, animationFrameDuration=0.12;
    private double fuseTime=3.0; private double timer; private boolean exploded=false;

    // +++ THÊM THUỘC TÍNH OWNER +++
    private final Object owner; // Có thể là Player hoặc Enemy

    // Sửa Constructor để nhận owner
    public Bomb(int gX, int gY, int tS, List<Image> frames, Object owner) { // Thêm owner
        this.gridX=gX; this.gridY=gY; this.size=tS; this.x=gX*tS; this.y=gY*tS;
        this.animationFrames=(frames!=null&&!frames.isEmpty())?frames:new ArrayList<>();
        this.timer=fuseTime;
        this.owner = owner; // Lưu owner
        if(this.animationFrames.isEmpty())System.err.println("WARN: Bomb no frames!");
    }

    // Giữ nguyên update() và render()
    public void update(double dT){if(!exploded){timer-=dT;if(timer<=0){exploded=true;return;}if(!animationFrames.isEmpty()){animationTimer+=dT;if(animationTimer>=animationFrameDuration){animationTimer-=animationFrameDuration;currentFrameIndex=(currentFrameIndex+1)%animationFrames.size();}}}}
    public void render(GraphicsContext gc){if(!exploded){Image cF=null;if(!animationFrames.isEmpty())cF=animationFrames.get(currentFrameIndex%animationFrames.size());if(cF!=null)gc.drawImage(cF,x,y,size,size);else{gc.setFill(Color.BLACK);gc.fillOval(x+size*0.1,y+size*0.1,size*0.8,size*0.8);}}}

    // --- Getters ---
    public boolean isExploded(){return exploded;} public int getGridX(){return gridX;} public int getGridY(){return gridY;}
    // +++ THÊM GETTER CHO OWNER +++
    public Object getOwner() { return owner; }
}