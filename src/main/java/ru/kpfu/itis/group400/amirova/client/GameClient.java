package ru.kpfu.itis.group400.amirova.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.kpfu.itis.group400.amirova.client.ui.GameUI;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class GameClient extends Application {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameUI gameUI;
    private Player player;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        gameUI = new GameUI();

        gameUI.exploreButton.setOnAction(e -> sendAction("EXPLORE"));
        gameUI.exitButton.setOnAction(e -> sendAction("EXIT"));
        gameUI.sleepButton.setOnAction(e -> sendAction("SLEEP"));
        gameUI.rotateButton.setOnAction(e -> sendAction("ROTATE"));
        gameUI.placeButton.setOnAction(e -> {
            int[] cell = gameUI.getSelectedCell();
            if (cell[0] >= 0 && cell[1] >= 0) {
                sendAction("PLACE|" + cell[0] + "," + cell[1]);
                gameUI.clearSelection();
            }
        });

        Scene scene = new Scene(gameUI.getRoot(), 1200, 800);
        stage.setTitle("Подземелья и Пёсики");
        stage.setScene(scene);
        stage.show();

        showConnectDialog();
    }

    private void showConnectDialog() {
        TextInputDialog dialog = new TextInputDialog("Игрок");
        dialog.setTitle("Подключение");
        dialog.setContentText("Введите имя:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            player = new Player(name);
            gameUI.setPlayerName(name);
            connectToServer(name);
        });
    }

    private void connectToServer(String name) {
        try {
            socket = new Socket("127.0.0.1", 5555);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("CONNECT|" + name);
            gameUI.addLog("Подключение к серверу...");

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        handleServerMessage(message);
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        gameUI.addLog("Соединение потеряно");
                    });
                }
            }).start();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Не удалось подключиться к серверу");
            alert.show();
        }
    }

    private void handleServerMessage(String message) {
        Platform.runLater(() -> {
            System.out.println("Сервер: " + message);
            gameUI.addLog("Сервер: " + message);

            String[] parts = message.split("\\|");
            String command = parts[0];

            switch (command) {
                case "GAME_START":
                    gameUI.addLog("Игра началась!");
                    gameUI.setTurnInfo("Ожидание хода...");
                    break;

                case "ROUND_START":
                    int round = Integer.parseInt(parts[1]);
                    gameUI.addLog("=== Раунд " + round + " начался ===");
                    break;

                case "CURRENT_PLAYER":
                    String currentPlayer = parts[1];
                    gameUI.setTurnInfo("Ходит: " + currentPlayer);

                    boolean myTurn = currentPlayer.equals(player.getUsername());
                    gameUI.setButtonsEnabled(myTurn);

                    if (myTurn) {
                        gameUI.addLog("Сейчас ваш ход!");
                    }
                    break;

                case "YOUR_TURN":
                    gameUI.setTurnInfo("Ваш ход!");
                    gameUI.setButtonsEnabled(true);
                    gameUI.enableRotateButton(false);
                    gameUI.enablePlaceButton(false);
                    break;

                case "NOT_YOUR_TURN":
                    gameUI.setTurnInfo("Ходит: " + parts[1]);
                    gameUI.setButtonsEnabled(false);
                    break;

                case "CARD_DRAWN":
                    gameUI.addLog("Вы взяли карту");
                    gameUI.enableRotateButton(true);
                    gameUI.enablePlaceButton(false);
                    break;

                case "AVAILABLE_POSITIONS":
                    String positions = parts[1];
                    gameUI.addLog("Доступные позиции: " + positions);
                    gameUI.enablePlaceButton(true);
                    break;

                case "ERROR":
                    String errorMsg = parts[1];
                    gameUI.addLog("Ошибка: " + errorMsg);
                    Alert alert = new Alert(Alert.AlertType.ERROR, errorMsg);
                    alert.show();
                    break;

                case "BROADCAST":
                    String broadcastMsg = parts[1];
                    gameUI.addLog("Оповещение: " + broadcastMsg);
                    break;

                case "PLAYER_SLEPT":
                    String sleeper = parts[1];
                    gameUI.addLog("Игрок " + sleeper + " лег спать");
                    break;

                case "PLAYER_EXITED":
                    String exiter = parts[1];
                    int coins = Integer.parseInt(parts[2]);
                    gameUI.addLog("Игрок " + exiter + " вышел с " + coins + " монетами");
                    break;

                case "CARD_PLACED":
                    String placer = parts[1];
                    String pos = parts[2];
                    gameUI.addLog("Игрок " + placer + " поставил карту на " + pos);
                    gameUI.updateBoard();
                    gameUI.enableRotateButton(false);
                    gameUI.enablePlaceButton(false);
                    break;

                case "GAME_END":
                    String winner = parts[1];
                    int winnerCoins = Integer.parseInt(parts[2]);
                    gameUI.addLog("=== Игра окончена! ===");
                    gameUI.addLog("Победитель: " + winner + " с " + winnerCoins + " монетами");
                    gameUI.setButtonsEnabled(false);
                    break;
            }
        });
    }

    private void sendAction(String action) {
        if (out != null) {
            out.println("ACTION|" + action);
            gameUI.addLog("Отправил: " + action);
        }
    }

    @Override
    public void stop() {
        try {
            if (out != null) out.println("DISCONNECT|" + player.getUsername());
            if (socket != null) socket.close();
        } catch (IOException e) {}
    }
}