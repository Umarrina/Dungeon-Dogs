package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.List;

public class RoundManager {
    private List<Player> players;
    private int currentRound;
    private DeckRooms deckRooms;

    // TODO добавь потом еще зелья и квесты

    public RoundManager( DeckRooms deckRooms, List<Player> players) {
        this.deckRooms = deckRooms;
        this.players = players;
    }
    /*
    * стадии в roundManager для игрока
    * выставление комнаты
    * выход из поздемелья
    * сон
     */

    public void startRound(int currentRound) {
        this.currentRound = currentRound;
        setStartParameter();

    }

    private void setStartParameter() {

    }
}
