package ru.kpfu.itis.group400.amirova.server.game.model.actions;

import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.ActionType;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.PlayerAction;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

public class PlaceCardAction extends PlayerAction {
    private Position position;

    public PlaceCardAction(Player player, Position position) {
        super(player, ActionType.PLACE_CARD);
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }
}
