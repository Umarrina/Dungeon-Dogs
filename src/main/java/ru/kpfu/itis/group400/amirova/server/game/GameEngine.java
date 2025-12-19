package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.GameSender;
import ru.kpfu.itis.group400.amirova.server.GameServer;
import ru.kpfu.itis.group400.amirova.server.game.model.GameSetupResult;
import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.CoinRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.EnemyRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GameEngine {
    private GameState gameState;
    private GameSetupManager gameSetupManager;
    private GameSender notifier;

    public GameEngine(List<Player> players, GameServer server) {
        this.notifier = new GameSender(server);
        gameSetupManager = new GameSetupManager(players);
        GameSetupResult gameSetupResult = gameSetupManager.setupGame();
        gameState = new GameState(players, gameSetupResult.getStartRoom(), gameSetupResult.getDeckRooms());

    }

    public void dealCardToPlayer(Player player) {
        DeckRooms deckRooms = gameState.getDeckRooms();
        if (!deckRooms.hasNextCard()) {
            notifier.sendError(player, "Колода пуста");
            return;
        }

        Room card = deckRooms.getFirstCard();

        if (card.getId() == 0) {
            System.out.println("ПРЕДУПРЕЖДЕНИЕ: Попытка выдать стартовую комнату!");
            if (deckRooms.hasNextCard()) {
                card = deckRooms.getFirstCard();
            } else {
                notifier.sendError(player, "Колода пуста");
                return;
            }
        }

        player.setCurrentRoomId(card.getId());
        List<Position> availablePositions = gameState.getAvailablePositions(card.getId(), deckRooms);
        notifier.notifyCardDrawn(player, card.getId(), availablePositions);
    }


    public void processRotateCard(Player player) {
        DeckRooms deckRooms = gameState.getDeckRooms();
        Integer cardId = player.getCurrentRoomId();
        if (cardId == null) {
            notifier.sendError(player, "Нет карты для поворота");
            return;
        }

        Room card = deckRooms.getCardById(cardId);
        if (card == null) {
            notifier.sendError(player, "Карта не найдена");
            return;
        }

        card.rotate();

        List<Position> availablePositions = gameState.getAvailablePositions(card, deckRooms);

        notifier.sendAvailablePositions(player, availablePositions);
    }

    public boolean canPlace(Player player, Position position) {
        DeckRooms deckRooms = gameState.getDeckRooms();
        Integer cardId = player.getCurrentRoomId();
        if (cardId == null) return false;

        Room card = deckRooms.getCardById(cardId);
        if (card == null) return false;

        List<Position> availablePositions = gameState.getAvailablePositions(card, deckRooms);

        return availablePositions.contains(position);
    }

    public void processPlaceCard(Player player, Position position) {
        DeckRooms deckRooms = gameState.getDeckRooms();
        Integer cardId = player.getCurrentRoomId();
        if (cardId == null) {
            notifier.sendError(player, "Нет карты для размещения");
            return;
        }

        Room card = deckRooms.getCardById(cardId);
        if (card == null) {
            notifier.sendError(player, "Карта не найдена");
            return;
        }

        if (!canPlace(player, position)) {
            notifier.sendError(player, "Нельзя разместить здесь");
            return;
        }

        card.setPositionX(position.getX());
        card.setPositionY(position.getY());
        gameState.addCardToBoard(cardId, position);

        player.setCurrentRoomId(null);

        notifier.notifyCardPlaced(player, cardId, position);
    }

    public boolean processExit(Player player, List<Position> path) {
        PlayerRoundState state = player.getPlayerRoundState();

        if (state.isHasExit() || state.isSlept()) {
            notifier.sendError(player, "Игрок уже вышел или спит");
            return false;
        }

        if (player.hasCard()) {
            notifier.sendError(player, "На руке есть карта");
            return false;
        }

        if (!isValidExitPath(path)) {
            notifier.sendError(player, "Неверный путь");
            return false;
        }

        int totalTokensNeeded = 0;
        List<Integer> collectedCardIds = new ArrayList<>();

        for (Position pos : path) {
            Integer cardId = gameState.getCardIdAtPosition(pos);
            if (cardId != null && cardId != 0) {
                totalTokensNeeded++;
                collectedCardIds.add(cardId);
            }
        }

        if (state.getCurrentTokens() < totalTokensNeeded) {
            notifier.sendError(player, "Недостаточно токенов. Нужно: " + totalTokensNeeded + ", есть: " + state.getCurrentTokens());
            return false;
        }

        int coins = collectCoinsFromPath(path);
        player.initTotalCoins(coins);

        for (Integer cardId : collectedCardIds) {
            state.addToExitPath(cardId);
        }

        state.setCurrentTokens(state.getCurrentTokens() - totalTokensNeeded);
        if (gameState.getCardsAvailable() == Integer.MAX_VALUE) {
            int remainingPlayers = (int) gameState.getPlayers().stream()
                    .filter(p -> !p.getPlayerRoundState().isHasExit() &&
                            !p.getPlayerRoundState().isSlept())
                    .count();
            gameState.setCardsAvailable(remainingPlayers * 4);
            notifier.broadcast("Лимит карт: " + (remainingPlayers * 4));
        }

        state.setHasExit(true);
        notifier.notifyPlayerExit(player, coins);
        return true;
    }

    private boolean isValidExitPath(List<Position> path) {
        if (path.isEmpty()) return false;

        if (!path.get(0).equals(new Position(0, 0))) {
            return false;
        }

        for (int i = 1; i < path.size(); i++) {
            if (!arePositionsAdjacent(path.get(i-1), path.get(i))) {
                return false;
            }
        }

        return true;
    }

    public List<Position> getAvailableExits(Player player) {
        return gameState.getExitPositions();
    }

    private boolean arePositionsAdjacent(Position pos1, Position pos2) {
        int dx = Math.abs(pos1.getX() - pos2.getX());
        int dy = Math.abs(pos1.getY() - pos2.getY());
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    public void processEndOfRound(int round) {
        for (Player p : gameState.getPlayers()) {
            PlayerRoundState state = p.getPlayerRoundState();
            state.setHasExit(false);
            state.setSlept(false);
            state.setCurrentTokens(p.getMaxTokens());
            state.setCurrentHealth(p.getDog().getMaxHealth());
            state.getExitPathCardIds().clear();
        }

        gameState.resetDeckLimit();
        notifier.broadcast("Раунд " + round + " завершен!");
    }

    public void calculateFinalScore() {
        List<Player> players = gameState.getPlayers();
        DeckRooms deckRooms = gameState.getDeckRooms();

        Map<Player, Integer> finalScores = new HashMap<>();

        for (Player player : players) {
            int score = player.getTotalCoins();
            finalScores.put(player, score);
        }

        calculateEnemyBonuses(players, deckRooms, finalScores);
        List<Player> sortedPlayers = sortPlayersByFinalScore(players, finalScores);
        sendFinalResults(sortedPlayers, finalScores);
    }

    private void calculateEnemyBonuses(List<Player> players, DeckRooms deckRooms, Map<Player, Integer> finalScores) {

        for (Player player : players) {
            PlayerRoundState state = player.getPlayerRoundState();
            int enemyBonus = 0;

            if (state != null && state.getExitPathCardIds() != null) {
                for (Integer cardId : state.getExitPathCardIds()) {
                    Room room = deckRooms.getCardById(cardId);
                    if (room != null && room.getEventType() == EventType.ENEMY) {
                        EnemyRoom enemy = (EnemyRoom) room;
                        enemyBonus += enemy.getCoins();
                    }
                }
            }
            if (enemyBonus > 0) {
                int currentScore = finalScores.get(player);
                finalScores.put(player, currentScore + enemyBonus);
            }
        }
    }

    private List<Player> sortPlayersByFinalScore(List<Player> players, Map<Player, Integer> finalScores) {
        return players.stream()
                .sorted((p1, p2) -> {
                    int score1 = finalScores.getOrDefault(p1, 0);
                    int score2 = finalScores.getOrDefault(p2, 0);
                    return Integer.compare(score2, score1);
                })
                .collect(Collectors.toList());
    }

    private void sendFinalResults(List<Player> sortedPlayers,
                                  Map<Player, Integer> finalScores) {
        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append("══════════════════════════════\n");
        resultMessage.append("  ФИНАЛЬНЫЕ РЕЗУЛЬТАТЫ\n");
        resultMessage.append("══════════════════════════════\n");

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player player = sortedPlayers.get(i);
            int score = finalScores.get(player);

            String placeEmoji;
            switch (i) {
                case 0: placeEmoji = "🥇"; break;
                case 1: placeEmoji = "🥈"; break;
                case 2: placeEmoji = "🥉"; break;
                default: placeEmoji = "  ";
            }

            String playerResult = String.format("%s %s (%s): %d монет",
                    placeEmoji, player.getUsername(),
                    player.getDog() != null ? player.getDog().getName() : "Без пса",
                    score);

            System.out.println(playerResult);
            resultMessage.append(playerResult).append("\n");

            notifier.sendToPlayer(player, "FINAL_SCORE|" +
                    (i + 1) + "|" + player.getUsername() + "|" + score);
        }

        if (!sortedPlayers.isEmpty()) {
            int maxScore = finalScores.get(sortedPlayers.get(0));
            List<Player> winners = sortedPlayers.stream()
                    .filter(p -> finalScores.get(p) == maxScore)
                    .collect(Collectors.toList());

            if (winners.size() == 1) {
                Player winner = winners.get(0);
                String winMessage = String.format("\n🏆 ПОБЕДИТЕЛЬ: %s с %d монетами!",
                        winner.getUsername(), maxScore);
                System.out.println(winMessage);
                resultMessage.append(winMessage);

                notifier.broadcast("WINNER|" + winner.getUsername() + "|" + maxScore);
            } else {
                String winnersList = winners.stream()
                        .map(p -> p.getUsername())
                        .collect(Collectors.joining(", "));

                String winMessage = String.format("\n🤝 НИЧЬЯ! Победители: %s с %d монетами",
                        winnersList, maxScore);
                System.out.println(winMessage);
                resultMessage.append(winMessage);

                notifier.broadcast("DRAW|" + maxScore + "|" + winnersList);
            }
        }

        resultMessage.append("\n══════════════════════════════");

        notifier.broadcast("GAME_OVER|" + resultMessage);

    }

    private int collectCoinsFromPath(List<Position> path) {
        DeckRooms deckRooms = gameState.getDeckRooms();
        int coins = 0;

        for (Position pos : path) {
            Integer cardId = gameState.getCardIdAtPosition(pos);
            if (cardId != null) {
                Room room = deckRooms.getCardById(cardId);
                if (room != null) {
                    if (room.getEventType() == EventType.COIN) {
                        if (room instanceof CoinRoom) {
                            int roomCoins = ((CoinRoom) room).getCountCoins();
                            coins += roomCoins;
                        }
                    } else if (room.getEventType() == EventType.ENEMY) {
                        coins += ((EnemyRoom) room).getCoins();
                    }
                }
            }
        }

        return coins;
    }

    public GameState getGameState() {
        return gameState;
    }
}
