package ru.kpfu.itis.group400.amirova.server;

import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GameSerializer {

    public Position deserializePositions(String positionStr) {
        if (positionStr == null || positionStr.isEmpty()) {
            return new Position(0, 0);
        }
        String[] parts = positionStr.split(",");
        if (parts.length != 2) {
            return new Position(0, 0);
        }
        return new Position(
                Integer.parseInt(parts[0].trim()),
                Integer.parseInt(parts[1].trim())
        );
    }
}
