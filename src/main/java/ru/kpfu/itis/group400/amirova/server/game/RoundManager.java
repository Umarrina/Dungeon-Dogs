package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.GameSender;
import ru.kpfu.itis.group400.amirova.server.GameSerializer;
import ru.kpfu.itis.group400.amirova.server.MessageHandler;
import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoundManager {
    private List<Player> players;
    private DeckRooms deckRooms;
    private GameSender notifier;
    private MessageHandler handler;
    private GameEngine gameEngine;
    private GameSerializer gameSerializer;
    private GameState gameState;

    public RoundManager(DeckRooms deckRooms, List<Player> players, GameEngine gameEngine, MessageHandler handler, GameSender notifier) {
        this.deckRooms = deckRooms;
        this.players = players;
        this.gameEngine = gameEngine;
        this.handler = handler;
        this.notifier = notifier;
        this.gameSerializer = new GameSerializer();
        this.gameState = gameEngine.getGameState();
    }

    public void startGame() {
        setStartParameter();
        new Thread(() -> {
            try {
                deckRooms.shuffle();
                for (int i = 1; i <= 3; i++) {
                    startRound(i);
                    gameEngine.processEndOfRound(i);
                }
                gameEngine.calculateFinalScore();
                notifier.broadcast("GAME_OVER|Игра завершена!");
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
            state.setCurrentRoomId(null);
        }
    }

    private void startRound(int round) throws InterruptedException {
        notifier.broadcast("ROUND_START|" + round);

        for (Player player : players) {
            notifier.sendToPlayer(player, "SERVER_COMMAND|ROUND_RESET");
        }

        for (Player player : players) {
            PlayerRoundState state = player.getPlayerRoundState();
            state.setHasExit(false);
            state.setSlept(false);

            state.setCurrentTokens(player.getMaxTokens());

            state.setCurrentHealth(player.getDog().getMaxHealth());
            state.getExitPathCardIds().clear();
            player.setCurrentRoomId(null);
        }

        prepareBoardForNewRound();

        gameState.resetDeckLimit();

        while (countActivePlayers() > 0) {
            for (Player player : players) {
                PlayerRoundState state = player.getPlayerRoundState();

                // Пропускаем игроков, которые вышли или спят
                if (state.isSlept() || state.isHasExit()) {
                    System.out.println("Пропускаем игрока " + player.getUsername() +
                            " (спит: " + state.isSlept() +
                            ", вышел: " + state.isHasExit() + ")");
                    continue;
                }

                System.out.println("=== ХОД ИГРОКА: " + player.getUsername() + " ===");

                notifier.notifyCurrentPlayer(player);
                notifier.sendActionRequest(player, "YOUR_TURN");

                for (Player p : players) {
                    if (!p.equals(player)) {
                        notifier.sendToPlayer(p, "NOT_YOUR_TURN|" + player.getUsername());
                    }
                }

                // Ждем первое действие игрока
                String response = handler.waitResponse(player);

                if (response != null) {
                    System.out.println("Получено от " + player.getUsername() + ": " + response);
                    handlePlayerAction(player, response);

                    // После обработки действия проверяем, не вышел ли игрок или не уснул
                    if (state.isHasExit() || state.isSlept()) {
                        System.out.println("Игрок " + player.getUsername() + " завершил ход");
                        continue; // Переходим к следующему игроку
                    }

                    // Если игрок все еще активен, продолжаем ждать от него действия
                    // (это важно для многошаговых действий)
                    System.out.println("Игрок " + player.getUsername() + " все еще в игре, продолжаем ход");
                }

                if (countActivePlayers() == 0) {
                    break;
                }
            }
        }

        notifier.broadcast("ROUND_END|" + round);

        for (Player p : players) {
            notifier.sendToPlayer(p, "PLAYER_STATS|" +
                    p.getUsername() + "|" +
                    p.getDog().getName() + "|" +
                    p.getTotalCoins() + "|" +
                    p.getPlayerRoundState().getCurrentHealth() + "|" +
                    p.getPlayerRoundState().getCurrentTokens());
        }

        if (round == 3) {
            calculateFinalScore();
        }
    }

    private void prepareBoardForNewRound() {
       gameState.clearBoardExceptStart();
    }

    private void handlePlayerAction(Player player, String response) throws InterruptedException {
        String[] parts = response.split("\\|");
        if (parts.length < 2 || !parts[0].equals("ACTION")) {
            notifier.sendError(player, "Неверный формат действия");
            return;
        }

        String action = parts[1];

        switch (action) {
            case "SLEEP":
                handleSleep(player);
                break;

            case "EXPLORE":
                handleExplore(player);
                break;

            case "REQUEST_EXIT":
                handleRequestExit(player);
                return;

            case "EXIT":
                if (parts.length < 3) {
                    notifier.sendError(player, "Не указан путь выхода");
                    break;
                }
                handleExit(player, parts[2]);
                break;

            case "ROTATE":
                handleRotate(player);
                break;

            case "PLACE":
                if (parts.length < 3) {
                    notifier.sendError(player, "Не указана позиция");
                    break;
                }
                handlePlace(player, parts[2]);
                break;

            default:
                notifier.sendError(player, "Неизвестное действие: " + action);
        }
    }

    private void handleSleep(Player player) {
        if (player.hasCard()) {
            notifier.sendError(player, "Нельзя спать с картой на руке");
            return;
        }

        PlayerRoundState state = player.getPlayerRoundState();
        if (state.isHasExit() || state.isSlept()) {
            notifier.sendError(player, "Игрок уже вышел или спит");
            return;
        }

        state.setSlept(true);
        player.addMaxTokens(1);
        state.setCurrentTokens(state.getCurrentTokens() + 1);
        notifier.notifyPlayerSleep(player);
        System.out.println("Игрок " + player.getUsername() + " уснул");
    }

    private void handleExplore(Player player) throws InterruptedException {
        if (player.hasCard()) {
            notifier.sendError(player, "У вас уже есть карта на руке. Разместите ее сначала.");
            return;
        }

        boolean placed = false;
        gameEngine.dealCardToPlayer(player);
        notifier.sendActionRequest(player, "PLACE_CARD_UI");

        while (!placed && player.hasCard()) {
            String response = handler.waitResponse(player);

            if (response == null || !response.startsWith("ACTION|")) {
                continue;
            }

            String[] parts = response.split("\\|");
            String action = parts[1];

            if (action.equals("PLACE") && parts.length > 2) {
                String posData = parts[2];
                Position position = gameSerializer.deserializePositions(posData);

                if (gameEngine.canPlace(player, position)) {
                    gameEngine.processPlaceCard(player, position);
                    placed = true;
                    break;
                } else {
                    notifier.sendError(player, "Неверная позиция для размещения");
                }
            } else if (action.equals("ROTATE")) {
                gameEngine.processRotateCard(player);
            } else if (action.equals("EXPLORE")) {
                notifier.sendError(player, "Закончите размещение текущей карты");
            }
        }

        if (!placed && player.hasCard()) {
            player.setCurrentRoomId(null);
        }
    }

    private void handleRequestExit(Player player) throws InterruptedException {
        if (player.hasCard()) {
            notifier.sendError(player, "Нельзя выходить с картой на руке");
            return;
        }

        PlayerRoundState state = player.getPlayerRoundState();
        if (state.isHasExit() || state.isSlept()) {
            notifier.sendError(player, "Игрок уже вышел или спит");
            return;
        }

        List<Position> exits = gameEngine.getAvailableExits(player);
        if (exits.isEmpty()) {
            notifier.sendError(player, "Нет доступных выходов из подземелья");
        } else {
            notifier.sendToPlayer(player, "START_EXIT_PATH|" +
                    exits.stream()
                            .map(p -> p.getX() + "," + p.getY())
                            .collect(Collectors.joining(";")));

            boolean exitHandled = false;
            while (!exitHandled) {
                String response = handler.waitResponse(player);
                System.out.println("Ожидание пути выхода от " + player.getUsername() + ": " + response);

                if (response == null || !response.startsWith("ACTION|")) {
                    continue;
                }

                String[] exitParts = response.split("\\|");
                String exitAction = exitParts[1];

                if ("EXIT".equals(exitAction)) {
                    String pathData = exitParts.length > 2 ? exitParts[2] : "";

                    if (gameEngine.processExit(player, parsePathData(pathData))) {
                        int coins = player.getTotalCoins();
                        notifier.sendToPlayer(player, "EXIT_SUCCESS|" + coins);
                        notifier.notifyPlayerExit(player, coins);
                        exitHandled = true;
                    } else {
                        notifier.sendError(player, "Неверный путь для выхода");
                    }
                } else if ("CANCEL_EXIT".equals(exitAction)) {
                    notifier.sendToPlayer(player, "EXIT_CANCELLED");
                    notifier.sendToPlayer(player, "SERVER_COMMAND|YOUR_TURN");
                    exitHandled = true;
                } else {
                    notifier.sendError(player, "Закончите выбор пути выхода или отмените его");
                }
            }
        }
    }

    private void handleExit(Player player, String pathData) {
        List<Position> path = parsePathData(pathData);

        if (gameEngine.processExit(player, path)) {
            int coins = player.getTotalCoins();
            notifier.sendToPlayer(player, "EXIT_SUCCESS|" + coins);
            notifier.notifyPlayerExit(player, coins);
        } else {
            notifier.sendError(player, "Неверный путь для выхода");
        }
    }

    private void handleRotate(Player player) {
        if (!player.hasCard()) {
            notifier.sendError(player, "Нет карты для поворота");
            return;
        }
        gameEngine.processRotateCard(player);
    }

    private void handlePlace(Player player, String posData) {
        if (!player.hasCard()) {
            notifier.sendError(player, "Нет карты для размещения");
            return;
        }

        String[] placeData = posData.split("\\|");
        String posStr = placeData[0];  // "1,0"
        int rotation = placeData.length > 1 ? Integer.parseInt(placeData[1]) : 0;  // 90!

        Position position = gameSerializer.deserializePositions(posStr);
        if (gameEngine.canPlace(player, position)) {
            gameEngine.processPlaceCard(player, position);

            String broadcastMsg = "BROADCAST|CARD_PLACED|" + player.getUsername() + "|" + posStr + "|" + player.getCurrentRoomId() + "|" + rotation;
            notifier.broadcast(broadcastMsg);
        } else {
            notifier.sendError(player, "Неверная позиция для размещения");
        }
    }



    private List<Position> parsePathData(String pathData) {
        List<Position> path = new ArrayList<>();
        if (pathData == null || pathData.isEmpty()) return path;

        String[] positions = pathData.split(";");
        for (String posStr : positions) {
            if (!posStr.isEmpty()) {
                String[] coords = posStr.split(",");
                if (coords.length == 2) {
                    try {
                        int x = Integer.parseInt(coords[0].trim());
                        int y = Integer.parseInt(coords[1].trim());
                        path.add(new Position(x, y));
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка парсинга позиции: " + posStr);
                    }
                }
            }
        }
        return path;
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

    public void calculateFinalScore() {
        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalCoins(), p1.getTotalCoins()));

        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append("══════════════════════════════\n");
        resultMessage.append("  ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ\n");
        resultMessage.append("══════════════════════════════\n");

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            int coins = player.getTotalCoins();
            String place = getPlaceEmoji(i);

            String playerResult = String.format("%s %s (%s): %d монет",
                    place, player.getUsername(), player.getDog().getName(), coins);

            resultMessage.append(playerResult).append("\n");

            notifier.sendToPlayer(player, "FINAL_SCORE|" +
                    (i + 1) + "|" + player.getUsername() + "|" + coins);
        }

        if (sortedPlayers.size() > 0) {
            int maxCoins = sortedPlayers.get(0).getTotalCoins();
            List<Player> winners = sortedPlayers.stream()
                    .filter(p -> p.getTotalCoins() == maxCoins)
                    .collect(Collectors.toList());

            if (winners.size() == 1) {
                Player winner = winners.get(0);
                String winMessage = "🏆 ПОБЕДИТЕЛЬ: " + winner.getUsername() +
                        " (" + winner.getDog().getName() + ") с " + maxCoins + " монетами!";
                resultMessage.append("══════════════════════════════\n");
                resultMessage.append(winMessage);
                notifier.broadcast("WINNER|" + winner.getUsername() + "|" + maxCoins);
            } else {
                String winMessage = "🤝 НИЧЬЯ! Победители: " +
                        winners.stream()
                                .map(p -> p.getUsername() + " (" + p.getDog().getName() + ")")
                                .collect(Collectors.joining(", ")) +
                        " с " + maxCoins + " монетами";
                resultMessage.append("══════════════════════════════\n");
                resultMessage.append(winMessage);
                notifier.broadcast("DRAW|" + maxCoins);
            }
        }

        notifier.broadcast("GAME_OVER|" + resultMessage);
    }

    private String getPlaceEmoji(int place) {
        switch (place) {
            case 0: return "🥇";
            case 1: return "🥈";
            case 2: return "🥉";
            default: return "  ";
        }
    }
}