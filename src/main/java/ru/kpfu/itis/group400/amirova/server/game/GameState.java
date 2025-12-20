package ru.kpfu.itis.group400.amirova.server.game;

import ru.kpfu.itis.group400.amirova.server.game.model.Position;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.ArtifactRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.CoinRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.EnemyRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.StartRoom;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Direction;
import ru.kpfu.itis.group400.amirova.server.game.model.cards.rooms.base.Room;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;
import ru.kpfu.itis.group400.amirova.server.game.model.players.Player;

import java.util.*;

public class GameState {
    private List<Player> players;
    private StartRoom startRoom;
    private DeckRooms deckRooms;
    private int cardsAvailable;

    private Map<Position, Integer> placedCards = new HashMap<>();

    public GameState(List<Player> players, StartRoom startRoom, DeckRooms deckRooms) {
        this.players = players;
        this.startRoom = startRoom;
        this.deckRooms = deckRooms;
        cardsAvailable = Integer.MAX_VALUE;


        Position startPos = new Position(0, 0);
        placedCards.put(startPos, startRoom.getId());
        startRoom.setPositionX(0);
        startRoom.setPositionY(0);
    }

    public void addCardToBoard(int cardId, Position position) {
        placedCards.put(position, cardId);
    }

    public Integer getCardIdAtPosition(Position position) {
        return placedCards.get(position);
    }

    public Map<Position, Integer> getPlacedCards() {
        return Collections.unmodifiableMap(placedCards);
    }

    private Room getRoomById(int cardId, DeckRooms deckRooms) {
        if (cardId == startRoom.getId()) {
            return startRoom;
        }
        return deckRooms.getCardById(cardId);
    }

    public List<Position> getAvailablePositions(int cardId, DeckRooms deckRooms) {
        Room card = deckRooms.getCardById(cardId);
        if (card == null) {
            System.out.println("Карта с ID " + cardId + " не найдена в колоде");
            return new ArrayList<>();
        }

        Room rotatedCard = createRotatedCard(card);

        return getAvailablePositions(rotatedCard, deckRooms);
    }

    public List<Position> getAvailablePositions(Room card, DeckRooms deckRooms) {
        List<Position> positions = new ArrayList<>();

        if (card == null) {
            return positions;
        }

        for (Map.Entry<Position, Integer> entry : placedCards.entrySet()) {
            Position placedPos = entry.getKey();
            Integer placedCardId = entry.getValue();
            Room placedCard = getRoomById(placedCardId, deckRooms);

            if (placedCard == null) {
                continue;
            }

            for (Direction dir : Direction.values()) {
                if (Boolean.TRUE.equals(placedCard.getExits().get(dir))) {
                    Position neighborPos = getNeighborPosition(placedPos, dir);

                    if (placedCards.containsKey(neighborPos)) {
                        continue;
                    }
                    Direction oppositeDir = getOppositeDirection(dir);
                    if (Boolean.TRUE.equals(card.getExits().get(oppositeDir))) {
                        positions.add(neighborPos);
                    }
                }
            }
        }

        return positions;
    }

    private Room createRotatedCard(Room originalCard) {
        Room copy = null;

        try {
            if (originalCard instanceof EnemyRoom) {
                EnemyRoom enemy = (EnemyRoom) originalCard;
                copy = new EnemyRoom(
                        enemy.getId(),
                        enemy.getName(),
                        enemy.getExits(),
                        enemy.getRequiredDamage(),
                        enemy.getDamage(),
                        enemy.getGivenCharacteristics(),
                        enemy.getCoins()
                );
            } else if (originalCard instanceof CoinRoom) {
                CoinRoom coin = (CoinRoom) originalCard;
                copy = new CoinRoom(
                        coin.getId(),
                        coin.getName(),
                        coin.getExits(),
                        coin.getCountCoins()
                );
            } else if (originalCard instanceof ArtifactRoom) {
                ArtifactRoom artifact = (ArtifactRoom) originalCard;
                copy = new ArtifactRoom(
                        artifact.getId(),
                        artifact.getName(),
                        artifact.getExits(),
                        artifact.getDamage()
                );
            } else if (originalCard instanceof StartRoom) {
                StartRoom start = (StartRoom) originalCard;
                copy = new StartRoom(start.getId(), start.getName(), start.getExits());
            }

            if (copy != null) {
                copy.setRotation(originalCard.getRotation());

                int rotations = originalCard.getRotation() / 90;
                for (int i = 0; i < rotations; i++) {
                    copy.rotate();
                }
            }
        } catch (Exception e) {
            return originalCard;
        }

        return copy != null ? copy : originalCard;
    }
    private Position getNeighborPosition(Position pos, Direction dir) {
        switch (dir) {
            case TOP: return new Position(pos.getX(), pos.getY() - 1);
            case BOTTOM: return new Position(pos.getX(), pos.getY() + 1);
            case LEFT: return new Position(pos.getX() - 1, pos.getY());
            case RIGHT: return new Position(pos.getX() + 1, pos.getY());
            default: return pos;
        }
    }

    private Direction getOppositeDirection(Direction dir) {
        switch (dir) {
            case TOP: return Direction.BOTTOM;
            case BOTTOM: return Direction.TOP;
            case LEFT: return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
            default: return dir;
        }
    }

    private boolean hasExitToOutside(Position pos, Room room) {
        for (Direction dir : Direction.values()) {
            if (Boolean.TRUE.equals(room.getExits().get(dir))) {
                Position neighbor = getNeighborPosition(pos, dir);
                if (!placedCards.containsKey(neighbor)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Position> getExitPositions() {
        List<Position> exits = new ArrayList<>();

        for (Map.Entry<Position, Integer> entry : placedCards.entrySet()) {
            Position pos = entry.getKey();
            Integer cardId = entry.getValue();

            if (cardId == null) {
                continue;
            }

            Room room = deckRooms.getCardById(cardId);
            if (room == null) {
                continue;
            }

            if (hasExitToOutside(pos, room)) {
                exits.add(pos);
            }
        }

        return exits;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public DeckRooms getDeckRooms() {
        return deckRooms;
    }

    public int getCardsAvailable() {
        return cardsAvailable;
    }

    public void setCardsAvailable(int cardsAvailable) {
        this.cardsAvailable = cardsAvailable;
    }

    public void resetDeckLimit() {
        cardsAvailable = Integer.MAX_VALUE;
    }

    public void clearBoardExceptStart() {
        Position startPos = new Position(0, 0);
        Integer startCardId = placedCards.get(startPos);
        placedCards.clear();
        if (startCardId != null) {
            placedCards.put(startPos, startCardId);
        }
    }
}
