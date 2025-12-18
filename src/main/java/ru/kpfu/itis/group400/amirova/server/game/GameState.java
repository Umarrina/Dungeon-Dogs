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

    public void addPlacesRooms (Room room, Position position) {
        placesRooms.put(position, room);
    }

    public boolean placeCard(Room placedCard, Position position) {
        if (placesRooms.containsKey(position)) {
            return false;
        }
        return true;
    }

    public boolean canDrawCard() {
        if (cardsAvailable > 0 && deckRooms.hasNextCard()) {
            return true;
        }
        return false;
    }

    public boolean allPlayersFinished() {
        for (Player player : players) {
            if (!player.getPlayerRoundState().isHasExit() && !player.getPlayerRoundState().isSlept()) {
                return false;
            }
        }
        return true;
    }

    public List<Position> getAvailablePositions(Room room) {
        initializeAvailablePosition(room);
        return availablePositions;
    }

    public void resetDeckLimit() {
        cardsAvailable = Integer.MAX_VALUE;
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

    public int getRound() {
        return round;
    }

    public void setMaxRound(int maxRound) {
        this.maxRound = maxRound;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public StartRoom getStartRoom() {
        return startRoom;
    }

    public void setStartRoom(StartRoom startRoom) {
        this.startRoom = startRoom;
    }

    public DeckRooms getDeckRooms() {
        return deckRooms;
    }

    public void setDeckRooms(DeckRooms deckRooms) {
        this.deckRooms = deckRooms;
    }

    public int getCardsAvailable() {
        return cardsAvailable;
    }

    public void setCardsAvailable(int cardsAvailable) {
        this.cardsAvailable = cardsAvailable;
    }

    public DeckPotions getDeckPotions() {
        return deckPotions;
    }

    public void setDeckPotions(DeckPotions deckPotions) {
        this.deckPotions = deckPotions;
    }

    public DeckQuests getDeckQuests() {
        return deckQuests;
    }

    public void setDeckQuests(DeckQuests deckQuests) {
        this.deckQuests = deckQuests;
    }

    public Map<Position, Room> getPlacesRooms() {
        return placesRooms;
    }

    public void setPlacesRooms(Map<Position, Room> placesRooms) {
        this.placesRooms = placesRooms;
    }

    public List<Position> getAvailablePositions() {
        return availablePositions;
    }

    public void setAvailablePositions(List<Position> availablePositions) {
        this.availablePositions = availablePositions;
    }
}
