package ru.kpfu.itis.group400.amirova.server.game.model;

import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.StartRoom;

import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.List;

public class GameSetupResult {
    private List<Player> players;
    private StartRoom startRoom;
    private DeckRooms  deckRooms;

    public  GameSetupResult(List<Player> players, StartRoom startRoom, DeckRooms deckRooms) {
        this.players = players;
        this.startRoom = startRoom;
        this.deckRooms = deckRooms;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public StartRoom getStartRoom() {
        return startRoom;
    }

    public DeckRooms getDeckRooms() {
        return deckRooms;
    }

}
