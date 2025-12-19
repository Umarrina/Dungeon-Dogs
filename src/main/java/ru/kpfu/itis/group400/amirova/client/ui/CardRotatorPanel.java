package ru.kpfu.itis.group400.amirova.client.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CardRotatorPanel extends Pane {
    private double currentAngle = 0;
    private double targetAngle = 0;
    private int displayCardId = -1;
    private String displayName = "";
    private String currentExits = "1:1:1:1";
    private Canvas canvas;

    public CardRotatorPanel() {
        canvas = new Canvas(150, 200);
        getChildren().add(canvas);
        drawEmptyCard();
    }


    public void setCard(int cardId, String name, String exits) {
        this.displayCardId = cardId;
        this.displayName = name;
        this.currentExits = exits != null ? exits : "1:1:1:1";
        this.currentAngle = 0;
        drawCard();
    }

    public void rotateTo(double angleDeg) {
        targetAngle = angleDeg;
        AnimationTimer animation = new AnimationTimer() {
            private long startTime = System.currentTimeMillis();
            @Override
            public void handle(long now) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed < 400) {
                    double progress = 1 - Math.pow(1 - elapsed / 400.0, 3);
                    currentAngle = lerpAngle(currentAngle, targetAngle, progress);
                    drawCard();
                } else {
                    currentAngle = targetAngle;
                    drawCard();
                    stop();
                }
            }
        };
        animation.start();
    }

    private void drawCard() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, 150, 200);

        g.setFill(Color.BLACK);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        String shortName = displayName.length() > 14 ? displayName.substring(0, 14) + ".." : displayName;
        g.fillText(shortName, 10, 25);

        g.setFill(Color.BLACK);
        g.setFont(Font.font("Arial", 12));
        g.fillText("ID: " + displayCardId, 10, 45);

        g.save();
        g.translate(75, 125);
        g.rotate(currentAngle);

        g.setFill(Color.hsb(220, 0.3, 0.25));
        g.fillRoundRect(-50, -50, 100, 100, 12, 12);
        g.setStroke(Color.YELLOW);
        g.setLineWidth(2.5);
        g.strokeRoundRect(-50, -50, 100, 100, 12, 12);

        drawSquareExits(g);

        g.restore();

        g.setFill(Color.BLACK.deriveColor(0,1,1,0.8));
        g.fillRoundRect(10, 165, 50, 25, 8, 8);
        g.setFill(Color.WHITE);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        g.fillText((int)currentAngle + "°", 18, 183);
    }


    private void drawSquareExits(GraphicsContext g) {
        String[] exitArr = currentExits.split(":");
        boolean[] hasExit = {
                exitArr.length > 0 && "1".equals(exitArr[0]),  // ↑
                exitArr.length > 1 && "1".equals(exitArr[1]),  // →
                exitArr.length > 2 && "1".equals(exitArr[2]),  // ↓
                exitArr.length > 3 && "1".equals(exitArr[3])   // ←
        };

        g.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        if (hasExit[0]) { g.setFill(Color.LIME); g.fillText("↑",  -5, -20); }  // Верх
        if (hasExit[1]) { g.setFill(Color.LIME); g.fillText("→",  20,  10); }  // Право
        if (hasExit[2]) { g.setFill(Color.LIME); g.fillText("↓",  -5,  30); }  // Низ
        if (hasExit[3]) { g.setFill(Color.LIME); g.fillText("←",  -40,  10); }  // Лево
    }

    private double lerpAngle(double from, double to, double t) {
        double diff = to - from;
        if (Math.abs(diff) > 180) diff += (diff > 0 ? -360 : 360);
        return from + diff * t;
    }

    public void clearCard() {
        displayCardId = -1;
        displayName = "";
        currentExits = "1:1:1:1";
        currentAngle = 0;
        drawEmptyCard();
    }

    private void drawEmptyCard() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, 150, 200);

        g.setFill(Color.GRAY.deriveColor(0, 1, 1, 0.3));
        g.fillRoundRect(25, 50, 100, 100, 12, 12);

        g.setFill(Color.WHITE);
        g.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        g.fillText("КАРТА В РУКЕ", 35, 105);
        g.setFont(Font.font("Arial", 12));
        g.fillText("Кликните для деталей", 28, 130);
    }
}
