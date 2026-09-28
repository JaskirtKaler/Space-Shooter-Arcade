import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Gameboard {
    private static int height = 600;
    private static int width = 900;
    private static int y_axis = 0;
    private static int x_axis = 0;

    public void init() {
        JFrame frame = new JFrame("Space Shooter Arcade");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center window on screen
        frame.setResizable(false);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Gameboard board = new Gameboard();
            board.init();
        });
    }
}
