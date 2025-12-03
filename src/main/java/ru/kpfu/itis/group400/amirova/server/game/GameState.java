package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.StartRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckPotions;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckQuests;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.*;

public class GameState {
    private int round;
    private int maxRound;

    private List<Player> players;
    private StartRoom startRoom;

    private DeckRooms deckRooms;
    private int cardsAvailable;

    private DeckPotions deckPotions;
    private DeckQuests deckQuests;

    private Map<Position, Room> placesRooms;
    private List<Position> availablePositions;

    private Player currentPlayer;

    // первичная инициализация
    public GameState(List<Player> players, StartRoom startRoom, DeckRooms deckRooms,  DeckPotions deckPotions, DeckQuests deckQuests) {
        maxRound = 3;
        this.players = players;
        this.startRoom = startRoom;
        this.deckRooms = deckRooms;
        cardsAvailable = Integer.MAX_VALUE;
        this.deckPotions = deckPotions;
        this.deckQuests = deckQuests;
    }

    public int getMaxRound() {
        return maxRound;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public void initializeAvailablePosition(Room checkingRooms) {
        availablePositions = new ArrayList<>();
        for (Map.Entry<Position, Room> entry : placesRooms.entrySet()) {
            Room room = entry.getValue();
            for (EnumMap.Entry<Direction, Boolean> direction : checkingRooms.getExits().entrySet()) {
                if (checkingRooms.connectionIsPossible(room, direction.getKey())) {
                    availablePositions.add(entry.getKey());
                    break;
                }
            }
        }
    }

    public List<Position> getAvailablePositions(Room room) {
        initializeAvailablePosition(room);
        return availablePositions;
    }

    public Room getFirstCard() {
        return deckRooms.getFirstCard();
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
