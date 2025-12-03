package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms;

import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;

import java.util.EnumMap;

public class EnemyRoom extends Room {
    private EnumMap<DamageType, Integer> RequiredDamage;
    private int damage;
    private EnumMap<DamageType, Integer> givenCharacteristics;
    private int coins;

    // первичная инициализация
    public EnemyRoom(EnumMap<DamageType, Integer> requiredDamage, int damage, EnumMap<DamageType, Integer> givenCharacteristics, int coins) {
        RequiredDamage = requiredDamage;
        this.damage = damage;
        this.givenCharacteristics = givenCharacteristics;
        this.coins = coins;
        eventType = EventType.ENEMY;
        isVisited = false;
    }

    public EnumMap<DamageType, Integer> getRequiredDamage() {
        return RequiredDamage;
    }

    public int getDamage() {
        return damage;
    }

    public EnumMap<DamageType, Integer> getGivenCharacteristics() {
        return givenCharacteristics;
    }

    public int getCoins() {
        return coins;
    }
}
