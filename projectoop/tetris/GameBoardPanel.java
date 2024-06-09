package pixelunity.projectoop.tetris;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import pixelunity.projectoop.tetris.Tetromino.Tetrominoes;

public class GameBoardPanel extends JPanel implements ActionListener {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 22;
    private static final int INITIAL_TIMER_RESOLUTION = 370;

    private Timer timer;
    private boolean isFallingDone = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int currentScore = 0;

    private int curX = 0;
    private int curY = 0;

    private Tetromino curBlock;
    private Tetrominoes[] gameBoard;
    private Color[] colorTable;

    private String currentStatus;
    private String currentLevel;
    private int currentTimerResolution;

    private GameWindow tetrisFrame;

    public GameBoardPanel(GameWindow tetrisFrame, int timerResolution) {
        setFocusable(true);
        setBackground(new Color(0, 30, 30));
        curBlock = new Tetromino();
        timer = new Timer(timerResolution, this);
        timer.start();
        currentTimerResolution = timerResolution;

        gameBoard = new Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];

        colorTable = new Color[]{
                new Color(0, 0, 0), new Color(164, 135, 255),
                new Color(255, 128, 0), new Color(255, 0, 0),
                new Color(32, 128, 255), new Color(255, 0, 255),
                new Color(255, 255, 0), new Color(0, 255, 0)
        };

