package ru.kpfu.itis.group400.amirova.server.game.model.decks;

import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

import java.util.*;

public class DeckRooms {

    private List<Room> rooms = new ArrayList<>();
    private List<Room> drawnCards = new ArrayList<>();
    private final Random random = new Random();

    public void add(DeckRooms deckRooms) {
        for (Room room : deckRooms.rooms) {
            add(room);
        }
    }

    public int getRoomCount() {
        return rooms.size();
    }

    public void add(Room room) {
        rooms.add(room);
    }

    public Room getCardById(int id) {
        for (Room drawnCard : drawnCards) {
            if (drawnCard.getId() == id) {
                return drawnCard;
            }
        }
        return null;
    }

    public Room getFirstCard() {
        if (rooms.isEmpty()) {
            return null;
        }

        Room card = rooms.remove(random.nextInt(rooms.size()));
        drawnCards.add(card);

        return card;
    }

    public boolean hasNextCard() {
        return !rooms.isEmpty();
    }

    public void shuffle() {
        Collections.shuffle(rooms);
    }
}