package game_2048;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

//========================================================================//
public class Gameboard extends HighScore {

    //Direções
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int UP = 2;
    public static final int DOWN = 3;

    //Board será 4x4
    public static final int ROWS = 4;
    public static final int COLUMNS = 4;

    //Numero de blocos iniciais 
    private int startingTile = 2;

    //Matriz para o jogo
    private Tile[][] board;

    //Verifica e vitoria
    private boolean winGame;

    //Para criar Gameboard
    private BufferedImage gameBoard;

    //Cores de fundo do board e do bloco
    private Color backgroundBoard = new Color(0x776E65);
    private Color backgroundTile = new Color(0xD8BFD8);

    //Posição para desenhar na tela
    private int x;
    private int y;

    //Espaçamento entre as peças
    private static int SPACING = 10;

    //Altura e largura em pixel do board
    public static int BOARD_WIDTH = (COLUMNS + 1) * SPACING + COLUMNS * Tile.WIDTH;
    public static int BOARD_HEIGHT = (ROWS + 1) * SPACING + ROWS * Tile.HEIGHT;

    //Verifica se o jogo iniciou
    private boolean gameStarted;

    private Button newGame;
    private MouseEvent e;

    //========================================================================//
    public Gameboard(int x, int y) {
        scoreFont = Game.main.deriveFont(30f);

        this.x = x;
        this.y = y;

        board = new Tile[ROWS][COLUMNS];   //Board 4x4

        gameBoard = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);

