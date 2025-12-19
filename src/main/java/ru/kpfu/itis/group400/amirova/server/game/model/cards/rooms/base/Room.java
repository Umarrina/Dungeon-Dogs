package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base;

import java.util.EnumMap;

public abstract class Room {

    protected int id;
    protected String name;
    protected EnumMap<Direction, Boolean> exits;
    protected EventType eventType;
    protected int positionX;
    protected int positionY;
    protected int rotation;
    protected boolean isVisited;

    public String getName() {
        return name;
    }

    public EnumMap<Direction, Boolean> getExits() {
        return exits;
    }

    public EventType getEventType() {
        return eventType;
    }

    public int getId() {
        return id;
    }

    public int getRotation() {
        return rotation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExits(EnumMap<Direction, Boolean> exits) {
        this.exits = exits;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void rotate() {
        rotation = (rotation + 90) % 360;
        Boolean temp = exits.get(Direction.TOP);
        exits.put(Direction.TOP, exits.get(Direction.LEFT));
        exits.put(Direction.LEFT, exits.get(Direction.BOTTOM));
        exits.put(Direction.BOTTOM, exits.get(Direction.RIGHT));
        exits.put(Direction.RIGHT, temp);
    }
}
