package ru.kpfu.itis.group400.amirova.server;

import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageHandler {
    private final Map<Player, BlockingQueue<String>> playerQueues = new ConcurrentHashMap<>();

    public MessageHandler() {
    }

    public void registerPlayer(Player player) {
        playerQueues.putIfAbsent(player, new LinkedBlockingQueue());
    }

    public String waitResponse(Player player) throws InterruptedException {
        return playerQueues.get(player).take();
    }

    public void onRawMessageReceived(Player player, String message) {
        BlockingQueue<String> queue = playerQueues.get(player);
        if (queue != null) {
            try {
                queue.put(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
