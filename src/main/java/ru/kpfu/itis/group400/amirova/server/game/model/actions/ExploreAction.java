package ru.kpfu.itis.group400.amirova.server.game.model.actions;

import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.ActionType;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.PlayerAction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;


public class ExploreAction extends PlayerAction {

    private Room room;
    private Position position;

    public ExploreAction(Player player, Room room, Position position) {
        super(player, ActionType.EXPLORE);
        this.room = room;
        this.position = position;
    }

    public Room getRoom() {
        return room;
    }

    public Position getPosition() {
        return position;
    }

}
