package ru.kpfu.itis.group400.amirova.server.game.model.players;

import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.dogs.Dog;


public class Player {
    private String username;
    private Dog dog;

    private int totalCoins;
    private int maxTokens;
    private DeckRooms trophyDeck;

    private PlayerRoundState playerRoundState;

    public Player(String username) {
        this.username = username;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

    public void initTotalCoins(int coins) {
        if (totalCoins == 0) {
            totalCoins = coins;
        } else {
            totalCoins += coins;
        }
    }

    public void initTrophyDeck(DeckRooms additionalTrophyDeck) {
        if (trophyDeck == null) {
            trophyDeck = additionalTrophyDeck;
        } else {
            trophyDeck.add(additionalTrophyDeck);
        }
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void addMaxTokens(int additionalMaxTokens) {
        this.maxTokens += additionalMaxTokens;
    }

    public void setPlayerRoundState() {
        this.playerRoundState = new PlayerRoundState();
    }

    public PlayerRoundState getPlayerRoundState() {
        return playerRoundState;
    }

    public String getUsername() {
        return username;
    }

    public Dog getDog() {
        return dog;
    }

    public int getTotalCoins() {
        return totalCoins;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public Integer getCurrentRoomId() {
        return playerRoundState.getCurrentRoomId();
    }

    public void setCurrentRoomId(Integer roomId) {
        playerRoundState.setCurrentRoomId(roomId);
    }

    public boolean hasCard() {
        return playerRoundState.getCurrentRoomId() != null;
    }
}
