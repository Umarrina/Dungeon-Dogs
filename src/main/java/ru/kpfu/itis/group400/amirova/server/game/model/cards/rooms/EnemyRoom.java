package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms;

import ru.kpfu.itis.group400.amirova.server.game.model.TrophyType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;

import java.util.EnumMap;

public class EnemyRoom extends Room {
    private EnumMap<DamageType, Integer> requiredDamage;
    private int damage;
    private EnumMap<TrophyType, Integer> givenCharacteristics;
    private int coins;

    public EnemyRoom(int id, String name, EnumMap<Direction, Boolean> exits,
                     EnumMap<DamageType, Integer> requiredDamage, int damage,
                     EnumMap<TrophyType, Integer> givenCharacteristics, int coins) {
        this.id = id;
        this.name = name;
        this.exits = exits;
        this.eventType = EventType.ENEMY;
        this.positionX = 0;
        this.positionY = 0;
        this.rotation = 0;
        this.isVisited = false;
        this.requiredDamage = requiredDamage;
        this.damage = damage;
        this.givenCharacteristics = givenCharacteristics;
        this.coins = coins;
    }

    public EnumMap<DamageType, Integer> getRequiredDamage() {
        return requiredDamage;
    }

    public int getDamage() {
        return damage;
    }

    public EnumMap<TrophyType, Integer> getGivenCharacteristics() {
        return givenCharacteristics;
    }

    public int getCoins() {
        return coins;
    }
}
