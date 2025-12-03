package ru.kpfu.itis.group400.amirova.server.game.model.players;

import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;
import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckPotions;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.dogs.Dog;

import java.util.EnumMap;

public class Player {
    private String username;
    private Dog dog;

    private int totalCoins;
    private int maxTokens;
    private DeckRooms trophyDeck;
    private DeckPotions deckPotions;

    private EnumMap<DamageType, Integer> additionalDamage;

    private PlayerRoundState playerRoundState;

    // TODO классы колода-квесты и колода-зелья

    // Первичная инициализация при регистрации
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

        // TODO сделать здесь подсчет добавленного уроно исзодя из трофеев
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void addMaxTokens(int additionalMaxTokens) {
        this.maxTokens += maxTokens;
    }

    public void setPlayerRoundState() {
        playerRoundState = new PlayerRoundState();
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

    public DeckRooms getTrophyDeck() {
        return trophyDeck;
    }

    public DeckPotions getDeckPotions() {
        return deckPotions;
    }

    public EnumMap<DamageType, Integer> getAdditionalDamage() {
        return additionalDamage;
    }

    public Room getCurrentCard() {
        return playerRoundState.getCurrentRoom();
    }
}
