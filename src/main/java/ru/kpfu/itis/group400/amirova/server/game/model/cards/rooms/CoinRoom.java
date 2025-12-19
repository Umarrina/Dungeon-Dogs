package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms;

import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

import java.util.EnumMap;

public class CoinRoom extends Room {
    private int countCoins;

    public CoinRoom(int id, String name, EnumMap<Direction, Boolean> exits, int countCoins) {
        this.id = id;
        this.name = name;
        this.exits = exits;
        this.eventType = EventType.COIN;
        this.positionX = 0;
        this.positionY = 0;
        this.rotation = 0;
        this.isVisited = false;
        this.countCoins = countCoins;
    }

    public int getCountCoins() {
        return countCoins;
    }
}
