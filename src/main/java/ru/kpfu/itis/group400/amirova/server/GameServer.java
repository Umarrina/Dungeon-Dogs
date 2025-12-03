package ru.kpfu.itis.group400.amirova.server;

import ru.kpfu.itis.group400.amirova.server.game.GameEngine;
import ru.kpfu.itis.group400.amirova.server.game.GameState;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.IllegalFormatCodePointException;
import java.util.List;

public class GameServer {
    private GameEngine  gameEngine;
    private List<Player> players;
/*
* варианты запуска игры
* игра запускается когда плжклбчаются все 4 игрока (это всегда)
* игра запскается если есть хотя бы 2 игрока и за отведенное время не подкобчились все
* первый пдключенный - хост, который выбирает, когда начнем
* у всех кнопка, когда больше половины за начало игры, игра запускается
 */

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(5555);
            // прописываем логику подключения
            // важно закрыть сокет когда начинается игра
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        startGameProcess();
    }

    private void startGameProcess() {
        gameEngine = new GameEngine(players);
        gameEngine.startNewGame();
    }


}
