package ru.kpfu.itis.group400.amirova.server;

import ru.kpfu.itis.group400.amirova.server.game.GameEngine;
import ru.kpfu.itis.group400.amirova.server.game.GameInitializer;
import ru.kpfu.itis.group400.amirova.server.game.RoundManager;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.dogs.Dog;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
    private GameEngine  gameEngine;
    private RoundManager roundManager;
    private MessageHandler handler;
    private GameSender notifier;

    private List<ClientHandler> clientHandlers = new ArrayList<>();
    private List<Player> players = new  ArrayList<>();
    private ServerSocket socket;
    private Map<Player, ClientHandler> playerToHandler = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }

    private void start() {
        try {
            socket = new ServerSocket(5555);

            while (clientHandlers.size() < 2) {
                Socket clientSocket = socket.accept();

                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientHandlers.add(handler);
                new Thread(handler).start();
            }

            socket.close();

            initializeGameComponents();
            startGameProcess();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeGameComponents() {
        try {
            GameInitializer initializer = new GameInitializer();
            initializer.initializeAll();
            DeckRooms deckRooms = initializer.getDeckRooms();
            List<Dog> dogs = initializer.getDogs();

            for (Player p : players) {
                if (p.getDog() == null) {
                    int index = players.indexOf(p) % dogs.size();
                    p.setDog(dogs.get(index));
                }
                p.setPlayerRoundState();
                p.getPlayerRoundState().setCurrentTokens(5);
                p.getPlayerRoundState().setCurrentHealth(p.getDog().getMaxHealth());
            }

            this.gameEngine = new GameEngine(players, this);
            this.handler = new MessageHandler();
            this.notifier = new GameSender(this);
            this.roundManager = new RoundManager(deckRooms, players, gameEngine, handler, notifier);

            for(Player p : players) {
                handler.registerPlayer(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
            broadcastToAll("ERROR|Ошибка инициализации игры: " + e.getMessage());
        }
    }

    public synchronized void addPlayer(Player player, ClientHandler clientHandler) {
        players.add(player);
        playerToHandler.put(player, clientHandler);
    }

    public void handleClientMessage(Player player, String message) {
        handler.onRawMessageReceived(player, message);
    }

    private void startGameProcess() {
        broadcastToAll("GAME_START|Игра началась!");
        roundManager.startGame();
    }

    public void sendToPlayer(Player player, String message) {
        ClientHandler clientHandler = playerToHandler.get(player);
        if (clientHandler != null && clientHandler.out != null) {
            clientHandler.out.println(message);
        }
    }

    public void broadcastToAll(String message) {
        for (ClientHandler handler : clientHandlers) {
            if (handler.out != null) {
                handler.out.println(message);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private GameServer gameServer;
        private Player player;

        public ClientHandler(Socket clientSocket, GameServer gameServer) {
            this.clientSocket = clientSocket;
            this.gameServer = gameServer;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("от клиента: " + message);

                    if (message.startsWith("CONNECT|")) {
                        String username = message.split("\\|")[1];
                        this.player = new Player(username);
                        gameServer.addPlayer(player, this);
                        out.println("CONNECT_OK|" + username);
                    } else if (player != null) {
                        gameServer.handleClientMessage(player, message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка соединения с игроком " +
                        (player != null ? player.getUsername() : "unknown"));
            }
        }
    }
}