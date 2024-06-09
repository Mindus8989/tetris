package pixelunity.projectoop.tetris;

import java.util.concurrent.ThreadLocalRandom;

public class Tetromino {
    public enum Tetrominoes {
        NO_BLOCK, Z_SHAPE, S_SHAPE, I_SHAPE, T_SHAPE, O_SHAPE, L_SHAPE, J_SHAPE
    }

    private static final int[][][] SHAPE_COORDS = new int[][][]{
        {{0, 0}, {0, 0}, {0, 0}, {0, 0}}, // NO_BLOCK
        {{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}, // Z_SHAPE
        {{0, -1}, {0, 0}, {1, 0}, {1, 1}}, // S_SHAPE
        {{0, -1}, {0, 0}, {0, 1}, {0, 2}}, // I_SHAPE
        {{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, // T_SHAPE
        {{0, 0}, {1, 0}, {0, 1}, {1, 1}}, // O_SHAPE
        {{-1, -1}, {0, -1}, {0, 0}, {0, 1}}, // L_SHAPE
        {{1, -1}, {0, -1}, {0, 0}, {0, 1}} // J_SHAPE
    };

    private Tetrominoes shape;
    private int[][] coords;

    public Tetromino() {
        coords = new int[4][2];
        setShape(Tetrominoes.NO_BLOCK);
    }

    public void setShape(Tetrominoes shape) {
        this.shape = shape;
        for (int i = 0; i < 4; i++) {
            coords[i][0] = SHAPE_COORDS[shape.ordinal()][i][0];
            coords[i][1] = SHAPE_COORDS[shape.ordinal()][i][1];
        }
    }

    public void setRandomShape() {
        int x = ThreadLocalRandom.current().nextInt(1, Tetrominoes.values().length);
        setShape(Tetrominoes.values()[x]);
    }

    public Tetrominoes getShape() {
        return shape;
    }

    public int getX(int index) {
        return coords[index][0];
    }

    public int getY(int index) {
        return coords[index][1];
    }

    public int minX() {
        int minX = coords[0][0];
        for (int i = 1; i < 4; i++) {
            minX = Math.min(minX, coords[i][0]);
        }
        return minX;
    }

    public int minY() {
        int minY = coords[0][1];
        for (int i = 1; i < 4; i++) {
            minY = Math.min(minY, coords[i][1]);
        }
        return minY;
    }

    public Tetromino rotateLeft() {
        if (shape == Tetrominoes.O_SHAPE) {
            return this;
        }

        Tetromino result = new Tetromino();
        result.shape = shape;

        for (int i = 0; i < 4; i++) {
            result.coords[i][0] = coords[i][1];
            result.coords[i][1] = -coords[i][0];
        }

        return result;
    }

    public Tetromino rotateRight() {
        if (shape == Tetrominoes.O_SHAPE) {
            return this;
        }

        Tetromino result = new Tetromino();
        result.shape = shape;

        for (int i = 0; i < 4; i++) {
            result.coords[i][0] = -coords[i][1];
            result.coords[i][1] = coords[i][0];
        }

        return result;
    }
}
