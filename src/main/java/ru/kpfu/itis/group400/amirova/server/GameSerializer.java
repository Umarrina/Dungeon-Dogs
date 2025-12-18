package ru.kpfu.itis.group400.amirova.server;

import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GameSerializer {

    public String serializeCard(Room card) {
        if (card == null) {
            return "null";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(card.getName() + ":")
                .append(card.getEventType() + ":")
                .append(card.getPositionX() + ":")
                .append(card.getPositionY() + ":")
                .append(serializeExits(card.getExits()));

        return sb.toString();
    }

    public String serializeExits(EnumMap<Direction, Boolean> exits) {
        return exits.entrySet().stream()
                .map(e -> e.getKey().name() + "=" + e.getValue())
                .collect(Collectors.joining(","));
    }

    public String serializePositions(List<Position> positions) {
        return positions.stream()
                .map(Position::toString)
                .collect(Collectors.joining(","));
    }

    public Position deserializePositions(String positionString) {
        String[] parts =  positionString.split(",");
        return new  Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    public String serializePlayer(Player player) {
        return player.getUsername() + ":" +
                player.getDog().getName() + ":" +
                player.getTotalCoins() + ":" +
                player.getPlayerRoundState().getCurrentHealth();
    }

    public String serializeBoard(Map<Position, Room> board) {
        return board.entrySet().stream()
                .map(e -> {
                    Position position = e.getKey();
                    Room room = e.getValue();
                    return position.getX() + ","  + position.getY() + ":" + serializeCard(room);
                })
                .collect(Collectors.joining("|"));
    }

    public String serializeGameState(List<Player> players, Map<Position, Room> board) {
        StringBuilder sb = new StringBuilder();

        sb.append("PLAYERS:");
        for (Player player : players) {
            sb.append(serializePlayer(player)).append(";");
        }

        sb.append("BOARD:").append(serializeBoard(board));

        return sb.toString();
    }

    public List<Position> deserializePath(String data) {
        String[] parts = data.split("\\|");
        List<Position> path = new ArrayList<>();
        for (String part : parts) {
            path.add(new  Position(Integer.parseInt(part.split(":")[0]), Integer.parseInt(part.split(":")[1])));
        }
        return path;
    }
}
