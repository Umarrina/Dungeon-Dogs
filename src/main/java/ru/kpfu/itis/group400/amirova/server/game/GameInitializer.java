package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.factory.CardFactory;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.StartRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.dogs.Dog;
import ru.kpfu.itis.group400.amirova.server.game.model.dogs.GradeType;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class GameInitializer {

    private DeckRooms deckRooms;
    private List<Dog> dogs;
    private StartRoom startRoom;

    public GameInitializer() {
        deckRooms = new DeckRooms();
        dogs = new ArrayList<>();
    }

    public void initializeAll() {
        if (deckRooms.getRoomCount() == 0) {
            createDogs();
            loadCardsFromCSV();
        }
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

    private void loadCardsFromCSV() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/configuration.csv")))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                try {
                    Room room = CardFactory.createRoomFromCSV(parts);

                    if (room.getEventType() == EventType.START) {
                        startRoom = (StartRoom) room;
                    } else {
                        deckRooms.add(room);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при создании карты из строки: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении CSV файла: " + e.getMessage());
        }
    }


    public List<Dog> getDogs() {
        return dogs;
    }

    public DeckRooms getDeckRooms() {
        return deckRooms;
    }

    public StartRoom getStartRoom() {
        return startRoom;
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
}
