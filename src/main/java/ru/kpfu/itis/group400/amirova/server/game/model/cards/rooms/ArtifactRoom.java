package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms;

import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

import java.util.EnumMap;

public class ArtifactRoom extends Room {
    private EnumMap<DamageType, Integer> damage;
}
