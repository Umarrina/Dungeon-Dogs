package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms;

import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

import java.util.EnumMap;

public class ArtifactRoom extends Room {
    private EnumMap<DamageType, Integer> damage;

    public ArtifactRoom(int id, String name, EnumMap<Direction, Boolean> exits, EnumMap<DamageType, Integer> damage) {
        this.id = id;
        this.name = name;
        this.exits = exits;
        this.eventType = EventType.ARTIFACT;
        this.positionX = 0;
        this.positionY = 0;
        this.rotation = 0;
        this.isVisited = false;
        this.damage = damage;
    }

    public EnumMap<DamageType, Integer> getDamage() {
        return damage;
    }
}
