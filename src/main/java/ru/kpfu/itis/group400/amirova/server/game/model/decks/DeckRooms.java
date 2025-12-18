package ru.kpfu.itis.group400.amirova.server.game.model.decks;

import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

import java.util.List;

public class DeckRooms {
    private List<Room> rooms;

//долэны ыбьт метод передать карту игроку, убрать карту в сброс


    public void add(DeckRooms deckRooms) {
        rooms.addAll(deckRooms.rooms);
    }

    public Room getFirstCard() {
        return rooms.remove(0);
    }

    public boolean hasNextCard() {
        return !rooms.isEmpty();
    }
}
