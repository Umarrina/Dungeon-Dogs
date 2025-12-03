package ru.kpfu.itis.group400.amirova.server.game.model;

import ru.kpfu.itis.group400.amirova.server.game.GameInitializer;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerRoundState {
    private boolean hasExit;
    private boolean isSlept;
    private List<Room> exitPath;

    private int currentTokens;
    private DeckRooms collectedRooms;
    private int currentHealth;

    private Room currentRoom;

    // Первичная инициализация, когда начинаем раунд
    public PlayerRoundState() {
        this.hasExit = false;
        this.isSlept = false;
        this.exitPath = new ArrayList<Room>();
    }

    public void setHasExit(boolean hasExit) {
        this.hasExit = hasExit;
    }

    public void setSlept(boolean slept) {
        isSlept = slept;
    }

    public void setExitPath(List<Room> exitPath) {
        this.exitPath = exitPath;
    }

    public void setCurrentTokens(int currentTokens) {
        this.currentTokens = currentTokens;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public boolean isHasExit() {
        return hasExit;
    }

    public boolean isSlept() {
        return isSlept;
    }

    public List<Room> getExitPath() {
        return exitPath;
    }

    public int getCurrentTokens() {
        return currentTokens;
    }

    public DeckRooms getCollectedRooms() {
        return collectedRooms;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }
}
