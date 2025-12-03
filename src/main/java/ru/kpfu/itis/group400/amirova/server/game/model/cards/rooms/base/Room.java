package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base;

import java.util.EnumMap;

// класс абстрактный, потому что мне не нужны объекты, нужны только объекты наследников
public abstract class Room{
    /*
    * у каждой комнаты, в теории, есть 4 выхода
    * на практике от 2 до 4
    * также в центре комнаты изображено событие
    * относительно всего поля каждая клетка имеет координаты x и y
    * TODO спросить о том какую логику поля сделать
    * данные координаты считаются относительно двумерного массива, предположительно, 90*90
    * любую карту можно повернуть
    * важно проверять возможность коннекта карт
     */

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

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
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

    /*
     * допустим, есть карта, у которой проходы справа и снизу
     * мы ее хотим приконнектить так, чтобы в нее пришли снизу
     * тогда direction bottom
     * а мы проверяем что у нашей карты exits.get(Direction.BOTTOM) = true
     */
    public boolean connectionIsPossible(Room otherCard, Direction direction) {
        if (otherCard.getExits().get(getOppositeDirection(direction)) == true) {
            if (direction == Direction.LEFT && exits.get(Direction.LEFT) == true) {
                return true;
            } else if (direction == Direction.RIGHT && exits.get(Direction.LEFT) == true) {
                return true;
            } else if (direction == Direction.TOP && exits.get(Direction.BOTTOM) == true) {
                return true;
            } else if (direction == Direction.BOTTOM && exits.get(Direction.TOP) == true) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private Direction getOppositeDirection(Direction direction) {
        switch (direction) {
            case TOP: return Direction.BOTTOM;
            case BOTTOM: return Direction.TOP;
            case LEFT: return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
            default: return direction;
        }
    }

    /*
     * поворот всегда осуществляем по часовой стрелке на 90 градусов
     * во временную переменную записываем наличие прохода сверху
     * сверху пишем то что было слева
     * слева пишем то что было снизу
     * снизу пишем то что было справа
     * справа пишем то что во временной перемнной
     */
    public void rotate() {
        rotation = (rotation + 90) % 360;
        Boolean temp = exits.get(Direction.TOP);
        exits.put(Direction.TOP, exits.get(Direction.LEFT));
        exits.put(Direction.LEFT, exits.get(Direction.BOTTOM));
        exits.put(Direction.BOTTOM, exits.get(Direction.RIGHT));
        exits.put(Direction.RIGHT, temp);
    }
}
