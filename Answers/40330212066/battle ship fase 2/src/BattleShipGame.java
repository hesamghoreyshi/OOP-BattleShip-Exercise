import java.util.*;

class Ship {
    String name;
    int size;
    List<String> positions;
    boolean sunk;

    public Ship(String name, int size) {
        this.name = name;
        this.size = size;
        this.positions = new ArrayList<>();
        this.sunk = false;
    }

    public void placeShip(List<String> positions) {
        this.positions.addAll(positions);
    }

    public void isHit(String coordinate) {
        if (positions.contains(coordinate)) {
            positions.remove(coordinate);
            if (positions.isEmpty()) {
                sunk = true;
                System.out.println(name + " has been sunk!");
            }
        }
    }

    public boolean isSunk() {
        return sunk;
    }
}

class SpecialAttack {

    public static void radarScan(Board enemyBoard, String centerPos) {
        System.out.println("Radar scan activated at " + centerPos + "!");

        char col = centerPos.charAt(0);
        int row = Character.getNumericValue(centerPos.charAt(1));

        for (int i = row - 1; i <= row + 1; i++) {
            for (char j = (char) (col - 1); j <= col + 1; j++) {
                if (i >= 0 && i < enemyBoard.getSize() && j >= 'A' && j < 'A' + enemyBoard.getSize()) {
                    String pos = "" + j + i;
                    if (enemyBoard.ships.containsKey(pos)) {
                        System.out.println("Ship detected at: " + pos);
                    }
                }
            }
        }
    }

    public static void multiStrike(Board enemyBoard, List<String> coordinates) {
        System.out.println("Multi-Strike attack!");
        for (String coordinate : coordinates) {
            boolean hit = enemyBoard.attack(coordinate);
            System.out.println(coordinate + ": " + (hit ? "Hit!" : "Miss!"));
        }
    }
}

class Board {
    Map<String, Ship> ships;
    Set<String> hits;
    Set<String> misses;
    int size;

    public Board(int size) {
        this.size = size;
        ships = new HashMap<>();
        hits = new HashSet<>();
        misses = new HashSet<>();
    }

    public boolean placeShip(Ship ship, List<String> positions) {
        for (String pos : positions) {
            if (ships.containsKey(pos)) return false;
        }
        ship.placeShip(positions);
        for (String pos : positions) {
            ships.put(pos, ship);
        }
        return true;
    }

    public boolean attack(String coordinate) {
        if (ships.containsKey(coordinate)) {
            hits.add(coordinate);
            ships.get(coordinate).isHit(coordinate);
            return true;
        } else {
            misses.add(coordinate);
            return false;
        }
    }

    public void displayBoard(boolean revealShips) {
        System.out.print("  ");
        for (char j = 'A'; j < 'A' + size; j++) {
            System.out.print(j + " ");
        }
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.print(i + " ");
            for (char j = 'A'; j < 'A' + size; j++) {
                String pos = "" + j + i;
                if (hits.contains(pos)) {
                    System.out.print("X ");
                } else if (misses.contains(pos)) {
                    System.out.print("O ");
                } else if (revealShips && ships.containsKey(pos)) {
                    System.out.print("S ");
                } else {
                    System.out.print("~ ");
                }
            }
            System.out.println();
        }
    }

    public boolean allShipsSunk() {
        for (Ship ship : new HashSet<>(ships.values())) {
            if (!ship.isSunk()) return false;
        }
        return true;
    }

    public int getSize() {
        return size;
    }

    public void resetBoard() {
        ships.clear();
        hits.clear();
        misses.clear();
    }
}

class Player {
    String name;
    Board board;
    boolean specialAttackUsed;

    public Player(String name, int boardSize) {
        this.name = name;
        this.board = new Board(boardSize);
        this.specialAttackUsed = false;
    }

    public boolean takeTurn(Scanner scanner, Player opponent) {
        if (this instanceof AIPlayer) {
            return ((AIPlayer) this).takeTurn(scanner, opponent);
        }

        System.out.println(name + ", choose attack type: (1) Normal Attack (2) Radar Scan (3) Multi-Strike");
        int attackChoice = -1;
        while (attackChoice < 1 || attackChoice > 3) {
            if (scanner.hasNextInt()) {
                attackChoice = scanner.nextInt();
            } else {
                System.out.println("Invalid input. Please enter 1, 2, or 3.");
                scanner.next();
            }
        }

        switch (attackChoice) {
            case 1:
                System.out.println(name + ", enter attack coordinate (e.g., B5): ");
                String move = scanner.next().toUpperCase();
                boolean hit = opponent.board.attack(move);
                System.out.println(hit ? "Hit!" : "Miss!");
                opponent.board.displayBoard(false);
                break;
            case 2:
                if (!specialAttackUsed) {
                    System.out.println(name + ", enter the center coordinate for Radar Scan (e.g., B5): ");
                    String center = scanner.next().toUpperCase();
                    SpecialAttack.radarScan(opponent.board, center);
                    specialAttackUsed = true;
                } else {
                    System.out.println("You have already used a special attack this game!");
                    return takeTurn(scanner, opponent);
                }
                break;
            case 3:
                if (!specialAttackUsed) {
                    System.out.println(name + ", enter attack coordinates for Multi-Strike (e.g., B5 C7 D9): ");
                    scanner.nextLine(); // Clear buffer
                    String moves = scanner.nextLine().toUpperCase();
                    List<String> coordinates = Arrays.asList(moves.split(" "));
                    SpecialAttack.multiStrike(opponent.board, coordinates);
                    specialAttackUsed = true;
                } else {
                    System.out.println("You have already used a special attack this game!");
                    return takeTurn(scanner, opponent);
                }
                break;
            default:
                System.out.println("Invalid choice. Try again.");
        }
        return !opponent.board.allShipsSunk();
    }

