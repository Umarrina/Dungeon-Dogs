package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.game.model.GameSetupResult;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.StartRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.List;

public class GameSetupManager {
    private List<Player> players;
    private GameInitializer gameInitializer;

    public GameSetupManager(List<Player> players) {
        gameInitializer = new GameInitializer();
        this.players = players;
    }

    public GameSetupResult setupGame() {
        gameInitializer.initializeAll();

        for (int i = 0; i < players.size(); i++) {
            players.get(i).setDog(gameInitializer.getDogs().get(i));
            players.get(i).setPlayerRoundState();
        }

        gameInitializer.firstInitializePlayersField(players);

        StartRoom startRoom = gameInitializer.getStartRoom();
        DeckRooms deckRooms = gameInitializer.getDeckRooms();

        return new GameSetupResult(players, startRoom, deckRooms);
    }


}
