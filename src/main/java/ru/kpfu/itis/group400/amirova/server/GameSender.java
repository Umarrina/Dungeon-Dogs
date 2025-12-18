package ru.kpfu.itis.group400.amirova.server;

import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.List;
import java.util.Map;

public class GameNotifier {
    private GameServer gameServer;
    private GameSerializer serializer;

    public GameNotifier(GameServer gameServer) {
        this.gameServer = gameServer;
        this.serializer = new GameSerializer();
    }

    public void sendActionRequest(Player player, String actionType) {
        String message = "SERVER_COMMAND|" + actionType;
        gameServer.sendToPlayer(player, message);
    }

    public void sendError(Player player, String message) {
        gameServer.sendToPlayer(player, "ERROR|" +  message);
    }

    public void broadcast(String message) {
        gameServer.broadcastToAll("BROADCAST|" + message);
    }

    public void notifyExplore(Player player, Room room, List<Position> availablePositions) {
        String cardData = serializer.serializeCard(room);
        String positions = serializer.serializePositions(availablePositions);

        gameServer.sendToPlayer(player, "CARD_DRAWN|" + cardData + "|" + positions);
    }

    public void notifyGameStateUpdate(List<Player> players, Map<Position, Room> rooms) {
        String state = serializer.serializeGameState(players, rooms);
        gameServer.broadcastToAll("GAMESTATE|" + state);
    }


    public void notifyRoundStart(int round) {
        gameServer.broadcastToAll("ROUND_START|" + round);
    }

    public void notifyCurrentPlayer(Player player) {
        gameServer.broadcastToAll("CURRENT_PLAYER|" + player.getUsername());
    }

    public void enablePlayerButtons(Player player, boolean enable) {
        gameServer.sendToPlayer(player, enable ? "ENABLE_BUTTONS" : "DISABLE_BUTTONS");
    }

    public void enableSpecialButtons(Player player, boolean rotate, boolean place) {
        gameServer.sendToPlayer(player, "ENABLE_SPECIAL|" + rotate + "|" + place);
    }

    public void notifyAvailablePositions(Player player, List<Position> positions) {
        StringBuilder sb = new StringBuilder();
        for (Position pos : positions) {
            sb.append(pos.getX()).append(",").append(pos.getY()).append(";");
        }
        gameServer.sendToPlayer(player, "AVAILABLE_POSITIONS|" + sb.toString());
    }

    public void notifyTokensUpdate(Player player, int tokens) {
        gameServer.sendToPlayer(player, "TOKENS_UPDATE|" + tokens);
    }

    public void notifyPlayerSlept(Player player) {
        gameServer.broadcastToAll("PLAYER_SLEPT|" + player.getUsername());
    }

    public void notifyPlayerExited(Player player, int coins) {
        gameServer.broadcastToAll("PLAYER_EXITED|" + player.getUsername() + "|" + coins);
    }

    public void notifyCardPlaced(Player player, Position position) {
        gameServer.broadcastToAll("CARD_PLACED|" + player.getUsername() + "|" +
                position.getX() + "," + position.getY());
    }

    public void startExitPathSelection(Player player, List<Position> exits) {
        StringBuilder sb = new StringBuilder();
        for (Position exit : exits) {
            sb.append(exit.getX()).append(",").append(exit.getY()).append(";");
        }
        gameServer.sendToPlayer(player, "START_EXIT_PATH|" + sb.toString());
    }

    public void notifyPlayerDisconnected(String username) {
        gameServer.broadcastToAll("PLAYER_DISCONNECTED|" + username);
    }

    public void notifyGameEnd(String winner, int coins) {
        gameServer.broadcastToAll("GAME_END|" + winner + "|" + coins);
    }

    public void notifyError(Player player, String error) {
        gameServer.sendToPlayer(player, "ERROR|" + error);
    }

}
