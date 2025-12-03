package ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms;

import ru.kpfu.itis.group400.amirova.exception.GameException;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;

public class StartRoom extends Room {

    // TODO  не факт что реализую, но пусть пока будет
    private int countExits;
    // singleton
    private static StartRoom instance;

    // synchronized для потокобезопасности
    public static synchronized StartRoom init(int countExits) {
        if (instance == null) {
            instance = new StartRoom(countExits);
        }
        return instance;
    }

    // первичная инициализация
    public StartRoom(int countExits) {
        validate(countExits);
        this.countExits = countExits;
        eventType = EventType.START;
        isVisited = true;
    }

    public void validate(int countExits) {
        if (countExits < 3 || countExits > 4) {
            throw new GameException("Count of exits for start room must be between 3 and 4.");
        }
    }
}
