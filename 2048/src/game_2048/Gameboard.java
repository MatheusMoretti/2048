package game_2048;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.*;

public class Gameboard extends HighScore {

    public enum Direction {

        LEFT, RIGHT, UP, DOWN
    }

    //Board será 4x4
    public static final int ROWS = 4;
    public static final int COLS = 4;

    private int startingTile = 2; //Numero de blocos iniciais 
    private Tile[][] board;

    private boolean dead; //Verifica derrota      
    private boolean won;  //Verifica vitória  

    private BufferedImage gameBoard; //Background gameBoard
    private BufferedImage finalBoard;

    private Color backgroundBoard;  //Cor do backgrond
    private Color backgroundRect;  //Cor dos retangulos

    //Posição para desenhar na tela
    private int x;
    private int y;

    private static int SPACING = 10; //Espaço entre as peças do jogo

    //Pega altura e largura em pixel do board
    public static int BOARD_WIDTH = (COLS + 1) * SPACING + COLS * Tile.WIDTH;
    public static int BOARD_HEIGHT = (ROWS + 1) * SPACING + ROWS * Tile.HEIGHT;

    private boolean hasStarted;

    //========================================================================//
    public Gameboard(int x, int y) {
        try {
            saveFile = Gameboard.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }

        scoreFont = Game.main.deriveFont(30f);

        this.x = x;
        this.y = y;

        board = new Tile[ROWS][COLS];   //Board 4x4

        gameBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        finalBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);

