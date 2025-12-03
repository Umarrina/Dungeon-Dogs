package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.game.model.GameSetupResult;
import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.*;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.base.PlayerAction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.List;

public class GameEngine {
    private GameState gameState;
    private GameSetupManager gameSetupManager;
    private RoundManager roundManager;

    public GameEngine(List<Player> players) {
        gameSetupManager = new GameSetupManager(players);
        GameSetupResult gameSetupResult = gameSetupManager.setupGame();
        gameState = new GameState(players, gameSetupResult.getStartRoom(), gameSetupResult.getDeckRooms(), null, null);
        roundManager = new RoundManager(gameSetupResult.getDeckRooms(), gameSetupResult.getPlayers());
    }

    /* на этот момент у меня уже есть: игроки и закрепленные за ними собаки. у игроков инициализированы токены, т.е. они готовы к игре
    * собраны колоды, создана стартовая рума
    * этот метод определит очередность хода
    *
    * стадии в roundManager для игрока
    * выставление комнаты
    * выход из поздемелья
    * сон
     */
    public void startNewGame() {
        for (int i = 1; i < gameState.getMaxRound() + 1; i++) {
            gameState.setRound(i);
            roundManager.startRound(i);
        }
    }

    public void processPlayerAction(PlayerAction playerAction) {
        Player player = playerAction.getPlayer();

        if (!isActionValid(playerAction)) {
// TODO ну короч нельзя
            return;
        }

        switch (playerAction.getActionType()) {
            case EXPLORE:
                handleExplore((ExploreAction) playerAction);
                break;
            case EXIT_DUNGEON:
                handleExit((ExitAction) playerAction);
                break;
            case SLEEP:
                handleSleep((SleepAction) playerAction);
                break;
            case ROTATE_CARD:
                handleRotateCard((RotateCardAction) playerAction);
                break;
            case PLACE_CARD:
                handlePlaceCard((PlaceCardAction) playerAction);
                break;
        }
    }

    private void handleExplore(ExploreAction playerAction) {
        Player player = playerAction.getPlayer();



        if (!canPlayerExplore(player)) {
            sendAllert(player, "Сейчас нельзя иследовать");
            return;
        }

        Room room = gameState.getFirstCard();
        // где-то проверка на поворот
        List<Position> availablePositions = gameState.getAvailablePositions(room);
    }

    private void handleExit(ExitAction playerAction) {}

    private void handleSleep(SleepAction playerAction) {}

    private boolean canPlayerExplore(Player player) {
        PlayerRoundState playerRoundState = gameState.
    }

    private boolean isActionValid(PlayerAction action) {
        Player player = action.getPlayer();
        PlayerRoundState roundState = player.getPlayerRoundState();

        if (roundState.isHasExit() || roundState.isSlept()) {
            return false;
        }

        if (!gameState.getCurrentPlayer().equals(player)) {
            return false;
        }

        switch (action.getActionType()) {
            case EXPLORE:
                return canPlayerExplore(player);
            case ROTATE_CARD:
            case PLACE_CARD:
                return player.getCurrentCard() != null;
            case EXIT_DUNGEON:
                return canPlayerExit(player, ((ExitAction) action).getExitPath());
            case SLEEP:
                return true;
            default:
                return false;
        }
    }
}
