package ru.kpfu.itis.group400.amirova.server.game.model.actions;

import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.ActionType;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.PlayerAction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.List;

public class ExitAction extends PlayerAction {
    private List<Room> exitPath;

    public ExitAction(Player player) {
        super(player, ActionType.EXIT_DUNGEON);
        this.exitPath = exitPath;
    }

    public List<Room> getExitPath() {
        return exitPath;
    }
}