        loadHighScore();
        createBoardImage();
        start();
    }

    //========================================================================//
    public void update() {
        checkTypedKeys();

        if (currentScore >= highScore) {
            highScore = currentScore;
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Tile current = board[row][col];
                if (current == null) {
                    continue;
                }
                current.update();
                //reset the position
                resetPosition(current, row, col);
                if (current.getValue() == 2048) {
                    won = true;
                }
            }
        }
    }

    //========================================================================//
    public void render(Graphics2D g) {
        Graphics2D g2d = (Graphics2D) finalBoard.getGraphics();
        g2d.drawImage(gameBoard, 0, 0, null);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Tile currently = board[row][col];
                if (currently == null) {
                    continue;
                }
                currently.render(g2d);
            }
        }

        g.drawImage(finalBoard, x, y, null);
        g2d.dispose();

        g.setColor(Color.lightGray);
        g.setFont(scoreFont);
        g.drawString("Score: " + currentScore, 30, 40);   //g.drawString(score, vertical, horizontal);
        g.setColor(Color.red);
        g.drawString("Best: " + highScore, Game.WIDTH - DrawUtils.getMessageWidth("", scoreFont, g) - 470, 80);
    }

    //========================================================================//
    //Cria background do Board
    private void createBoardImage() {
        Graphics2D g = (Graphics2D) gameBoard.getGraphics();
        backgroundBoard = new Color(0x776E65); //Cor de fundo do Board
        g.setColor(backgroundBoard);

        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        backgroundRect = new Color(0xD8BFD8); //Cor de fundo do retangulo
        g.setColor(backgroundRect);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = SPACING + SPACING * col + Tile.WIDTH * col;
                int y = SPACING + SPACING * row + Tile.HEIGHT * row;
                g.fillRoundRect(x, y, Tile.WIDTH, Tile.HEIGHT, 0, 0);
            }
        }

    }

    //========================================================================//
    private void start() {
        for (int i = 0; i < startingTile; i++) {
            spawnRandom();
        }

    }

    //========================================================================//
    /*
     Spawn randomico de blocos
     Checa todas as posições do board e escolhe uma randomicamente
     */
    private void spawnRandom() {
        Random random = new Random();
        int location = random.nextInt(ROWS * COLS);
        int row = 0, col = 0;

        do {
            location = (location + 1) % (ROWS * COLS);
            row = location / ROWS;
            col = location % COLS;
        } while (board[row][col] != null);

        //80% de chance de spawnar 2 / 20% de chance de spawnar 4
        int value = random.nextInt(10) < 8 ? 2 : 4;

        //Posição recebe a tile
        board[row][col] = new Tile(value, getTileX(col), getTileY(row));

    }

    //========================================================================//
    private void resetPosition(Tile current, int row, int col) {
        if (current == null) {
            return;
        }

        int x = getTileX(col);
        int y = getTileY(row);
        int distX = current.getX() - x;
        int distY = current.getY() - y;

        if (Math.abs(distX) < Tile.TILE_SPEED) {
            current.setX(current.getX() - distX); //para nao ter flic na animacao 
        }
        if (Math.abs(distY) < Tile.TILE_SPEED) {
            current.setY(current.getY() - distY); //para nao ter flic na animacao 
        }

        if (distX < 0) {
            current.setX(current.getX() + Tile.TILE_SPEED);
        }
        if (distY < 0) {
            current.setY(current.getY() + Tile.TILE_SPEED);
        }
        if (distX > 0) {
            current.setX(current.getX() - Tile.TILE_SPEED);
        }
        if (distY > 0) {
            current.setY(current.getY() - Tile.TILE_SPEED);
        }
    }

    //========================================================================//
    //Checa se é possivel mover as peças
    private boolean move(int row, int col, int horizontalDirection, int verticalDirection, Direction dir) {
        boolean canMove = false;

        Tile current = board[row][col];
        if (current == null) {
            return false;
        }
        boolean move = true;
        int newCol = col;
        int newRow = row;

        while (move) {
            newCol = newCol + horizontalDirection;
            newRow = newRow + verticalDirection;
            if (outOfBounds(dir, newRow, newCol)) {
                break;
            }
            if (board[newRow][newCol] == null) { //se esta vazio
                board[newRow][newCol] = current;
                board[newRow - verticalDirection][newCol - horizontalDirection] = null;

                board[newRow][newCol].setSlideTo(new Point(newRow, newCol));
                canMove = true;
            } else if (board[newRow][newCol].getValue() == current.getValue() && board[newRow][newCol].iscanCombine()) { //se da pra combinar
                board[newRow][newCol].setCanCombine(false);
                board[newRow][newCol].setValue(board[newRow][newCol].getValue() * 2);
                canMove = true;
                board[newRow - verticalDirection][newCol - horizontalDirection] = null;
                board[newRow][newCol].setSlideTo(new Point(newRow, newCol));

                //if (board[newRow][newCol].getValue() == 4) {
                board[newRow][newCol].setCombineAnimation(true);
                //}

                currentScore = currentScore + board[newRow][newCol].getValue();
            } else {
                move = false;
            }
        }

        return canMove;
    }

    //========================================================================//
    //Checa limite do board
    private boolean outOfBounds(Direction dir, int row, int col) {
        if (dir == Direction.LEFT) {
            return col < 0;
        } else if (dir == Direction.RIGHT) {
            return col > COLS - 1;
        } else if (dir == Direction.UP) {
            return row < 0;
        } else if (dir == Direction.DOWN) {
            return row > ROWS - 1;
        }
        return false;
    }

    //========================================================================//
    //Move as peças do jogo
    private void moveTiles(Direction dir) {
        boolean canMove = false;
        int horizontalDirection = 0;
        int verticalDirection = 0;

        if (dir == Direction.LEFT) {
            horizontalDirection = -1;
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (!canMove) {
                        canMove = move(row, col, horizontalDirection, verticalDirection, dir);
                    } else {
                        move(row, col, horizontalDirection, verticalDirection, dir);
                    }
                }
            }
        } else if (dir == Direction.RIGHT) {
            horizontalDirection = +1;
            for (int row = 0; row < ROWS; row++) {
                for (int col = COLS - 1; col >= 0; col--) {
                    if (!canMove) {
                        canMove = move(row, col, horizontalDirection, verticalDirection, dir);
                    } else {
                        move(row, col, horizontalDirection, verticalDirection, dir);
                    }
                }
            }
        } else if (dir == Direction.UP) {
            verticalDirection = -1;
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (!canMove) {
                        canMove = move(row, col, horizontalDirection, verticalDirection, dir);
                    } else {
                        move(row, col, horizontalDirection, verticalDirection, dir);
                    }
                }
            }
        } else if (dir == Direction.DOWN) {
            verticalDirection = 1;
            for (int row = ROWS - 1; row >= 0; row--) {
                for (int col = 0; col < COLS; col++) {
                    if (!canMove) {
                        canMove = move(row, col, horizontalDirection, verticalDirection, dir);
                    } else {
                        move(row, col, horizontalDirection, verticalDirection, dir);
                    }
                }
            }
        } else {
            System.out.println(dir + "is not a valid direction.");
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Tile current = board[row][col];
                if (current == null) {
                    continue;
                }
                current.setCanCombine(true);
            }
        }
        if (canMove) {
            spawnRandom();
            checkDead();
        }

    }

    //========================================================================//
    /*
     Checa fim do jogo
     Percorre todas as peças checando a combinação nos arredores
     Se não houver cobinação possível -> end game
     */
    private void checkDead() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] == null) {
                    return;
                }
                if (checkSurroundingTiles(row, col, board[row][col])) {
                    return;
                }
            }
        }
        dead = true;

        if (currentScore >= highScore) {
            highScore = currentScore;
        }
        setHighScore();
    }

    //========================================================================//
    //Checa arredores das peças
    private boolean checkSurroundingTiles(int row, int col, Tile current) {
        if (row > 0) {
            Tile check = board[row - 1][col];
            if (check == null) {
                return true;
            }
            if (current.getValue() == check.getValue()) {
                return true;
            }
        }
        if (row < ROWS - 1) {
            Tile check = board[row + 1][col];
            if (check == null) {
                return true;
            }
            if (current.getValue() == check.getValue()) {
                return true;
            }
        }
        if (col > 0) {
            Tile check = board[row][col - 1];
            if (check == null) {
                return true;
            }
            if (current.getValue() == check.getValue()) {
                return true;
            }
        }
        if (col < COLS - 1) {
            Tile check = board[row][col + 1];
            if (check == null) {
                return true;
            }
            if (current.getValue() == check.getValue()) {
                return true;
            }
        }
        return false;
    }

    //========================================================================//
    //Checa qual tecla foi pressionada para mover a peça
    private void checkTypedKeys() {
        //LEFT
        if (Keyboard.typed(KeyEvent.VK_LEFT)) {
            moveTiles(Direction.LEFT);
            if (!hasStarted) {
                hasStarted = true;
            }
        }
        if (Keyboard.typed(KeyEvent.VK_A)) {
            moveTiles(Direction.LEFT);
            if (!hasStarted) {
                hasStarted = true;
            }
        }

        //RIGHT
        if (Keyboard.typed(KeyEvent.VK_RIGHT)) {
            moveTiles(Direction.RIGHT);
            if (!hasStarted) {
                hasStarted = true;
            }
        }
        if (Keyboard.typed(KeyEvent.VK_D)) {
            moveTiles(Direction.RIGHT);
            if (!hasStarted) {
                hasStarted = true;
            }
        }

        //UP
        if (Keyboard.typed(KeyEvent.VK_UP)) {
            moveTiles(Direction.UP);
            if (!hasStarted) {
                hasStarted = true;
            }
        }
        if (Keyboard.typed(KeyEvent.VK_W)) {
            moveTiles(Direction.UP);
            if (!hasStarted) {
                hasStarted = true;
            }
        }

        //DOWN
        if (Keyboard.typed(KeyEvent.VK_DOWN)) {
            moveTiles(Direction.DOWN);
            if (!hasStarted) {
                hasStarted = true;
            }
        }
        if (Keyboard.typed(KeyEvent.VK_S)) {
            moveTiles(Direction.DOWN);
            if (!hasStarted) {
                hasStarted = true;
            }
        }
    }

    //========================================================================//
    //Getters e Setters
    public int getTileX(int col) {
        return SPACING + col * Tile.WIDTH + col * SPACING;
    }

    public int getTileY(int row) {
        return SPACING + row * Tile.HEIGHT + row * SPACING;
    }

    //========================================================================//
}
