package ru.kpfu.itis.group400.amirova.server.game.model.actions;

import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.ActionType;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.PlayerAction;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

public class SleepAction extends PlayerAction {
    public SleepAction(Player player) {
        super(player, ActionType.SLEEP);
    }
}
