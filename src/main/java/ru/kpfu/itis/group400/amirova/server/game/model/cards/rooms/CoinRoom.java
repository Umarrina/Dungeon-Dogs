package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms;

import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

public class CoinRoom extends Room {
    private int countCoins;

    public CoinRoom(int countCoins) {
        this.countCoins = countCoins;
        eventType = EventType.COIN;
        isVisited = false;
    }
}
