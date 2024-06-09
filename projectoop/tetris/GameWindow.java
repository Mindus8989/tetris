package pixelunity.projectoop.tetris;

import java.awt.GridLayout;
import javax.swing.JFrame;

public class GameWindow extends JFrame {

    public GameWindow() {
        setTitle("Tetris ");
        setSize(300, 714);
        setResizable(false);

        setLayout(new GridLayout(1, 1));

        GameBoardPanel gameBoard = new GameBoardPanel(this, 400);
        add(gameBoard);
        gameBoard.start();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
