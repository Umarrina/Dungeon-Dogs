package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.exception.GameException;
import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;
import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.StartRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckPotions;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckQuests;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.dogs.Dog;
import ru.kpfu.itis.group400.amirova.server.game.model.dogs.GradeType;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.ArrayList;
import java.util.EnumMap;
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
        EnumMap<DamageType, Integer> damage1 = new EnumMap<>(DamageType.class);
        damage1.put(DamageType.STRENGTH, 2);
        damage1.put(DamageType.DEXTERITY, 1);
        Dog dog1 = new Dog("Жанна Гавк", GradeType.KNIGHT, "", "", damage1, 3);

        EnumMap<DamageType, Integer> damage2 = new EnumMap<>(DamageType.class);
        damage2.put(DamageType.STRENGTH, 2);
        damage2.put(DamageType.MAGIC, 1);
        Dog dog2 = new Dog("Боньк Могучий", GradeType.BARBARIAN, "", "", damage2, 3);

        EnumMap<DamageType, Integer> damage3 = new EnumMap<>(DamageType.class);
        damage3.put(DamageType.STRENGTH, 1);
        damage3.put(DamageType.MAGIC, 2);
        Dog dog3 = new Dog("Рута Снежная", GradeType.PRIESTESS, "", "", damage3, 5);

        EnumMap<DamageType, Integer> damage4 = new EnumMap<>(DamageType.class);
        damage4.put(DamageType.MAGIC, 2);
        damage4.put(DamageType.DEXTERITY, 1);
        Dog dog4 = new Dog("Шарик Огненный", GradeType.WIZARD, "", "", damage1, 3);

        dogs.add(dog1);
        dogs.add(dog2);
        dogs.add(dog3);
        dogs.add(dog4);
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
            player.setPlayerRoundState();

            player.getPlayerRoundState().setCurrentTokens(player.getMaxTokens());
            player.getPlayerRoundState().setCurrentHealth(player.getDog().getMaxHealth());
        }
    }

    public void firstInitializePlayersField(List<Player> players) {
        for (Player player : players) {
            player.setMaxTokens(5);
            player.setPlayerRoundState();

            player.getPlayerRoundState().setCurrentTokens(player.getMaxTokens());
            player.getPlayerRoundState().setCurrentHealth(player.getDog().getMaxHealth());

            player.initTrophyDeck(new DeckRooms());
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
