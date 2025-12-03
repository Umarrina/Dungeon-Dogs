package ru.kpfu.itis.group400.amirova.server.game.model.actions;

import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.ActionType;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.PlayerAction;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

public class RotateCardAction extends PlayerAction {

    public RotateCardAction(Player player) {
        super(player, ActionType.ROTATE_CARD);
    }
}