    public void resetPlayer(int boardSize) {
        this.board.resetBoard();
        this.specialAttackUsed = false;
    }
}

class AIPlayer extends Player {
    public AIPlayer(String name, int boardSize) {
        super(name, boardSize);
    }

    @Override
    public boolean takeTurn(Scanner scanner, Player opponent) {
        Random rand = new Random();
        char col = (char) ('A' + rand.nextInt(opponent.board.getSize()));
        int row = rand.nextInt(opponent.board.getSize());
        String move = "" + col + row;
        System.out.println(name + " attacks: " + move);
        boolean hit = opponent.board.attack(move);
        System.out.println(hit ? "Hit!" : "Miss!");
        opponent.board.displayBoard(false);
        return !opponent.board.allShipsSunk();
    }
}


public class BattleShipGame {
    static final String[] SHIP_NAMES = {"Aircraft Carrier", "Battleship", "Submarine", "Patrol Boat"};
    static final int[] SHIP_SIZES = {5, 4, 3, 2};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean playAgain = true;

        while (playAgain) {
            System.out.println("Enter board size (minimum 5, maximum 15): ");
            int boardSize;
            while (true) {
                boardSize = scanner.nextInt();
                if (boardSize >= 5 && boardSize <= 15) break;
                System.out.println("Invalid size. Please enter a number between 5 and 15:");
            }
            System.out.println("Select game mode: (1) Two-player (2) One-player (with AI)");
            int gameMode = scanner.nextInt();

            Player player1 = new Player("Player 1", boardSize);
            Player player2;
            if (gameMode == 1) {
                player2 = new Player("Player 2", boardSize);
            } else {
                player2 = new AIPlayer("AI", boardSize);
            }

            setupBoard(scanner, player1);
            setupBoard(scanner, player2);

            boolean gameOn = true;
            while (gameOn) {
                gameOn = player1.takeTurn(scanner, player2);
                if (!gameOn) {
                    System.out.println(player1.name + " wins!");
                    break;
                }

                gameOn = player2.takeTurn(scanner, player1);
                if (!gameOn) {
                    System.out.println(player2.name + " wins!");
                }
            }

            System.out.println("Do you want to play again? (Y/N)");
            char choice = scanner.next().toUpperCase().charAt(0);
            if (choice != 'Y') {
                playAgain = false;
                System.out.println("Thank you for playing!");
            } else {
                player1.resetPlayer(boardSize);
                player2.resetPlayer(boardSize);
            }
        }
        scanner.close();
    }


    public static void setupBoard(Scanner scanner, Player player) {
        System.out.println(player.name + ", do you want to place ships manually or randomly? (M/R)");
        char choice = scanner.next().toUpperCase().charAt(0);

        if (choice == 'M') {
            System.out.println(player.name + ", place your ships manually.");
            for (int i = 0; i < SHIP_NAMES.length; i++) {
                Ship ship = new Ship(SHIP_NAMES[i], SHIP_SIZES[i]);
                boolean placed = false;
                while (!placed) {
                    System.out.println("Enter starting coordinate for " + SHIP_NAMES[i] + " (size " + SHIP_SIZES[i] + "): ");
                    String start = scanner.next().toUpperCase();
                    System.out.println("Horizontal (H) or Vertical (V)?");
                    char direction = scanner.next().toUpperCase().charAt(0);

                    if (direction != 'H' && direction != 'V') {
                        System.out.println("Invalid direction. Please enter 'H' for Horizontal or 'V' for Vertical.");
                        continue;
                    }
                    List<String> positions = generatePositions(start, direction, SHIP_SIZES[i], player.board.getSize());

                    if (positions != null && player.board.placeShip(ship, positions)) {
                        placed = true;
                    } else {
                        System.out.println("Invalid placement. Try again.");
                    }
                }

            }
        } else {
            System.out.println(player.name + " is placing ships randomly...");
            Random rand = new Random();
            for (int i = 0; i < SHIP_NAMES.length; i++) {
                Ship ship = new Ship(SHIP_NAMES[i], SHIP_SIZES[i]);
                boolean placed = false;
                while (!placed) {
                    char col = (char) ('A' + rand.nextInt(player.board.getSize()));
                    int row = rand.nextInt(player.board.getSize());
                    char direction = rand.nextBoolean() ? 'H' : 'V';
                    List<String> positions = generatePositions("" + col + row, direction, SHIP_SIZES[i], player.board.getSize());
                    if (positions != null && player.board.placeShip(ship, positions)) {
                        placed = true;
                    }
                }
            }
        }
        System.out.println(player.name + "'s board:");
        player.board.displayBoard(true);
    }

    public static List<String> generatePositions(String start, char direction, int size, int boardSize) {
        List<String> positions = new ArrayList<>();
        char col = start.charAt(0);
        int row = Character.getNumericValue(start.charAt(1));

        for (int i = 0; i < size; i++) {
            if (direction == 'H') {
                if (col + i >= 'A' + boardSize) return null;
                positions.add("" + (char) (col + i) + row);
            } else {
                if (row + i >= boardSize) return null;
                positions.add("" + col + (row + i));
            }
        }
        return positions;
    }
}
