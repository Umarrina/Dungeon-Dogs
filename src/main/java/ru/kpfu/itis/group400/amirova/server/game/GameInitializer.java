package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.exception.GameException;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.StartRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckPotions;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckQuests;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.dogs.Dog;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.ArrayList;
import java.util.List;

public class GameInitializer {

    private DeckRooms deckRooms;
    private DeckPotions deckPotions;
    private DeckQuests deckQuests;
    private List<Dog> dogs;

    public GameInitializer() {
        deckRooms = new DeckRooms();
        deckPotions = new DeckPotions();
        deckQuests = new DeckQuests();
        dogs = new ArrayList<>();
    }

    public void initializeAll() {
        createDogs();
        createRooms();

    }

    private void createDogs() {

    }

    private void createRooms() {
        createDogs();
        createStartRoom();
        createEnemyRooms();
        createCoinRooms();
        createArtifactRooms();
    }

    private void createEnemyRooms() {

    }

    private void createCoinRooms() {

    }

    private void createStartRoom() {

    }

    private void createArtifactRooms() {

    }

    public List<Dog> getDogs() {
        return dogs;
    }

    public DeckRooms getDeckRooms() {
        return deckRooms;
    }

    public void initializePlayersField(List<Player> players) {
        for (Player player : players) {
            player.setMaxTokens(5);
            player.getPlayerRoundState().setCurrentTokens(player.getMaxTokens());
            player.getPlayerRoundState().setCurrentHealth(player.getDog().getMaxHealth());
        }
    }

    public StartRoom initializeStartRoom(List<Player> players) {
        int countPlayers = players.size();
        if (countPlayers == 2) {
            return new StartRoom(3);
        }  else if (countPlayers == 3 ||  countPlayers == 4) {
            return new StartRoom(4);
        } else {
            throw new GameException("Invalid number of players");
        }
    }

    public DeckRooms initializeDeckRooms() {
        return deckRooms;
    }
}
