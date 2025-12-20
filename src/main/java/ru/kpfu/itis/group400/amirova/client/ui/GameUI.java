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
import javafx.scene.text.Font;
import ru.kpfu.itis.group400.amirova.server.game.model.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameUI {
    private BorderPane root = new BorderPane();
    public Button exploreButton = new Button("Исследовать");
    public Button exitButton = new Button("Выйти");
    public Button sleepButton = new Button("Спать");
    public Button rotateButton = new Button("Повернуть");
    public Button placeButton = new Button("Поставить");
    public Button confirmExitButton = new Button("Подтвердить выход");
    public Button cancelExitButton = new Button("Отмена выхода");

    private Label turnLabel = new Label("Ожидание начала игры");
    private TextArea logArea = new TextArea();
    private TextArea playersStatsArea = new TextArea();

    public TextArea currentCardArea = new TextArea();
    private Label currentCardLabel = new Label("Текущая карта:");

    private Canvas gameBoard;
    private ScrollPane boardScroll;

    private int cellSize = 60;
    private int selectedCellX = -1;
    private int selectedCellY = -1;
    private List<Position> availablePositions = new ArrayList<>();

    private List<Position> selectedExitPath = new ArrayList<>();
    private boolean isSelectingExitPath = false;
    private List<Position> availableExits = new ArrayList<>();
    private Map<Position, String> boardCards = new HashMap<>();
    private Map<Position, String> boardCardTypes = new HashMap<>();
    private Map<Position, String> boardCardExits = new HashMap<>();

    private Consumer<int[]> boardClickListener;

    private int minX = -5;
    private int maxX = 5;
    private int minY = -5;
    private int maxY = 5;

    private Map<Position, Integer> boardCardRotations = new HashMap<>();
    private int currentCardRotation = 0;

    private CardRotatorPanel handCardPreview;
    private int currentCardId = -1;
    private String currentCardName = "";
    private int currentRotation = 0;

    public GameUI() {
        createUI();
        Position startPos = new Position(0, 0);
        boardCards.put(startPos, "Стартовая комната");
        boardCardTypes.put(startPos, "START");
        boardCardExits.put(startPos, "1:1:1:1");
        boardCardRotations.put(startPos, 0);
        updateFieldBounds();
        expandCanvasIfNeeded();
        drawBoard();
    }

    private void createUI() {
        createTopPanel();
        createCenterPanel();
        createBottomPanel();

        handCardPreview = new CardRotatorPanel();
        root.setLeft(handCardPreview);

        drawBoard();

        confirmExitButton.setVisible(false);
        confirmExitButton.setDisable(true);
    }

    private void createTopPanel() {
        HBox top = new HBox(10);
        top.setPadding(new Insets(10));
        top.setStyle("-fx-background-color: #f0f0f0;");

        Label title = new Label("Подземелья и песики");
        title.setStyle("-fx-font-size: 16px;");
        turnLabel.setStyle("-fx-font-size: 14px;");

        top.getChildren().addAll(title, turnLabel);
        root.setTop(top);
    }

    private void createCenterPanel() {
        GridPane center = new GridPane();
        center.setHgap(10);
        center.setVgap(10);
        center.setPadding(new Insets(10));
        center.setStyle("-fx-background-color: #f0f0f0;");

        VBox leftPanel = createPlayersStatsPanel();

        VBox centerPanel = createGameBoardPanel();

        VBox rightPanel = createCurrentCardPanel();

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(300);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPrefWidth(250);

        center.getColumnConstraints().addAll(col1, col2, col3);
        center.add(leftPanel, 0, 0);
        center.add(centerPanel, 1, 0);
        center.add(rightPanel, 2, 0);

        root.setCenter(center);
    }

    public void resetSelection() {
        selectedCellX = -1;
        selectedCellY = -1;
        clearAvailablePositions();
        enablePlaceButton(false);
        enableRotateButton(false);
        drawBoard();
        addLog("Выбор сброшен");
    }

    private VBox createPlayersStatsPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #e0e0e0;");

        Label playerLabel = new Label("Статистика игрока");
        playerLabel.setStyle("-fx-font-weight: bold;");

        playersStatsArea.setPrefSize(280, 600);
        playersStatsArea.setEditable(false);
        playersStatsArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px; -fx-control-inner-background: #f0f0f0;");

        panel.getChildren().addAll(playerLabel, playersStatsArea);
        return panel;
    }

    private VBox createCurrentCardPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #e0e0e0;");

        currentCardLabel.setStyle("-fx-font-weight: bold;");

        currentCardArea.setPrefSize(230, 150);
        currentCardArea.setEditable(false);
        currentCardArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px; -fx-control-inner-background: #f0f0f0;");
        currentCardArea.setText("Нет карты на руке");

        panel.getChildren().addAll(currentCardLabel, currentCardArea);
        return panel;
    }

    private VBox createGameBoardPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-border-color: gray; -fx-border-width: 1; -fx-background-color: #e0e0e0;");

        Label boardLabel = new Label("Игровое поле:");
        gameBoard = new Canvas(800, 600);
        gameBoard.setOnMouseClicked(this::handleBoardClick);

        boardScroll = new ScrollPane(gameBoard);
        boardScroll.setPrefViewportWidth(500);
        boardScroll.setPrefViewportHeight(400);
        boardScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        boardScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        panel.getChildren().addAll(boardLabel, boardScroll);
        return panel;
    }

    private void createBottomPanel() {
        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setStyle("-fx-border-color: gray; -fx-border-width: 1 0 0 0; -fx-background-color: #f0f0f0;");

        Label myInfo = new Label("Текущий игрок: -");
        myInfo.setStyle("-fx-font-weight: bold;");

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(exploreButton, exitButton, sleepButton, rotateButton, placeButton, confirmExitButton, cancelExitButton);

        rotateButton.setDisable(true);
        placeButton.setDisable(true);

        cancelExitButton.setVisible(false);
        cancelExitButton.setDisable(true);

        confirmExitButton.setVisible(false);
        confirmExitButton.setDisable(true);

        Label logLabel = new Label("Лог игры:");
        logArea.setPrefHeight(100);
        logArea.setEditable(false);
        logArea.setStyle("-fx-control-inner-background: #f0f0f0;");

        bottomPanel.getChildren().addAll(myInfo, buttons, logLabel, logArea);
        root.setBottom(bottomPanel);
    }

    private void handleBoardClick(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        int cellX = (int)(mouseX / cellSize);
        int cellY = (int)(mouseY / cellSize);

        int centerX = (int)(gameBoard.getWidth() / (2 * cellSize));
        int centerY = (int)(gameBoard.getHeight() / (2 * cellSize));

        int gameX = cellX - centerX;
        int gameY = cellY - centerY;

        selectedCellX = gameX;
        selectedCellY = gameY;
        drawBoard();
        addLog("Клик: [" + gameX + ", " + gameY + "]");

        boolean isAvailable = availablePositions.stream()
                .anyMatch(p -> p.getX() == gameX && p.getY() == gameY);
        placeButton.setDisable(!isAvailable);

        if (boardClickListener != null) {
            boardClickListener.accept(new int[]{gameX, gameY});
        }
    }

    private void drawBoard() {
        GraphicsContext gc = gameBoard.getGraphicsContext2D();
        gc.clearRect(0, 0, gameBoard.getWidth(), gameBoard.getHeight());

        int cols = (int)(gameBoard.getWidth() / cellSize);
        int rows = (int)(gameBoard.getHeight() / cellSize);
        int centerX = cols / 2;
        int centerY = rows / 2;


        drawGrid(gc, cols, rows);

        drawAllCards(gc, centerX, centerY);

        if (!isSelectingExitPath) {
            drawAvailablePositions(gc, centerX, centerY);
        }
        drawSelectedCell(gc, centerX, centerY);

        if (isSelectingExitPath) {
            drawExitPath(gc, centerX, centerY);
        }
    }

    private void drawGrid(GraphicsContext gc, int cols, int rows) {
        gc.setFill(Color.rgb(245, 245, 245));
        gc.fillRect(0, 0, gameBoard.getWidth(), gameBoard.getHeight());

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);

        for (int i = 0; i <= cols; i++) {
            gc.strokeLine(i * cellSize, 0, i * cellSize, rows * cellSize);
        }
        for (int i = 0; i <= rows; i++) {
            gc.strokeLine(0, i * cellSize, cols * cellSize, i * cellSize);
        }

        gc.setFill(Color.GRAY);
        gc.setFont(new Font("Arial", 9));

        int centerX = cols / 2;
        int centerY = rows / 2;

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                int gameX = i - centerX;
                int gameY = j - centerY;
                if (Math.abs(gameX) % 5 == 0 && Math.abs(gameY) % 5 == 0) {
                    gc.fillText(gameX + "," + gameY, i * cellSize + 2, j * cellSize + 10);
                }
            }
        }
    }

    private void drawAvailablePositions(GraphicsContext gc, int centerX, int centerY) {
        gc.setFill(Color.rgb(200, 255, 200, 0.5));
        for (Position pos : availablePositions) {
            int drawX = (pos.getX() + centerX) * cellSize;
            int drawY = (pos.getY() + centerY) * cellSize;
            gc.fillRect(drawX, drawY, cellSize, cellSize);

            gc.setStroke(Color.GREEN);
            gc.setLineWidth(1);
            gc.strokeRect(drawX, drawY, cellSize, cellSize);
        }
    }

    private void drawSelectedCell(GraphicsContext gc, int centerX, int centerY) {
        if (selectedCellX >= -centerX && selectedCellX <= centerX &&
                selectedCellY >= -centerY && selectedCellY <= centerY) {

            int drawX = (selectedCellX + centerX) * cellSize;
            int drawY = (selectedCellY + centerY) * cellSize;

            gc.setFill(Color.rgb(255, 255, 200, 0.5));
            gc.fillRect(drawX, drawY, cellSize, cellSize);

            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2);
            gc.strokeRect(drawX, drawY, cellSize, cellSize);
        }
    }

    private void drawAllCards(GraphicsContext gc, int centerX, int centerY) {
        for (Map.Entry<Position, String> entry : boardCards.entrySet()) {
            Position pos = entry.getKey();
            String cardName = entry.getValue();
            String cardType = boardCardTypes.getOrDefault(pos, "?");
            String exitsStr = boardCardExits.getOrDefault(pos, "0:0:0:0");
            int rotation = boardCardRotations.getOrDefault(pos, 0);

            double drawX = (pos.getX() + centerX) * (double)cellSize;
            double drawY = (pos.getY() + centerY) * (double)cellSize;

            gc.setFill(getBrightCardColor(cardType));
            gc.fillRect(drawX, drawY, cellSize, cellSize);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(3);
            gc.strokeRect(drawX, drawY, cellSize, cellSize);

            String rotatedExits = rotateExits(exitsStr, rotation);
            System.out.println("Карта " + cardName + " поворот=" + rotation + " исходные=" + exitsStr + " → " + rotatedExits);
            drawCardExits(gc, drawX, drawY, rotatedExits);

            gc.setFill(Color.BLACK);
            gc.setFont(new Font("Arial", 20));
            gc.fillText(getLargeCardIcon(cardType), drawX + 8, drawY + 28);
            gc.setFont(new Font("Arial", 12));
            String shortName = cardName.length() > 10 ? cardName.substring(0,10)+".." : cardName;
            gc.fillText(shortName, drawX + 8, drawY + 48);

            gc.setFill(Color.MAGENTA);
            gc.setFont(new Font("Arial", 10));
            gc.fillText("R:" + rotation + "°", drawX + 2, drawY + 15);
        }
    }

    private String rotateExits(String exitsStr, int degrees) {
        String[] exits = exitsStr.split(":");
        int rotations = degrees / 90 % 4;

        for (int r = 0; r < rotations; r++) {
            String temp = exits[0];
            exits[0] = exits[3];
            exits[3] = exits[2];
            exits[2] = exits[1];
            exits[1] = temp;
        }
        return String.join(":", exits);
    }

    public void setAvailablePositions(List<Position> positions) {
        availablePositions.clear();
        availablePositions.addAll(positions);

        if (!positions.isEmpty()) {
            Position firstPos = positions.get(0);
            selectedCellX = firstPos.getX();
            selectedCellY = firstPos.getY();
            placeButton.setDisable(false);
            addLog("Доступные позиции: " + positions.size());
        }
        drawBoard();
    }

    private Color getBrightCardColor(String cardType) {
        if (cardType == null || cardType.isEmpty()) return Color.rgb(230, 230, 230);

        String typeUpper = cardType.toUpperCase();
        switch (typeUpper) {
            case "ENEMY": return Color.rgb(255, 150, 150);
            case "COIN": return Color.rgb(255, 255, 150);
            case "ARTIFACT": return Color.rgb(150, 150, 255);
            case "START": return Color.rgb(150, 255, 150);
            default: return Color.rgb(230, 230, 230);
        }
    }

    private String getLargeCardIcon(String cardType) {
        if (cardType == null) return "❓";

        String type = cardType.toUpperCase();
        switch (type) {
            case "ENEMY": return "👹";
            case "COIN": return "💰";
            case "ARTIFACT": return "⚔️";
            case "START": return "🚪";
            default: return "❓";
        }
    }

    private void drawCardExits(GraphicsContext gc, double x, double y, String exitsStr) {
        if (exitsStr == null || exitsStr.isEmpty()) return;

        String[] exits = exitsStr.split(":");
        if (exits.length < 4) return;

        gc.setFill(Color.CYAN);
        double markerSize = 16;
        double halfMarker = markerSize / 2;

        if ("1".equals(exits[0])) {
            gc.fillRect(x + cellSize / 2 - halfMarker, y, markerSize, markerSize);
        }
        if ("1".equals(exits[1])) {
            gc.fillRect(x + cellSize - markerSize, y + cellSize / 2 - halfMarker, markerSize, markerSize);
        }
        if ("1".equals(exits[2])) {
            gc.fillRect(x + cellSize / 2 - halfMarker, y + cellSize - markerSize, markerSize, markerSize);
        }
        if ("1".equals(exits[3])) {
            gc.fillRect(x, y + cellSize / 2 - halfMarker, markerSize, markerSize);
        }
    }

    private void updateFieldBounds() {
        minX = -10; maxX = 10;
        minY = -10; maxY = 10;
    }

    private void expandCanvasIfNeeded() {
        int colsNeeded = maxX - minX + 3;
        int rowsNeeded = maxY - minY + 3;

        int neededWidth = colsNeeded * cellSize;
        int neededHeight = rowsNeeded * cellSize;

        neededWidth = Math.max(neededWidth, 1200);
        neededHeight = Math.max(neededHeight, 900);

        if (neededWidth > gameBoard.getWidth() || neededHeight > gameBoard.getHeight()) {
            Canvas newCanvas = new Canvas(neededWidth, neededHeight);
            newCanvas.setOnMouseClicked(this::handleBoardClick);
            gameBoard = newCanvas;
            boardScroll.setContent(gameBoard);
        }
    }

    private void drawExitPath(GraphicsContext gc, int centerX, int centerY) {
        gc.setFill(Color.rgb(255, 200, 200, 0.5));
        for (Position exit : availableExits) {
            int drawX = (exit.getX() + centerX) * cellSize;
            int drawY = (exit.getY() + centerY) * cellSize;
            gc.fillRect(drawX, drawY, cellSize, cellSize);

            gc.setStroke(Color.RED);
            gc.setLineWidth(2);
            gc.strokeRect(drawX, drawY, cellSize, cellSize);
            gc.setFill(Color.BLACK);
            gc.fillText("ВЫХОД", drawX + 10, drawY + 35);
        }

        for (int i = 0; i < selectedExitPath.size(); i++) {
            Position pos = selectedExitPath.get(i);
            int drawX = (pos.getX() + centerX) * cellSize;
            int drawY = (pos.getY() + centerY) * cellSize;

            if (i == 0) {
                gc.setFill(Color.rgb(180, 220, 180, 0.7));
            } else if (i == selectedExitPath.size() - 1) {
                if (availableExits.contains(pos)) {
                    gc.setFill(Color.rgb(255, 100, 100, 0.7));
                } else {
                    gc.setFill(Color.rgb(220, 200, 180, 0.7));
                }
            } else {
                gc.setFill(Color.rgb(200, 220, 240, 0.7));
            }

            gc.fillRect(drawX, drawY, cellSize, cellSize);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(drawX, drawY, cellSize, cellSize);

            gc.setFill(Color.BLACK);
            gc.fillText(String.valueOf(i + 1), drawX + cellSize/2 - 5, drawY + cellSize/2 + 5);

            if (i == selectedExitPath.size() - 1 && availableExits.contains(pos)) {
                gc.setFill(Color.RED);
                gc.fillText("🚪", drawX + cellSize - 20, drawY + 20);
            }
        }
    }


    public BorderPane getRoot() {
        return root;
    }

    public void addLog(String message) {
        logArea.appendText(message + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    public void setTurnInfo(String text) {
        turnLabel.setText(text);
    }

    public void setButtonsEnabled(boolean enabled) {
        exploreButton.setDisable(!enabled);
        exitButton.setDisable(!enabled);
        sleepButton.setDisable(!enabled);
        if (!enabled) {
            rotateButton.setDisable(true);
            placeButton.setDisable(true);
        }
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

    public void disableAllButtons() {
        exploreButton.setDisable(true);
        exitButton.setDisable(true);
        sleepButton.setDisable(true);
        rotateButton.setDisable(true);
        placeButton.setDisable(true);
    }

    public boolean isPositionAvailable(int x, int y) {
        return availablePositions.stream()
                .anyMatch(p -> p.getX() == x && p.getY() == y);
    }

    public void setCurrentCard(String cardInfo) {
        currentCardRotation = 0;
        currentCardArea.setText(cardInfo + "\nПоворот: 0°");
    }

    public void addCardToBoard(Position position, String cardName, String cardType, String exits, int rotation) {
        System.out.println("addCardToBoard: " + position + " " + cardName + " поворот: " + rotation + "°");

        if (boardCards.containsKey(position)) {
            addLog("Позиция занята!");
            return;
        }

        boardCards.put(position, cardName);
        boardCardTypes.put(position, cardType);
        boardCardExits.put(position, exits);
        boardCardRotations.put(position, rotation);

        System.out.println("✅ Карта добавлена с поворотом: " + rotation + "°");

        clearAvailablePositions();
        clearSelection();
        updateFieldBounds();
        expandCanvasIfNeeded();
        drawBoard();
        addLog("Карта '" + cardName + "' размещена на " + position);
    }

    public void clearBoard() {
        Position startPos = new Position(0, 0);
        String startName = boardCards.get(startPos);
        String startType = boardCardTypes.get(startPos);
        String startExits = boardCardExits.get(startPos);

        boardCards.clear();
        boardCardTypes.clear();
        boardCardExits.clear();

        if (startName != null) {
            boardCards.put(startPos, startName);
            boardCardTypes.put(startPos, startType);
            boardCardExits.put(startPos, startExits);
        }

        clearAvailablePositions();

        drawBoard();
        addLog("Поле очищено, стартовая комната сохранена");
    }

    public void setSelectedCell(int x, int y) {
        selectedCellX = x;
        selectedCellY = y;
        drawBoard();
    }

    public void setBoardClickListener(Consumer<int[]> listener) {
        this.boardClickListener = listener;
    }

    public void handleNotYourTurn(String currentPlayer) {
        setTurnInfo("Ходит: " + currentPlayer);
        disableAllButtons();
        addLog("Сейчас ходит: " + currentPlayer);
    }

    public void startExitPathSelection(List<Position> exits) {
        this.isSelectingExitPath = true;
        this.availableExits = new ArrayList<>(exits);
        this.selectedExitPath.clear();

        Position startPos = new Position(0, 0);
        if (!selectedExitPath.contains(startPos)) {
            selectedExitPath.add(startPos);
        }

        confirmExitButton.setVisible(true);
        confirmExitButton.setDisable(true);
        cancelExitButton.setVisible(true);
        cancelExitButton.setDisable(false);

        exploreButton.setDisable(true);
        sleepButton.setDisable(true);
        exitButton.setDisable(true);
        rotateButton.setDisable(true);
        placeButton.setDisable(true);

        addLog("Выберите путь выхода. Начните со стартовой комнаты.");
        addLog("Доступно выходов: " + exits.size());
        drawBoard();
    }

    public void handleExitPathClick(Position clickedPos) {
        if (!isSelectingExitPath) return;

        try {
            if (!clickedPos.equals(new Position(0, 0)) && !boardCards.containsKey(clickedPos)) {
                addLog("Ошибка: на этой ячейке нет карты!");
                return;
            }

            if (selectedExitPath.contains(clickedPos)) {
                int index = selectedExitPath.indexOf(clickedPos);
                if (index > 0) {
                    selectedExitPath = new ArrayList<>(selectedExitPath.subList(0, index));
                    addLog("Убрана комната [" + clickedPos.getX() + ", " + clickedPos.getY() + "]");

                    confirmExitButton.setDisable(!isExitPathComplete());
                }
            } else {
                if (!selectedExitPath.isEmpty()) {
                    Position lastPos = selectedExitPath.get(selectedExitPath.size() - 1);
                    if (!arePositionsAdjacent(lastPos, clickedPos)) {
                        addLog("Ошибка: комнаты должны быть смежными!");
                        return;
                    }
                }

                selectedExitPath.add(clickedPos);
                addLog("Добавлена комната [" + clickedPos.getX() + ", " + clickedPos.getY() + "]");

                if (availableExits.contains(clickedPos)) {
                    addLog("✓ Достигнут выход! Теперь нажмите 'Подтвердить выход'.");
                }

                confirmExitButton.setDisable(!isExitPathComplete());
            }

            drawBoard();
        } catch (Exception e) {
            addLog("Ошибка при выборе пути: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean arePositionsAdjacent(Position pos1, Position pos2) {
        int dx = Math.abs(pos1.getX() - pos2.getX());
        int dy = Math.abs(pos1.getY() - pos2.getY());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    public List<Position> getSelectedExitPath() {
        return new ArrayList<>(selectedExitPath);
    }

    public void cancelExitPathSelection() {
        isSelectingExitPath = false;
        selectedExitPath.clear();
        availableExits.clear();

        confirmExitButton.setVisible(false);
        confirmExitButton.setDisable(true);
        cancelExitButton.setVisible(false);
        cancelExitButton.setDisable(true);

        drawBoard();
    }

    public boolean isExitPathComplete() {
        if (selectedExitPath.isEmpty()) return false;

        Position lastPos = selectedExitPath.get(selectedExitPath.size() - 1);
        return availableExits.contains(lastPos);
    }

    public boolean isSelectingExitPath() {
        return isSelectingExitPath;
    }

    public void clearAvailablePositions() {
        this.availablePositions.clear();
        this.placeButton.setDisable(true);
        this.selectedCellX = -1;
        this.selectedCellY = -1;
        drawBoard();
        addLog("Доступные позиции сброшены");
    }

    public void addPlayerStats(String playerInfo) {
        playersStatsArea.appendText(playerInfo + "\n");
    }

    public void clearPlayersStats() {
        playersStatsArea.clear();
    }

    public void setCurrentCard(int cardId, String name, String exits) {
        currentCardId = cardId;
        currentCardName = name;
        currentRotation = 0;
        if (handCardPreview != null) {
            handCardPreview.setCard(cardId, name, exits);
        }
        addLog("Карта: " + name + " (ID: " + cardId + ")");
    }

    public void rotateCurrentCard() {
        currentRotation = (currentRotation + 90) % 360;
        if (handCardPreview != null) {
            handCardPreview.rotateTo(currentRotation);
        }
        addLog("Поворот: " + currentRotation + "°");
    }

    public int getCurrentCardRotation() {
        return currentRotation;
    }
}