        loadScore();
        createBoardImage();
        startGame();
    }

    //========================================================================//
    //funcao que checa updates, seja de teclas pressionadas, um novo highscore, ou se houve vitoria
    public void update() {
        typedKeysLeft();
        typedKeysRight();
        typedKeysUp();
        typedKeysDown();

        if (currentScore >= highScore) {
            highScore = currentScore;
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Tile current = board[row][col];
                if (current == null) {
                    continue;
                }
                current.update();
                //reset the position
                resetPosition(current, row, col);
                if (current.getValue() == 2048) {
                    winGame = true;
                }
            }
        }
    }

    //========================================================================//
    //Desenha coisas como o Score atual, e o recorde(highscore) salvo no arquivo
    public void draw(Graphics2D g) {

        BufferedImage BI = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) BI.getGraphics();
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g2d.drawImage(gameBoard, 0, 0, null);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Tile currently = board[row][col];
                if (currently == null) {
                    continue;
                }
                currently.draw(g2d);
            }
        }

        g.drawImage(BI, x, y, null);
        g2d.dispose();

        g.setColor(scoreColor);
        g.setFont(scoreFont);
        g.drawString("PONTUAÇÃO: " + currentScore, 30, 40);   //g.drawString(score, horizontal, vertical);
        g.setColor(bestColor);
        g.drawString("RECORDE: " + highScore, Game.WIDTH - MessageSize.getMessageWidth("RECORDE", scoreFont, g) - 341, 80);
    }

    //==============================RESET=====================================//
    //Reseta informações quando o usuário selecionar jogar novamente
    public void resetBoard() {
        board = new Tile[ROWS][COLUMNS];
        startGame();
        winGame = false;
        gameStarted = false;
        currentScore = 0;
    }

    public void resetBoardEasterEgg() {
        board = new Tile[ROWS][COLUMNS];
        startEasterEgg();
        winGame = false;
        gameStarted = false;
        currentScore = 0;
    }

    //========================================================================//
    //Cria background do Board
    private void createBoardImage() {
        Graphics2D g = (Graphics2D) gameBoard.getGraphics();

        //Cor de fundo do Board
        g.setColor(backgroundBoard);
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        //Cor de fundo de cada bloco 
        g.setColor(backgroundTile);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int x = SPACING + SPACING * col + Tile.WIDTH * col;
                int y = SPACING + SPACING * row + Tile.HEIGHT * row;
                g.fillRect(x, y, Tile.WIDTH, Tile.HEIGHT);
            }
        }

    }

    //========================================================================//
    //Inicia spawnando blocos 2 , 4
    private void startGame() {
        for (int i = 0; i < startingTile; i++) {
            spawnRandomTile(2, 4);
        }

    }

    //inicia spawnando blocos 1024 , 1024
    private void startEasterEgg() {
        for (int i = 0; i < startingTile; i++) {
            spawnRandomTile(1024, 1024);
        }

    }

    //========================================================================//
    //Spawn randomico
    private void spawnRandomTile(int value1, int value2) {
        Random random = new Random();
        int location = random.nextInt(ROWS * COLUMNS);
        int row = 0, col = 0;

        do {
            location = (location + 1) % (ROWS * COLUMNS);
            row = location / ROWS;
            col = location % COLUMNS;
        } while (board[row][col] != null);

        //80% de chance de spawnar 2 / 20% de chance de spawnar 4
        int value = random.nextInt(10);
        if (value < 8) {
            value = value1;
        } else {
            value = value2;
        }

        //Posição recebe a tile
        board[row][col] = new Tile(value, getTileX(col), getTileY(row));

    }

    //========================================================================//
    //Arruma a velocidade para nao atrapalhar/bugar a animação de movimentacao
    private void resetPosition(Tile current, int row, int col) {
        if (current == null) {
            return;
        }

        int x = getTileX(col);
        int y = getTileY(row);
        int distX = current.getX() - x;
        int distY = current.getY() - y;

        if (Math.abs(distX) < Tile.SPEED) {
            current.setX(current.getX() - distX); //para nao ter flic na animacao 
        }
        if (Math.abs(distY) < Tile.SPEED) {
            current.setY(current.getY() - distY); //para nao ter flic na animacao 
        }

        if (distX < 0) {
            current.setX(current.getX() + Tile.SPEED);
        }
        if (distY < 0) {
            current.setY(current.getY() + Tile.SPEED);
        }
        if (distX > 0) {
            current.setX(current.getX() - Tile.SPEED);
        }
        if (distY > 0) {
            current.setY(current.getY() - Tile.SPEED);
        }
    }

    //========================================================================//
    //Checa se é possivel mover as peças
    private boolean move(int row, int col, int horizontalDirection, int verticalDirection, int dir) {
        boolean canMove = false;

        Tile tile = board[row][col];
        if (tile == null) {
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
            if (board[newRow][newCol] == null) {
                board[newRow][newCol] = tile;
                board[newRow - verticalDirection][newCol - horizontalDirection] = null;

                board[newRow][newCol].setSlideTo(new Point(newRow, newCol));
                canMove = true;
            } else if (board[newRow][newCol].getValue() == tile.getValue() && board[newRow][newCol].iscanCombine()) { //se da pra combinar
                board[newRow][newCol].setCanCombine(false);
                board[newRow][newCol].setValue(board[newRow][newCol].getValue() * 2);
                canMove = true;
                board[newRow - verticalDirection][newCol - horizontalDirection] = null;
                board[newRow][newCol].setSlideTo(new Point(newRow, newCol));

                board[newRow][newCol].setCombineAnimation(true);

                currentScore = currentScore + board[newRow][newCol].getValue();
            } else {            //senao estiver vazio ou nao poder unir com outro tile
                move = false;
            }
        }

        return canMove;
    }

    //========================================================================//
    //Checa limite do board
    private boolean outOfBounds(int dir, int row, int col) {
        if (dir == LEFT) {
            return col < 0;
        } else if (dir == RIGHT) {
            return col > COLUMNS - 1;
        } else if (dir == UP) {
            return row < 0;
        } else if (dir == DOWN) {
            return row > ROWS - 1;
        }
        return false;
    }

    //========================================================================//
    //Move as peças do jogo
    private void moveLeft() {
        boolean canMove = false;
        int horizontal = -1;
        int vertical = 0;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (!canMove) {
                    canMove = move(i, j, horizontal, vertical, LEFT);
                } else {
                    move(i, j, horizontal, vertical, LEFT);
                }
            }
        }
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                Tile current = board[i][j];
                if (current == null) {
                    continue;
                }
                current.setCanCombine(true);
            }
        }
        if (canMove) {
            spawnRandomTile(2, 4);
            checkDead();
            checkWin();
        }
    }

    private void moveRight() {
        boolean canMove = false;
        int horizontal = 1;
        int vertical = 0;

        for (int i = 0; i < ROWS; i++) {
            for (int j = COLUMNS - 1; j >= 0; j--) {
                if (!canMove) {
                    canMove = move(i, j, horizontal, vertical, RIGHT);
                } else {
                    move(i, j, horizontal, vertical, RIGHT);
                }
            }
        }
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                Tile current = board[i][j];
                if (current == null) {
                    continue;
                }
                current.setCanCombine(true);
            }
        }
        if (canMove) {
            spawnRandomTile(2, 4);
            checkDead();
            checkWin();
        }
    }

    private void moveUp() {
        boolean canMove = false;
        int horizontal = 0;
        int vertical = -1;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                if (!canMove) {
                    canMove = move(i, j, horizontal, vertical, UP);
                } else {
                    move(i, j, horizontal, vertical, UP);
                }
            }
        }
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                Tile current = board[i][j];
                if (current == null) {
                    continue;
                }
                current.setCanCombine(true);
            }
        }
        if (canMove) {
            spawnRandomTile(2, 4);
            checkDead();
            checkWin();
        }
    }

    private void moveDown() {
        boolean canMove = false;
        int horizontal = 0;
        int vertical = 1;

        for (int i = ROWS - 1; i >= 0; i--) {
            for (int j = 0; j < COLUMNS; j++) {
                if (!canMove) {
                    canMove = move(i, j, horizontal, vertical, DOWN);
                } else {
                    move(i, j, horizontal, vertical, DOWN);
                }
            }
        }
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                Tile current = board[i][j];
                if (current == null) {
                    continue;
                }
                current.setCanCombine(true);
            }
        }
        if (canMove) {
            spawnRandomTile(2, 4);
            checkDead();
            checkWin();
        }
    }

    //========================================================================//
    /*
     Checa fim do jogo, percorre todas as peças checando a combinação nos arredores
     Se não houver cobinação possível, fim do jogo */
    public boolean checkDead() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (board[row][col] == null) {
                    return false;
                }
                if (checkSurroundingTiles(row, col, board[row][col])) {
                    return false;
                }
            }
        }

        if (currentScore >= highScore) {
            highScore = currentScore;
        }
        setScore();

        return true;
    }

    //========================================================================//
    //Checa se houve vitoria
    public boolean checkWin() {
        if (winGame) {
            return winGame;
        }
        return false;
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
        if (col < COLUMNS - 1) {
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
    private void typedKeysLeft() {
        if (!winGame) {       //se o jogo for ganho nao permite o jogardor mover a peças
            if (KeyboardInput.keyTyped(KeyEvent.VK_LEFT)) {
                moveLeft();
                if (!gameStarted) {
                    gameStarted = true;
                }
            }
            if (KeyboardInput.keyTyped(KeyEvent.VK_A)) {
                moveLeft();
                if (!gameStarted) {
                    gameStarted = true;
                }
            }
        }
    }

    private void typedKeysRight() {
        if (!winGame) {
            if (KeyboardInput.keyTyped(KeyEvent.VK_RIGHT)) {
                moveRight();
                if (!gameStarted) {
                    gameStarted = true;
                }
            }

            if (KeyboardInput.keyTyped(KeyEvent.VK_D)) {
                moveRight();
                if (!gameStarted) {
                    gameStarted = true;
                }
            }
        }
    }

    private void typedKeysUp() {
        if (!winGame) {
            if (KeyboardInput.keyTyped(KeyEvent.VK_UP)) {
                moveUp();
                if (!gameStarted) {
                    gameStarted = true;
                }
            }

            if (KeyboardInput.keyTyped(KeyEvent.VK_W)) {
                moveUp();
                if (!gameStarted) {
                    gameStarted = true;
                }
            }
        }
    }

    private void typedKeysDown() {
        if (!winGame) {
            if (KeyboardInput.keyTyped(KeyEvent.VK_DOWN)) {
                moveDown();
                if (!gameStarted) {
                    gameStarted = true;
                }
            }

            if (KeyboardInput.keyTyped(KeyEvent.VK_S)) {
                moveDown();
                if (!gameStarted) {
                    gameStarted = true;
                }
            }
        }
    }

    //========================GETTERS e SETTERS===============================//
    public int getTileX(int col) {
        return SPACING + col * Tile.WIDTH + col * SPACING;
    }

    public int getTileY(int row) {
        return SPACING + row * Tile.HEIGHT + row * SPACING;
    }

}
