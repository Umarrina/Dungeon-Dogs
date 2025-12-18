package ru.kpfu.itis.group400.amirova.client.ui;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class GameUI {

    private BorderPane root = new  BorderPane();

    public Button exploreButton = new Button("Исследовать");
    public Button exitButton = new Button("Выйти");
    public Button sleepButton = new Button("Спать");
    public Button rotateButton = new Button("Повернуть");
    public Button placeButton = new Button("Поставить");

    private Label turnLabel = new Label("Ожидание начала игры");
    private TextArea logArea = new TextArea();
    private TextArea playersArea = new TextArea();
    private TextArea questsArea = new TextArea();

    private Canvas gameBoard;
    private ScrollPane boardScroll;

    private int cellSize = 60;
    private int offsetX = 0;
    private int offsetY = 0;
    private int selectedCellX = -1;
    private int selectedCellY = -1;

    public GameUI() {
        createSimpleUI();
        exploreButton.setDisable(true);
        exitButton.setDisable(true);
        sleepButton.setDisable(true);
        rotateButton.setDisable(true);
        placeButton.setDisable(true);
    }

    private void createSimpleUI() {
        HBox top = new HBox(10);
        top.setPadding(new Insets(10));

        Label title = new Label("Подземелья и песики");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        turnLabel.setStyle("-fx-font-size: 14px;");

        top.getChildren().addAll(title, turnLabel);
        root.setTop(top);

        GridPane center = new GridPane();
        center.setHgap(10);
        center.setVgap(10);
        center.setPadding(new Insets(10));

        VBox leftPanel =  new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-border-color: gray; -fx-border-width: 1;");

        Label playerLabel = new Label("Другие игроки");
        playersArea.setPrefSize(200, 400);
        playersArea.setEditable(false);
        playersArea.setText("Игрок 1\nИгрок 2\nИгрок 3");

        leftPanel.getChildren().addAll(playerLabel, playersArea);

        VBox centerPanel = new VBox(10);
        centerPanel.setPadding(new Insets(10));
        centerPanel.setStyle("-fx-border-color: gray; -fx-border-width: 1;");

        Label boardLabel = new Label("Игровое поле:");

        gameBoard = new Canvas(1000, 800);

        gameBoard.setOnMouseClicked(this::handleBoardClick);

        boardScroll = new ScrollPane(gameBoard);
        boardScroll.setPrefViewportWidth(500);
        boardScroll.setPrefViewportHeight(400);
        boardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        boardScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        centerPanel.getChildren().addAll(boardLabel, boardScroll);
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-border-color: gray; -fx-border-width: 1;");

        Label questsLabel = new Label("Квесты:");
        questsArea.setPrefSize(200, 400);
        questsArea.setEditable(false);

        rightPanel.getChildren().addAll(questsLabel, questsArea);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(250);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPrefWidth(250);

        center.getColumnConstraints().addAll(col1, col2, col3);
        center.add(leftPanel, 0, 0);
        center.add(centerPanel, 1, 0);
        center.add(rightPanel, 2, 0);

        root.setCenter(center);

        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setStyle("-fx-border-color: gray; -fx-border-width: 1 0 0 0;");

        Label myInfo = new Label("Текущий игрок: -");
        myInfo.setStyle("-fx-font-weight: bold;");

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(exploreButton, exitButton, sleepButton, rotateButton, placeButton);

        rotateButton.setDisable(true);
        placeButton.setDisable(true);

        Label logLabel = new Label("Лог игры:");
        logArea.setPrefHeight(100);
        logArea.setEditable(false);

        bottomPanel.getChildren().addAll(myInfo, buttons, logLabel, logArea);
        root.setBottom(bottomPanel);

        drawBoard();
    }

    private void drawBoard() {
        GraphicsContext gc = gameBoard.getGraphicsContext2D();

        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, gameBoard.getWidth(), gameBoard.getHeight());

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);

        int cols = (int)(gameBoard.getWidth() / cellSize);
        int rows = (int)(gameBoard.getHeight() / cellSize);

        for (int i = 0; i <= cols; i++) {
            gc.strokeLine(i * cellSize, 0, i * cellSize, rows * cellSize);
        }

        for (int i = 0; i <= rows; i++) {
            gc.strokeLine(0, i * cellSize, cols * cellSize, i * cellSize);
        }

        int centerX = cols / 2;
        int centerY = rows / 2;

        gc.setFill(Color.LIGHTGREEN);
        gc.fillRect(centerX * cellSize, centerY * cellSize, cellSize, cellSize);
        gc.setStroke(Color.BLACK);
        gc.strokeRect(centerX * cellSize, centerY * cellSize, cellSize, cellSize);
        gc.setFill(Color.BLACK);
        gc.fillText("СТАРТ", centerX * cellSize + 15, centerY * cellSize + 35);

        if (selectedCellX >= 0 && selectedCellY >= 0) {
            gc.setFill(Color.rgb(255, 255, 200, 0.5)); // Полупрозрачный желтый
            gc.fillRect(selectedCellX * cellSize, selectedCellY * cellSize, cellSize, cellSize);

            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2);
            gc.strokeRect(selectedCellX * cellSize, selectedCellY * cellSize, cellSize, cellSize);
        }

    }

    private void handleBoardClick(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        int cellX = (int)(mouseX / cellSize);
        int cellY = (int)(mouseY / cellSize);

        selectedCellX = cellX;
        selectedCellY = cellY;

        drawBoard();

        addLog("Выбрана клетка: [" + cellX + ", " + cellY + "]");

        placeButton.setDisable(false);
    }

    public BorderPane getRoot() {
        return root;
    }

    public void addLog(String message) {
        logArea.appendText(message + "\n");
    }

    public void setTurnInfo(String text) {
        turnLabel.setText(text);
    }

    public void setButtonsEnabled(boolean enabled) {
        exploreButton.setDisable(!enabled);
        exitButton.setDisable(!enabled);
        sleepButton.setDisable(!enabled);
    }

    public void setPlayerName(String name) {
        addLog("Вы: " + name);
    }

    public void enableRotateButton(boolean enable) {
        rotateButton.setDisable(!enable);
    }

    public void enablePlaceButton(boolean enable) {
        placeButton.setDisable(!enable);
    }

    public int[] getSelectedCell() {
        return new int[]{selectedCellX, selectedCellY};
    }

    public void clearSelection() {
        selectedCellX = -1;
        selectedCellY = -1;
        drawBoard();
    }

    public void updateBoard() {
        drawBoard();
    }
}