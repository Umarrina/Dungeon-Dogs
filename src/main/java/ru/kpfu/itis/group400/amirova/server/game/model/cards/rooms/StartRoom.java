package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms;

import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

import java.util.EnumMap;

public class StartRoom extends Room {

    public StartRoom(int id, String name, EnumMap<Direction, Boolean> exits) {
        this.id = id;
        this.name = name;
        this.exits = exits;
        this.eventType = EventType.START;
        this.positionX = 0;
        this.positionY = 0;
        this.rotation = 0;
        this.isVisited = true;
    }
}
