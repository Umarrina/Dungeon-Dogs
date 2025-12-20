package ru.kpfu.itis.group400.amirova.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ru.kpfu.itis.group400.amirova.client.ui.GameUI;
import ru.kpfu.itis.group400.amirova.io.ConfigurationReader;
import ru.kpfu.itis.group400.amirova.server.game.model.Position;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class GameClient extends Application {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameUI gameUI;
    private String username;

    private Map<Integer, String> cardInfoCache = new HashMap<>();
    private Map<Integer, String> cardTypeCache = new HashMap<>();
    private Map<Integer, String> cardExitsCache = new HashMap<>();
    private Map<Integer, String> cardDetailsCache = new HashMap<>();

    private int currentCardId = -1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        loadCardCache();
        gameUI = new GameUI();

        gameUI.exploreButton.setOnAction(e -> sendAction("EXPLORE"));
        gameUI.sleepButton.setOnAction(e -> sendAction("SLEEP"));
        gameUI.rotateButton.setOnAction(e -> {
            gameUI.rotateCurrentCard();
            sendAction("ROTATE");
        });
        gameUI.exitButton.setOnAction(e -> handleExitAction());
        gameUI.placeButton.setOnAction(e -> handlePlaceAction());
        gameUI.confirmExitButton.setOnAction(e -> handleConfirmExitAction());
        gameUI.cancelExitButton.setOnAction(e -> {
            sendAction("CANCEL_EXIT");
            gameUI.cancelExitPathSelection();
            gameUI.addLog("Выход отменен");
        });

        gameUI.setBoardClickListener(coords -> {
            int x = coords[0];
            int y = coords[1];

            if (gameUI.isSelectingExitPath()) {
                gameUI.handleExitPathClick(new Position(x, y));
            } else {
                gameUI.setSelectedCell(x, y);
                boolean isAvailable = gameUI.isPositionAvailable(x, y);
                gameUI.enablePlaceButton(isAvailable);
            }
        });

        Scene scene = new Scene(gameUI.getRoot(), 1200, 800);
        stage.setTitle("Подземелья и Пёсики");
        stage.setScene(scene);
        stage.show();

        showConnectDialog();
    }

    private void loadCardCache() {
        ConfigurationReader reader = new ConfigurationReader();
        List<String> lines = reader.loadAllLines();
        for (String line : lines) {
            String[] parts = line.split(";");
            if (parts.length >= 4) {
                try {
                    int cardId = Integer.parseInt(parts[0].trim());
                    String cardName = parts[1].trim();
                    String exitsStr = parts[2].trim();
                    String eventType = parts[3].trim();

                    cardInfoCache.put(cardId, cardName);
                    cardTypeCache.put(cardId, eventType);
                    cardExitsCache.put(cardId, exitsStr);

                    StringBuilder details = new StringBuilder();
                    details.append("ID: ").append(cardId).append("\n");
                    details.append("Название: ").append(cardName).append("\n");
                    details.append("Тип: ").append(eventType).append("\n");

                    if ("ENEMY".equals(eventType) && parts.length >= 12) {
                        String requiredDamage = parts[8];
                        String damage = parts[9];
                        String trophy = parts[10];
                        String coins = parts[11];
                        details.append("Требуется: ").append(requiredDamage).append("\n");
                        details.append("Урон: ").append(damage).append("\n");
                        details.append("Трофей: ").append(trophy).append("\n");
                        details.append("Монеты: ").append(coins).append("\n");
                    } else if ("COIN".equals(eventType) && parts.length >= 9) {
                        String coinCount = parts[8];
                        details.append("Монеты: ").append(coinCount).append("\n");
                    } else if ("ARTIFACT".equals(eventType) && parts.length >= 9) {
                        String artifactBonus = parts[8];
                        details.append("Бонус: ").append(artifactBonus).append("\n");
                    }

                    cardDetailsCache.put(cardId, details.toString());

                } catch (NumberFormatException e) {
                    System.err.println("Ошибка в строке " + line);
                }
            }
        }
    }

    private void showConnectDialog() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Подключение к серверу");
        dialog.setHeaderText("Введите данные для подключения");

        ButtonType connectButtonType = new ButtonType("Подключиться", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField hostField = new TextField("localhost");
        TextField portField = new TextField("5555");
        TextField nameField = new TextField("Игрок");

        grid.add(new Label("Хост:"), 0, 0);
        grid.add(hostField, 1, 0);
        grid.add(new Label("Порт:"), 0, 1);
        grid.add(portField, 1, 1);
        grid.add(new Label("Имя:"), 0, 2);
        grid.add(nameField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == connectButtonType) {
                Map<String, String> result = new HashMap<>();
                result.put("host", hostField.getText());
                result.put("port", portField.getText());
                result.put("name", nameField.getText());
                return result;
            }
            return null;
        });

        Optional<Map<String, String>> result = dialog.showAndWait();

        result.ifPresent(connectionData -> {
            String host = connectionData.get("host");
            String portStr = connectionData.get("port");
            username = connectionData.get("name");

            if (username == null || username.trim().isEmpty()) {
                showError("Имя не может быть пустым");
                showConnectDialog();
                return;
            }

            int port;
            try {
                port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showError("Неверный порт");
                showConnectDialog();
                return;
            }

            try {
                socket = new Socket(host, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("CONNECT|" + username);
                gameUI.addLog("Подключение как: " + username);

                Thread messageThread = new Thread(this::readServerMessages);
                messageThread.setDaemon(true);
                messageThread.start();

            } catch (IOException e) {
                showError("Не удалось подключиться: " + e.getMessage());
                showConnectDialog();
            }
        });
    }

    private void readServerMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                String finalMessage = message;
                Platform.runLater(() -> handleServerMessage(finalMessage));
            }
        } catch (IOException e) {
            Platform.runLater(() -> {
                gameUI.addLog("Соединение потеряно");
                showError("Соединение с сервером потеряно");
            });
        }
    }

    private void handleServerMessage(String message) {
        System.out.println("ПОЛУЧЕНО ОТ СЕРВЕРА: '" + message);
        String[] parts = message.split("\\|");
        if (parts.length == 0) return;

        String command = parts[0];

        switch (command) {
            case "CONNECT_OK":
                gameUI.addLog("Успешно подключен");
                break;

            case "GAME_START":
                gameUI.addLog("Игра началась!");
                gameUI.disableAllButtons();
                break;

            case "NOT_YOUR_TURN":
                if (parts.length >= 2) {
                    String currentPlayer = parts[1];
                    gameUI.handleNotYourTurn(currentPlayer);

                    if (gameUI.isSelectingExitPath()) {
                        gameUI.cancelExitPathSelection();
                    }
                }
                break;

            case "SERVER_COMMAND":
                if (parts.length >= 2) {
                    handleServerCommand(parts[1]);
                }
                break;

            case "CARD_DRAWN":
                handleCardDrawn(parts);
                break;

            case "AVAILABLE_POSITIONS":
                if (parts.length >= 2) {
                    handleAvailablePositions(parts[1]);
                }
                break;

            case "BROADCAST":
                if (parts.length >= 2) {
                    switch (parts[1]) {
                        case "CARD_PLACED":
                            String playerName = parts[2];
                            Position position = parsePosition(parts[3]);
                            int cardIdInt = Integer.parseInt(parts[4]);
                            int rotation = parts.length >= 6 ? Integer.parseInt(parts[5]) : 0;

                            String cardName = cardInfoCache.getOrDefault(cardIdInt, "Карта " + parts[4]);
                            String cardType = cardTypeCache.getOrDefault(cardIdInt, "room");
                            String exits = cardExitsCache.getOrDefault(cardIdInt, "1:1:1:1");

                            // Пропускаем свою карту!
                            if (playerName.equals(username)) {
                                System.out.println("Пропускаем свою карту");
                                return;
                            }

                            gameUI.addCardToBoard(position, cardName, cardType, exits, rotation);
                            System.out.println(cardName + " поворот: " + rotation + "°");
                            break;
                        case "CURRENT_PLAYER":
                            if (parts.length >= 2) {
                                handleCurrentPlayer(parts[1]);
                            }
                            break;
                        case "ROUND_START":
                            if (parts.length >= 2) {
                                gameUI.addLog("Раунд " + parts[1] + " начался");
                                gameUI.clearBoard();
                                if ("1".equals(parts[1])) {
                                    gameUI.clearPlayersStats();
                                }
                            }
                            break;
                    }
                }
                break;

            case "START_EXIT_PATH":
                if (parts.length >= 2) {
                    handleStartExitPath(parts[1]);
                }
                break;

            case "EXIT_CANCELLED":
                gameUI.addLog("Выход отменен. Ваш ход продолжается.");
                gameUI.cancelExitPathSelection();
                gameUI.setButtonsEnabled(true);
                break;

            case "EXIT_SUCCESS":
                if (parts.length >= 2) {
                    handleExitSuccess(parts[1]);
                }
                break;

            case "PLAYER_SLEPT":
                if (parts.length >= 2) {
                    gameUI.addLog("Игрок " + parts[1] + " уснул");
                }
                break;

            case "PLAYER_EXITED":
                if (parts.length >= 3) {
                    String playerName = parts[1];
                    String coins = parts[2];

                    if (playerName.equals(username)) {
                        gameUI.addLog("Вы вышли из подземелья с " + coins + " монетами!");
                        gameUI.disableAllButtons();
                        gameUI.cancelExitPathSelection();
                    } else {
                        gameUI.addLog("Игрок " + playerName + " вышел с " + coins + " монетами");
                    }
                }
                break;

            case "ERROR":
                if (parts.length >= 2) {
                    gameUI.addLog("Ошибка: " + parts[1]);
                }
                break;

            case "ROUND_END":
                if (parts.length >= 2) {
                    handleRoundEnd(parts[1]);
                }
                break;

            case "PLAYER_STATS":
                handlePlayerStats(parts);
                break;

            case "FINAL_SCORE":
                handleFinalScore(parts);
                break;

            case "WINNER":
                handleWinner(parts);
                break;

            case "DRAW":
                handleDraw(parts);
                break;

            case "GAME_OVER":
                handleGameOver(message.substring(9));
                break;

            case "ROUND_EXITED":
                gameUI.addLog("Вы вышли из раунда. Ожидайте его завершения.");
                gameUI.disableAllButtons();
                break;

            case "ROUND_SLEPT":
                gameUI.addLog("Вы уснули. Ожидайте завершения раунда.");
                gameUI.disableAllButtons();
                break;

            case "ROUND_RESET":
                Platform.runLater(() -> {
                    gameUI.setButtonsEnabled(false);
                    gameUI.enableRotateButton(false);
                    gameUI.enablePlaceButton(false);
                    gameUI.resetSelection();
                    gameUI.clearAvailablePositions();
                    if (gameUI.isSelectingExitPath()) {
                        gameUI.cancelExitPathSelection();
                    }
                    gameUI.addLog("Новый раунд начался. Ожидайте своего хода.");
                });
                break;

            default:
                System.out.println("Неизвестная команда: " + command);
        }
    }

    private void handleConfirmExitAction() {
        if (!gameUI.isSelectingExitPath()) {
            gameUI.addLog("Ошибка: вы не выбираете путь выхода!");
            return;
        }

        List<Position> path = gameUI.getSelectedExitPath();
        if (path.size() < 2) {
            gameUI.addLog("Ошибка: путь должен содержать минимум 2 комнаты!");
            return;
        }

        boolean isExit = gameUI.isExitPathComplete();

        if (!isExit) {
            gameUI.addLog("Ошибка: путь должен заканчиваться на выходе из подземелья!");
            return;
        }

        StringBuilder pathData = new StringBuilder();
        for (Position pos : path) {
            pathData.append(pos.getX()).append(",").append(pos.getY()).append(";");
        }
        if (pathData.length() > 0) {
            pathData.setLength(pathData.length() - 1);
        }

        gameUI.addLog("Отправка пути выхода на сервер...");
        sendAction("EXIT|" + pathData);

        gameUI.confirmExitButton.setDisable(true);
        gameUI.addLog("Путь отправлен. Ожидайте подтверждения от сервера...");
    }

    private void handleCurrentPlayer(String playerName) {
        boolean myTurn = playerName.equals(username);
        gameUI.setTurnInfo("Ходит: " + playerName);

        if (myTurn) {
            gameUI.addLog("Ваш ход!");
            gameUI.setButtonsEnabled(true);
            gameUI.enableRotateButton(false);
            gameUI.enablePlaceButton(false);
            gameUI.resetSelection();
        } else {
            gameUI.disableAllButtons();
            gameUI.resetSelection();
        }
    }

    private void handleServerCommand(String subCommand) {
        if ("YOUR_TURN".equals(subCommand)) {
            gameUI.setTurnInfo("Ваш ход! Выберите действие");
            gameUI.setButtonsEnabled(true);
            gameUI.enableRotateButton(false);
            gameUI.enablePlaceButton(false);
            gameUI.addLog("Сейчас ваш ход!");
        } else if ("PLACE_CARD_UI".equals(subCommand)) {
            gameUI.addLog("Разместите карту на поле");
            gameUI.enableRotateButton(true);
        }
    }

    private void handleCardDrawn(String[] parts) {
        if (parts.length >= 3) {
            currentCardId = Integer.parseInt(parts[1]);
            String positionsStr = parts[2];

            String cardName = cardInfoCache.getOrDefault(currentCardId, "Карта " + currentCardId);
            String cardType = cardTypeCache.getOrDefault(currentCardId, "UNKNOWN");
            String exits = cardExitsCache.getOrDefault(currentCardId, "1:1:1:1");

            gameUI.setCurrentCard(currentCardId, cardName, exits);

            List<Position> positions = parsePositions(positionsStr);
            gameUI.setAvailablePositions(positions);

            gameUI.enableRotateButton(true);
            gameUI.exploreButton.setDisable(true);
            gameUI.sleepButton.setDisable(true);
            gameUI.exitButton.setDisable(true);
        }
    }

    private List<Position> parsePositions(String positionsStr) {
        List<Position> positions = new ArrayList<>();
        if (positionsStr == null || positionsStr.isEmpty()) return positions;

        String[] posArray = positionsStr.split(";");
        for (String pos : posArray) {
            if (!pos.isEmpty()) {
                String[] coords = pos.split(",");
                if (coords.length == 2) {
                    try {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        positions.add(new Position(x, y));
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка парсинга позиции: " + pos);
                    }
                }
            }
        }
        return positions;
    }

    private void handleAvailablePositions(String positionsStr) {
        List<Position> positions = parsePositions(positionsStr);
        gameUI.setAvailablePositions(positions);
        gameUI.enablePlaceButton(!positions.isEmpty());
    }

    private void handleStartExitPath(String exitsStr) {
        List<Position> exits = parsePositions(exitsStr);

        if (exits.isEmpty()) {
            gameUI.addLog("Нет доступных выходов из подземелья!");
            return;
        }

        gameUI.startExitPathSelection(exits);
        gameUI.addLog("Доступно выходов: " + exits.size());

        for (Position exit : exits) {
            gameUI.addLog("  Выход на позиции: [" + exit.getX() + ", " + exit.getY() + "]");
        }
    }

    private void handleExitSuccess(String coins) {
        gameUI.addLog("Вы успешно вышли из подземелья!");
        gameUI.addLog("Получено монет: " + coins);

        gameUI.disableAllButtons();
        gameUI.cancelExitPathSelection();

        gameUI.confirmExitButton.setVisible(false);
        gameUI.confirmExitButton.setDisable(true);

        gameUI.addLog("Вы вышли из раунда. Ожидайте его завершения...");
    }

    private void handleRoundEnd(String roundNumber) {
        gameUI.addLog("Раунд " + roundNumber + " завершен!");
        gameUI.disableAllButtons();
    }

    private void handlePlayerStats(String[] parts) {
        if (parts.length >= 6) {
            String playerName = parts[1];
            String dogName = parts[2];
            int coins = Integer.parseInt(parts[3]);
            int health = Integer.parseInt(parts[4]);
            int tokens = Integer.parseInt(parts[5]);

            String stats = String.format("%s (%s): %d монет, %d HP, %d токенов",
                    playerName, dogName, coins, health, tokens);

            gameUI.addPlayerStats(stats);
            gameUI.addLog("Статы " + playerName + ": HP=" + health + ", T=" + tokens + ", C=" + coins);
        }
    }

    private void handleFinalScore(String[] parts) {
        if (parts.length >= 4) {
            int place = Integer.parseInt(parts[1]);
            String playerName = parts[2];
            int coins = Integer.parseInt(parts[3]);

            String placeStr;
            switch (place) {
                case 1:
                    placeStr = "🥇 1 место";
                    break;
                case 2:
                    placeStr = "🥈 2 место";
                    break;
                case 3:
                    placeStr = "🥉 3 место";
                    break;
                default:
                    placeStr = place + " место";
            }

            String result = placeStr + ": " + playerName + " - " + coins + " монет";
            gameUI.addLog(result);
        }
    }

    private void handleWinner(String[] parts) {
        if (parts.length >= 3) {
            String winner = parts[1];
            int coins = Integer.parseInt(parts[2]);
            gameUI.addLog("🏆 ПОБЕДИТЕЛЬ: " + winner + " с " + coins + " монетами");
        }
    }

    private void handleDraw(String[] parts) {
        if (parts.length >= 2) {
            int coins = Integer.parseInt(parts[1]);
            gameUI.addLog("🤝 НИЧЬЯ! Победители набрали: " + coins + " монет");
        }
    }

    private void handleGameOver(String message) {
        gameUI.addLog("ИГРА ЗАВЕРШЕНА!");
        gameUI.addLog(message);
        gameUI.disableAllButtons();
    }

    private Position parsePosition(String posStr) {
        String[] coords = posStr.split(",");
        if (coords.length == 2) {
            try {
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                return new Position(x, y);
            } catch (NumberFormatException e) {
                return new Position(0, 0);
            }
        }
        return new Position(0, 0);
    }

    private void handleExitAction() {
        if (gameUI.isSelectingExitPath()) {
            sendAction("CANCEL_EXIT");
            gameUI.cancelExitPathSelection();
            gameUI.addLog("Выход отменен");
        } else {
            sendAction("REQUEST_EXIT");
            gameUI.addLog("Запрос на выход отправлен. Ожидайте список доступных выходов...");
        }
    }

    private void handlePlaceAction() {
        int[] cell = gameUI.getSelectedCell();
        if (cell[0] >= -1000 && cell[1] >= -1000) {

            int rotation = gameUI.getCurrentCardRotation();
            String cardName = cardInfoCache.getOrDefault(currentCardId, "Карта #" + currentCardId);
            String cardType = cardTypeCache.getOrDefault(currentCardId, "LOCAL");
            String exits = cardExitsCache.getOrDefault(currentCardId, "1:1:1:1");

            Position pos = new Position(cell[0], cell[1]);
            gameUI.addCardToBoard(pos, cardName, cardType, exits, rotation);

            gameUI.setCurrentCard("");  // Очищаем руку
            sendAction("PLACE|" + cell[0] + "," + cell[1] + "|" + gameUI.getCurrentCardRotation());
            gameUI.clearSelection();
        }
    }

    private void sendAction(String action) {
        if (out != null) {
            String fullAction = "ACTION|" + action;
            out.println(fullAction);
            gameUI.addLog("Отправлено: " + action);
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @Override
    public void stop() {
        try {
            if (out != null) out.println("DISCONNECT|" + username);
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }
}