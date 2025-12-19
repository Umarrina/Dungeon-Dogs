package ru.kpfu.itis.group400.amirova.server.game.model;

import java.util.ArrayList;
import java.util.List;

public class PlayerRoundState {
    private boolean hasExit;
    private boolean isSlept;
    private List<Integer> exitPath;

    private int currentTokens;
    private int currentHealth;

    private Integer currentRoomId;

    public PlayerRoundState() {
        this.hasExit = false;
        this.isSlept = false;
        this.exitPath = new ArrayList<>();
        this.currentRoomId = null;
    }

    public void setHasExit(boolean hasExit) {
        this.hasExit = hasExit;
    }

    public void setSlept(boolean slept) {
        isSlept = slept;
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

    public int getCurrentTokens() {
        return currentTokens;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void addToExitPath(int cardId) {
        exitPath.add(cardId);
    }

    public List<Integer> getExitPathCardIds() {
        return exitPath;
    }

    public void setExitPath(List<Integer> exitPath) {
        this.exitPath = exitPath;
    }

    public Integer getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(Integer currentRoomId) {
        this.currentRoomId = currentRoomId;
    }
}
