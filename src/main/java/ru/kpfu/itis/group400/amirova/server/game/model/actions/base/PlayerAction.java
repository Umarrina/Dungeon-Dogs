package ru.kpfu.itis.group400.amirova.server.game.model.actions.base;

import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

public abstract class PlayerAction {
    protected Player player;
    protected ActionType actionType;

    public PlayerAction(Player player, ActionType actionType) {
        this.player = player;
        this.actionType = actionType;
    }

    public Player getPlayer() {
        return player;
    }

    public ActionType getActionType() {
        return actionType;
    }
}
