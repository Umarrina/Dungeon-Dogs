package ru.kpfu.itis.group400.amirova.server;

import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.List;
import java.util.stream.Collectors;

public class GameSender {
    private GameServer gameServer;

    public GameSender(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    public void notifyCardDrawn(Player player, int cardId, List<Position> availablePositions) {
        String positions = serializePositions(availablePositions);
        String message = "CARD_DRAWN|" + cardId + "|" + positions;
        sendToPlayer(player, message);
    }

    public void notifyCardPlaced(Player player, int cardId, Position position, int rotation) {
        String pos = position.getX() + "," + position.getY();
        broadcast("CARD_PLACED|" + player.getUsername() + "|" + pos + "|" + cardId +  "|" + rotation);
    }

    public void sendAvailablePositions(Player player, List<Position> positions) {
        String posStr = serializePositions(positions);
        sendToPlayer(player, "AVAILABLE_POSITIONS|" + posStr);
    }

    private String serializePositions(List<Position> positions) {
        return positions.stream()
                .map(p -> p.getX() + "," + p.getY())
                .collect(Collectors.joining(";"));
    }

    public void sendActionRequest(Player player, String actionType) {
        sendToPlayer(player, "SERVER_COMMAND|" + actionType);
    }

    public void sendError(Player player, String message) {
        gameServer.sendToPlayer(player, "ERROR|" +  message);
    }

    public void broadcast(String message) {
        gameServer.broadcastToAll("BROADCAST|" + message);
    }

    public void sendToPlayer(Player player, String message) {
        gameServer.sendToPlayer(player, message);
    }

    public void notifyCurrentPlayer(Player player) {
        broadcast("CURRENT_PLAYER|" + player.getUsername());
    }

    public void notifyPlayerExit(Player player, int coins) {
        broadcast("PLAYER_EXITED|" + player.getUsername() + "|" + coins);
    }

    public void notifyPlayerSleep(Player player) {
        broadcast("PLAYER_SLEPT|" + player.getUsername());
    }
}
