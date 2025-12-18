package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.GameSender;
import ru.kpfu.itis.group400.amirova.server.GameSerializer;
import ru.kpfu.itis.group400.amirova.server.MessageHandler;
import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.ArrayList;
import java.util.List;

public class RoundManager {
    private List<Player> players;
    private int currentRound;
    private DeckRooms deckRooms;
    private GameSender notifier;
    private MessageHandler handler;
    private GameEngine gameEngine;
    private GameSerializer gameSerializer;

    // TODO добавь потом еще зелья и квесты

    public RoundManager(DeckRooms deckRooms, List<Player> players, GameEngine gameEngine, MessageHandler handler, GameSender notifier) {
        this.deckRooms = deckRooms;
        this.players = players;
        this.gameEngine = gameEngine;
        this.handler = handler;
        this.notifier = notifier;
    }

    public void startGame() {
        setStartParameter();
        new Thread(() -> {
            try {
                for (int i = 1; i <= 3; i++) {
                    startRound(i);
                    gameEngine.processEndOfRound(i);
                }
                gameEngine.calculateFinalScore();
                notifier.broadcast("Игра завершена. Итоги...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void setStartParameter() {
        for (Player player : players) {
            PlayerRoundState state = player.getPlayerRoundState();
            state.setHasExit(false);
            state.setSlept(false);
            state.setExitPath(new ArrayList<>());
            state.setCurrentRoom(null);
        }
    }

    public void startRound(int round) throws InterruptedException {
        currentRound = round;
        notifier.notifyRoundStart(round);
        setRoundParameter();

        while (countActivePlayers() > 0) {
            for (Player player : players) {
                PlayerRoundState state = player.getPlayerRoundState();

                if (state.isSlept() || state.isHasExit()) {
                    continue;
                }

                notifier.notifyCurrentPlayer(player);

                notifier.sendActionRequest(player, "YOUR_TURN");

                for (Player p : players) {
                    if (!p.equals(player)) {
                        notifier.sendToPlayer(p, "NOT_YOUR_TURN|" + player.getUsername());
                    }
                }

                String response = handler.waitResponse(player);

                handlePlayerAction(player, response);
            }
        }
    }

    private void setRoundParameter() {
        for (Player player : players) {
            PlayerRoundState state = player.getPlayerRoundState();
            state.setCurrentTokens(player.getMaxTokens());
            state.setCurrentHealth(player.getDog().getMaxHealth());
        }
    }

    private void handlePlayerAction(Player player, String response) throws InterruptedException {
        String[] parts =  response.split("\\|");
        String action = parts[1];

        if (parts.length < 2 || !parts[0].equals("ACTION")) {
            notifier.sendError(player, "Неверный формат действия");
            return;
        }

        switch (action) {
            case "SLEEP":
                gameEngine.processSleep(player);
                break;
            case "EXPLORE":
                handleExploreSequence(player);
                break;
            case "EXIT":
                String pathData = parts.length > 2 ? parts[2] : "";
                handleExitSequence(player, pathData);
                break;
            default:
                notifier.sendError(player, "Неверное действие.");
        }
    }

    private void handleExploreSequence(Player player) throws InterruptedException {
        boolean placed = false;
        gameEngine.dealCardToPlayer(player);

        while (!placed) {
            notifier.sendActionRequest(player, "PLACE_CARD_UI");

            String response = handler.waitResponse(player);

            if (response.equals("PLACE")) {
                String posData = response.split("\\|")[2];
                Position position = gameSerializer.deserializePositions(posData);
                if (gameEngine.canPlace(player, position)) {
                    gameEngine.processPlaceCard(player, position);
                    placed = true;
                } else {
                    notifier.sendError(player, "Неверная позиция, попробуйте еще раз.");
                }
            } else if (response.contains("ROTATE")) {
                gameEngine.processRotateCard(player);
            }
        }
    }

    private void handleExitSequence(Player player, String data) throws InterruptedException {
        if (data.isEmpty()) {
            notifier.sendActionRequest(player, "REQUEST_EXIT_PATH");
            String fullResponse = handler.waitResponse(player);
            handleExitSequence(player, fullResponse.split("\\|")[2]);
            return;
        }

        List<Position> path = gameSerializer.deserializePath(data);

        if (gameEngine.processExit(player, path)) {
            notifier.sendActionRequest(player, "EXIT_SUCCESS");
        } else {
            notifier.sendError(player, "Путь невалиден или не хватает ресурсов. Выберите другой.");
            handleExitSequence(player, "");
        }
    }

    private int countActivePlayers() {
        int count = 0;
        for (Player player : players) {
            if (!player.getPlayerRoundState().isSlept() && !player.getPlayerRoundState().isHasExit()) {
                count++;
            }
        }
        return count;
    }
}
