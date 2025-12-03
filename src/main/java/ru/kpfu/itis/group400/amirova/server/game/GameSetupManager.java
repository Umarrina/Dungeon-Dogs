package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.exception.GameException;
import ru.kpfu.itis.group400.amirova.server.game.model.GameSetupResult;
import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.StartRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameSetupManager {
    private List<Player> players;
    private GameInitializer gameInitializer;

    public GameSetupManager(List<Player> players) {
        gameInitializer = new GameInitializer();
        this.players = players;
    }

    public GameSetupResult setupGame() {
        // пока рандом только
        // TODO сделать не рандом
        gameInitializer.initializeAll();
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setDog(gameInitializer.getDogs().get(i));
            players.get(i).setPlayerRoundState();
        }

        gameInitializer.initializePlayersField(players);
        // TODO разадача карт квестов
        // TODO Раздача зелий

        StartRoom startRoom = gameInitializer.initializeStartRoom(players);

        DeckRooms deckRooms = gameInitializer.initializeDeckRooms();

        return new GameSetupResult(players,  startRoom, deckRooms);
    }


}
