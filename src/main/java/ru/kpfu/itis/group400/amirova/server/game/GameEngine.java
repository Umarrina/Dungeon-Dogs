package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.GameSender;
import ru.kpfu.itis.group400.amirova.server.GameSerializer;
import ru.kpfu.itis.group400.amirova.server.GameServer;
import ru.kpfu.itis.group400.amirova.server.MessageHandler;
import ru.kpfu.itis.group400.amirova.server.game.model.GameSetupResult;
import ru.kpfu.itis.group400.amirova.server.game.model.PlayerRoundState;
import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.actions.*;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameEngine {
    private GameState gameState;
    private GameSetupManager gameSetupManager;
    private RoundManager roundManager;
    private GameSerializer serializer;

    private GameServer gameServer;
    private GameSender notifier;

    public GameEngine(List<Player> players, GameServer server) {
        this.gameServer = server;
        this.notifier = new GameSender(server);
        gameSetupManager = new GameSetupManager(players);
        GameSetupResult gameSetupResult = gameSetupManager.setupGame();
        gameState = new GameState(players, gameSetupResult.getStartRoom(), gameSetupResult.getDeckRooms(), null, null);
        roundManager = new RoundManager(gameSetupResult.getDeckRooms(), gameSetupResult.getPlayers(), this, new MessageHandler(), notifier);
        serializer = new GameSerializer();
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

    public void dealCardToPlayer(Player player) {
        if (gameState.canDrawCard()) {
            player.getPlayerRoundState().setCurrentRoom(gameState.getFirstCard());
        } else {
            notifier.sendError(player, "Нельзя взять карту");
        }
    }

    public void processRotateCard(Player player) {
        PlayerRoundState state = player.getPlayerRoundState();
        Room currentRoom = state.getCurrentRoom();

        if (currentRoom == null) {
            notifier.sendError(player, "No room selected");
            return;
        }

        currentRoom.rotate();

        List<Position> availablePositions = gameState.getAvailablePositions(currentRoom);

        notifier.sendAvailablePositions(player, availablePositions);
    }

    public void processSleep(Player player) {
        PlayerRoundState state = player.getPlayerRoundState();

        if (state.getCurrentRoom() != null) {
            notifier.sendError(player, "На руке есть карта, невозможно совершить действие");
        }

        if (state.isHasExit() || state.isSlept()) {
            notifier.sendError(player, "Игрок уже вышел или спит");
            return;
        }

        state.setSlept(true);

        player.addMaxTokens(1);
        state.setCurrentTokens(state.getCurrentTokens() + 1);
    }

    public boolean canPlace(Player player, Position position) {
        PlayerRoundState state = player.getPlayerRoundState();
        if (state.getCurrentRoom() == null) {
            return false;
        }
        List<Position> availablePositions = gameState.getAvailablePositions(state.getCurrentRoom());
        if (!availablePositions.contains(position)) {
            return false;
        }
        return true;
    }

    public void processPlaceCard(Player player, Position position) {
        PlayerRoundState state = player.getPlayerRoundState();
        Room currentRoom = state.getCurrentRoom();

        if (!gameState.placeCard(currentRoom, position)) {
            notifier.sendError(player, "Невозможное разместить");
            return;
        }

        // устанавливаем позицию
        currentRoom.setPositionX(position.getX());
        currentRoom.setPositionY(position.getY());

        gameState.addPlacesRooms(currentRoom, position);

        state.setCurrentRoom(null);

    }

    public boolean processExit(Player player, List<Position> path) {
        List<Room> exitPath = recreatePathWithRoom(path);
        PlayerRoundState state = player.getPlayerRoundState();

        if (state.isHasExit() || state.isSlept()) {
            notifier.sendError(player, "Игрок уже вышел или спит");
            return false;
        }

        if (!isValidExitPath(exitPath)) {
            return false;
        }

        for (Room room : exitPath) {
            processRoomEffect(player, room);
        }

        state.setHasExit(true);
        state.setExitPath(exitPath);

        // если вышел первый игрок то активируем ограничение на кол-во карт
        if (gameState.getCardsAvailable() == Integer.MAX_VALUE) {
            gameState.setCardsAvailable(gameState.getPlayers().size() * 4);
            notifier.broadcast("Установлен лимит " + gameState.getCardsAvailable());
        }

        collectTrophies(player);

        return true;
    }

    private List<Room> recreatePathWithRoom(List<Position> path) {
        Map<Position, Room> rooms = gameState.getPlacesRooms();
        List<Room> availableRooms = new ArrayList<>();
        for (Position position : path) {
            if (rooms.containsKey(position)) {
                availableRooms.add(rooms.get(position));
            }
        }
        return availableRooms;
    }

    public boolean isValidExitPath(List<Room> exitPath) {
        if (exitPath == null || exitPath.isEmpty()) {
            return false;
        }

        // todo первое, всегда автоматически добавляй в exitpath старотовую руму
        // todo второе, реализуй флаги посещенная/непосещенная
        if (!exitPath.get(0).equals(gameState.getStartRoom())) {
            return false;
        }

        // проверяем что комнаты связаны
        for (int i = 1; i < exitPath.size(); i++) {
            Room current = exitPath.get(i);
            Room previous = exitPath.get(i-1);

            if (!areRoomsConnected(previous, current)) {
                return false;
            }
        }

        Room lastRoom = exitPath.get(exitPath.size() - 1);
        return hasExitToOutside(lastRoom);
    }

    private boolean areRoomsConnected(Room room1, Room room2) {
        int dx = Math.abs(room1.getPositionX() - room2.getPositionX());
        int dy = Math.abs(room1.getPositionY() - room2.getPositionY());

        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    private boolean hasExitToOutside(Room room) {
        return Math.abs(room.getPositionX()) > 4 || Math.abs(room.getPositionY()) > 4;
    }


    public void processEndOfRound(int round) {
        System.out.println("Раунд " + round + " завершен");
        for (Player p : gameState.getPlayers()) {
            p.getPlayerRoundState().setHasExit(false);
            p.getPlayerRoundState().setSlept(false);
            p.getPlayerRoundState().setCurrentRoom(null);
            p.getPlayerRoundState().setCurrentTokens(p.getMaxTokens());
            p.getPlayerRoundState().setCurrentHealth(p.getDog().getMaxHealth());
            collectTrophies(p);
        }
    }

    public void calculateFinalScore() {
        //todo
    }

    //todo check logic
    private void collectTrophies(Player player) {

        PlayerRoundState state = player.getPlayerRoundState();
        if (state.getExitPath() != null) {
            for (Room room : state.getExitPath()) {
                if (room.getEventType() != ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.EventType.START) {
                    //player.getTrophyDeck().add(room);
                }
            }
        }
        //todo gameServer.sendMessage(player, "TROPHIES_COLLECTED");
    }

    private void processRoomEffect(Player player, Room room) {
        switch (room.getEventType()) {
            case START:
                break;
            case COIN:
                processCoinRoom();
                break;
            case ENEMY:
                processEnemyRoom();
                break;
            case ARTIFACT:
                processArtifactRoom();
                break;
            default:
                //todo send error
        }
    }

    private void processEnemyRoom() {

    }

    private void processCoinRoom() {}

    private void processArtifactRoom() {}

}