        addKeyListener(new TetrisKeyAdapter());
        this.tetrisFrame = tetrisFrame;
        initBoard();
    }

    private void setResolution() {
        int level = currentScore / 10;
        currentTimerResolution = switch (level) {
            case 10 -> 100;
            case 9 -> 130;
            case 8 -> 160;
            case 7 -> 190;
            case 6 -> 220;
            case 5 -> 250;
            case 4 -> 280;
            case 3 -> 310;
            case 2 -> 340;
            default -> INITIAL_TIMER_RESOLUTION;
        };
        timer.setDelay(currentTimerResolution);
    }

    private void initBoard() {
        for (int i = 0; i < BOARD_WIDTH * BOARD_HEIGHT; i++) {
            gameBoard[i] = Tetrominoes.NO_BLOCK;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingDone) {
            isFallingDone = false;
            newTetromino();
        } else {
            advanceOneLine();
        }
    }

    public void start() {
        if (isPaused) {
            return;
        }
        isStarted = true;
        isFallingDone = false;
        currentScore = 0;
        initBoard();
        newTetromino();
        timer.start();
    }

    public void pause() {
        if (!isStarted) {
            return;
        }
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }
        repaint();
    }

    private int blockWidth() {
        return getWidth() / BOARD_WIDTH;
    }

    private int blockHeight() {
        return getHeight() / BOARD_HEIGHT;
    }

    private Tetrominoes curTetrominoPos(int x, int y) {
        return gameBoard[(y * BOARD_WIDTH) + x];
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (!isPaused) {
            currentStatus = "Score: " + currentScore;
            currentLevel = "Level: " + (currentScore / 10 + 1);
        } else {
            currentStatus = "PAUSED";
            currentLevel = "";
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.PLAIN, 28));
        g.drawString(currentStatus, 15, 35);
        g.drawString(currentLevel, 15, 70);

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * blockHeight();

        renderShadow(g, boardTop);
        renderGameBoard(g, boardTop);
        renderCurrentBlock(g, boardTop);
    }

    private void renderShadow(Graphics g, int boardTop) {
        int tempY = curY;
        while (tempY > 0) {
            if (!atomIsMovable(curBlock, curX, tempY - 1, false)) break;
            tempY--;
        }
        for (int i = 0; i < 4; i++) {
            int x = curX + curBlock.getX(i);
            int y = tempY - curBlock.getY(i);
            drawTetromino(g, x * blockWidth(), boardTop + (BOARD_HEIGHT - y - 1) * blockHeight(), curBlock.getShape(), true);
        }
    }

    private void renderGameBoard(Graphics g, int boardTop) {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Tetrominoes shape = curTetrominoPos(j, BOARD_HEIGHT - i - 1);
                if (shape != Tetrominoes.NO_BLOCK) {
                    drawTetromino(g, j * blockWidth(), boardTop + i * blockHeight(), shape, false);
                }
            }
        }
    }

    private void renderCurrentBlock(Graphics g, int boardTop) {
        if (curBlock.getShape() != Tetrominoes.NO_BLOCK) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curBlock.getX(i);
                int y = curY - curBlock.getY(i);
                drawTetromino(g, x * blockWidth(), boardTop + (BOARD_HEIGHT - y - 1) * blockHeight(), curBlock.getShape(), false);
            }
        }
    }

    private void drawTetromino(Graphics g, int x, int y, Tetrominoes shape, boolean isShadow) {
        Color curColor = colorTable[shape.ordinal()];
        g.setColor(isShadow ? curColor.darker().darker() : curColor);
        g.fillRect(x + 1, y + 1, blockWidth() - 2, blockHeight() - 2);
    }

    private void removeFullLines() {
        int fullLines = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            if (isFullLine(i)) {
                fullLines++;
                shiftLinesDown(i);
            }
        }
        if (fullLines > 0) {
            currentScore += fullLines;
            isFallingDone = true;
            curBlock.setShape(Tetrominoes.NO_BLOCK);
            setResolution();
            repaint();
        }
    }

    private boolean isFullLine(int row) {
        for (int j = 0; j < BOARD_WIDTH; j++) {
            if (curTetrominoPos(j, row) == Tetrominoes.NO_BLOCK) {
                return false;
            }
        }
        return true;
    }

    private void shiftLinesDown(int fromRow) {
        for (int k = fromRow; k < BOARD_HEIGHT - 1; k++) {
            for (int l = 0; l < BOARD_WIDTH; l++) {
                gameBoard[(k * BOARD_WIDTH) + l] = curTetrominoPos(l, k + 1);
            }
        }
    }

    private boolean atomIsMovable(Tetromino block, int x, int y, boolean updatePosition) {
        for (int i = 0; i < 4; i++) {
            int newX = x + block.getX(i);
            int newY = y - block.getY(i);
            if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
                return false;
            }
            if (curTetrominoPos(newX, newY) != Tetrominoes.NO_BLOCK) {
                return false;
            }
        }
        if (updatePosition) {
            curBlock = block;
            curX = x;
            curY = y;
            repaint();
        }
        return true;
    }

    private boolean isMovable(Tetromino block, int x, int y) {
        return atomIsMovable(block, x, y, true);
    }

    private void newTetromino() {
        curBlock.setRandomShape();
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curBlock.minY();
        if (!isMovable(curBlock, curX, curY)) {
            curBlock.setShape(Tetrominoes.NO_BLOCK);
            timer.stop();
            isStarted = false;
            gameOver(currentScore);
        }
    }

    private void fixTetromino() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curBlock.getX(i);
            int y = curY - curBlock.getY(i);
            gameBoard[(y * BOARD_WIDTH) + x] = curBlock.getShape();
        }
        removeFullLines();
        if (!isFallingDone) {
            newTetromino();
        }
    }

    private void advanceOneLine() {
        if (!isMovable(curBlock, curX, curY - 1)) {
            fixTetromino();
        }
    }

    private void advanceToEnd() {
        while (isMovable(curBlock, curX, curY - 1)) {
            curY--;
        }
        fixTetromino();
    }

    private void gameOver(int score) {
        int maxScore = readMaxScore();
        String message;
        if (score > maxScore) {
            writeMaxScore(score);
            message = String.format("Congratulations! %nNew high score: %d", score);
        } else {
            message = String.format("Score: %d %nHigh score: %d", score, maxScore);
        }
        UIManager.put("OptionPane.okButtonText", "New Game");
        JOptionPane.showMessageDialog(null, message, "Game Over", JOptionPane.OK_OPTION);
        setResolution();
        start();
    }

    private int readMaxScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("Tetris.score"))) {
            return Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    private void writeMaxScore(int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Tetris.score"))) {
            writer.write(String.valueOf(score));
            writer.newLine();
            writer.write("Tetris high score database.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TetrisKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || curBlock.getShape() == Tetrominoes.NO_BLOCK) {
                return;
            }
            int keycode = e.getKeyCode();
            if (keycode == KeyEvent.VK_P) {
                pause();
                return;
            }
            if (isPaused) {
                return;
            }
            handleKeyPress(keycode);
        }

        private void handleKeyPress(int keycode) {
            switch (keycode) {
                case KeyEvent.VK_A, KeyEvent.VK_LEFT -> isMovable(curBlock, curX - 1, curY);
                case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> isMovable(curBlock, curX + 1, curY);
                case KeyEvent.VK_W, KeyEvent.VK_UP -> isMovable(curBlock.rotateRight(), curX, curY);
                case KeyEvent.VK_S, KeyEvent.VK_DOWN -> advanceOneLine();
                case KeyEvent.VK_SPACE -> advanceToEnd();
            }
        }
    }
}
