package ru.kpfu.itis.group400.amirova.server.game.model.cards.factory;

import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;
import ru.kpfu.itis.group400.amirova.server.game.model.TrophyType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.*;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

import java.util.EnumMap;

public class CardFactory {

    public static Room createRoomFromCSV(String[] parts) {
        if (parts.length < 8) {
            throw new IllegalArgumentException("Недостаточно данных в CSV строке");
        }

        int id = Integer.parseInt(parts[0]);
        String name = parts[1];
        String exitsStr = parts[2];
        EventType eventType = EventType.valueOf(parts[3]);

        EnumMap<Direction, Boolean> exits = parseExits(exitsStr);

        Room room;
        switch (eventType) {
            case START:
                room = new StartRoom(id, name, exits);
                break;

            case ENEMY:
                if (parts.length < 12) {
                    throw new IllegalArgumentException("Недостаточно данных для врага: " + name);
                }
                EnumMap<DamageType, Integer> requiredDamage = parseDamage(parts[8]);
                int damage = Integer.parseInt(parts[9]);
                EnumMap<TrophyType, Integer> givenCharacteristics = parseTrophy(parts[10]);
                int coins = Integer.parseInt(parts[11]);
                room = new EnemyRoom(id, name, exits, requiredDamage, damage, givenCharacteristics, coins);
                break;

            case COIN:
                if (parts.length < 9) {
                    throw new IllegalArgumentException("Недостаточно данных для монет: " + name);
                }
                room = new CoinRoom(id, name, exits, Integer.parseInt(parts[8]));
                break;

            case ARTIFACT:
                if (parts.length < 9) {
                    throw new IllegalArgumentException("Недостаточно данных для артефакта: " + name);
                }
                EnumMap<DamageType, Integer> artifactGiven = parseDamage(parts[8]);
                room = new ArtifactRoom(id, name, exits, artifactGiven);
                break;

            default:
                throw new IllegalArgumentException("Неизвестный тип события: " + eventType);
        }

        room.setName(name);
        room.setExits(exits);

        return room;
    }

    private static EnumMap<Direction, Boolean> parseExits(String exitsStr) {
        String[] exitsArr = exitsStr.split(":");
        EnumMap<Direction, Boolean> exits = new EnumMap<>(Direction.class);

        exits.put(Direction.TOP, exitsArr[0].equals("1"));
        exits.put(Direction.RIGHT, exitsArr[1].equals("1"));
        exits.put(Direction.BOTTOM, exitsArr[2].equals("1"));
        exits.put(Direction.LEFT, exitsArr[3].equals("1"));

        return exits;
    }

    private static EnumMap<DamageType, Integer> parseDamage(String damageStr) {
        String[] damageArr = damageStr.split(":");
        EnumMap<DamageType, Integer> damage = new EnumMap<>(DamageType.class);

        damage.put(DamageType.DEXTERITY, Integer.parseInt(damageArr[0]));
        damage.put(DamageType.STRENGTH, Integer.parseInt(damageArr[1]));
        damage.put(DamageType.MAGIC, Integer.parseInt(damageArr[2]));

        return damage;
    }

    private static EnumMap<TrophyType, Integer> parseTrophy(String damageStr) {
        String[] damageArr = damageStr.split(":");
        EnumMap<TrophyType, Integer> damage = new EnumMap<>(TrophyType.class);

        damage.put(TrophyType.DEXTERITY, Integer.parseInt(damageArr[0]));
        damage.put(TrophyType.STRENGTH, Integer.parseInt(damageArr[1]));
        damage.put(TrophyType.MAGIC, Integer.parseInt(damageArr[2]));
        damage.put(TrophyType.HEALTH, Integer.parseInt(damageArr[3]));

        return damage;
    }
}