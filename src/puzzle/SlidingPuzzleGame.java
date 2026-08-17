package puzzle;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * A sliding tile puzzle game. Supports square boards from 3x3 to 8x8,
 * tiles labeled with numbers or cut from a chosen picture, move counting,
 * sound effects, and looping background music.
 */
public class SlidingPuzzleGame extends MouseAdapter {

    private static final String ICON_PATH = "src/media/sliding-puzzle.png";
    private static final String[] PICTURE_PATHS = {
            "src/media/seashell.jpg",
            "src/media/lastdinner.jpg",
            "src/media/starrynight.jpg",
            "src/media/red.jpg",
            "src/media/woman.jpg",
            "src/media/tuna.jpg"
    };

    private final JFrame frame = new JFrame("SlidingPuzzle");
    private final SoundPlayer soundPlayer = new SoundPlayer();
    private final JButton emptyButton = new JButton();

    private JButton[][] board;
    private int sizeRow, sizeCol;
    private int emptyRow, emptyCol;
    private int row, col;
    private int movementCounter = 0;
    private boolean isShuffled = false;
    private boolean isPicture;

    public SlidingPuzzleGame() {
        frame.setSize(700, 788);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(buildMenuBar());
        setPuzzle();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);

        JMenuItem shuffleItem = new JMenuItem("Shuffle");
        shuffleItem.addActionListener(e -> {
            shuffle();
            frame.revalidate();
            frame.repaint();
        });
        menu.add(shuffleItem);

        JMenuItem setSizeItem = new JMenuItem("Set Puzzle Size");
        setSizeItem.addActionListener(e -> {
            frame.setVisible(false);
            frame.getContentPane().removeAll();
            setPuzzle();
            isShuffled = false;
            movementCounter = 0;
        });
        menu.add(setSizeItem);

        JMenuItem choosePictureItem = new JMenuItem("Choose Picture");
        choosePictureItem.addActionListener(e -> {
            frame.setVisible(false);
            frame.getContentPane().removeAll();
            choosePic();
            isShuffled = false;
            movementCounter = 0;
        });
        menu.add(choosePictureItem);

        JMenuItem chooseTypeItem = new JMenuItem("Choose Type");
        chooseTypeItem.addActionListener(e -> {
            frame.setVisible(false);
            frame.getContentPane().removeAll();
            setType();
            isShuffled = false;
            movementCounter = 0;
        });
        menu.add(chooseTypeItem);

        JMenu musicMenu = new JMenu("Music");
        JMenuItem musicOn = new JMenuItem("On");
        JMenuItem musicOff = new JMenuItem("Off");
        musicOn.addActionListener(e -> soundPlayer.playBackgroundMusic());
        musicOff.addActionListener(e -> soundPlayer.stop());
        musicMenu.add(musicOn);
        musicMenu.add(musicOff);
        menu.add(musicMenu);

        JMenuItem referencesItem = new JMenuItem("References:");
        referencesItem.addActionListener(e -> showReferences());
        menu.add(referencesItem);

        return menuBar;
    }

    private void showReferences() {
        JFrame ref = new JFrame("References");
        ref.setLayout(null);
        ref.setResizable(false);
        ref.getContentPane().setBackground(Color.white);
        ref.setSize(400, 300);
        ref.setLocationRelativeTo(null);

        String[] credits = {"Tuna Bekiş", "Emre Erdem", "Metehan Kartop", "İsmail Ambarkütük", "Chat GPT version 3.5"};
        for (int i = 0; i < credits.length; i++) {
            JLabel label = new JLabel(credits[i]);
            label.setBounds(20, 20 + i * 40, 150, 40);
            ref.add(label);
        }

        ref.setVisible(true);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        findIndex(e);

        boolean adjacentToEmpty =
                (emptyCol == col && Math.abs(emptyRow - row) == 1) ||
                (emptyRow == row && Math.abs(emptyCol - col) == 1);

        if (adjacentToEmpty) {
            soundPlayer.playValidMove();
            swapButton();
        } else {
            soundPlayer.playWrongMove();
        }

        if (isShuffled) {
            check();
        }
    }

    private void findIndex(MouseEvent e) {
        JButton clickedButton = (JButton) e.getSource();
        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                if (board[i][j] == clickedButton) {
                    row = i;
                    col = j;
                }
            }
        }
    }

    private void swapButton() {
        movementCounter++;

        JButton clickedTile = board[row][col];
        JButton[][] updatedBoard = new JButton[sizeRow][sizeCol];

        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                if (board[i][j] != emptyButton) {
                    updatedBoard[i][j] = board[i][j];
                }
            }
        }

        updatedBoard[row][col] = emptyButton;
        updatedBoard[emptyRow][emptyCol] = clickedTile;
        board = updatedBoard;

        frame.getContentPane().removeAll();
        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                frame.getContentPane().add(board[i][j]);
            }
        }

        emptyRow = row;
        emptyCol = col;

        frame.revalidate();
        frame.repaint();
    }

    /**
     * Shuffles the board by simulating a long sequence of random legal moves
     * on a plain index grid, which guarantees the resulting layout is solvable.
     */
    private void shuffle() {
        movementCounter = 0;
        Random random = new Random();
        int totalTiles = sizeRow * sizeCol;

        int[][] template = new int[sizeRow][sizeCol];
        int tileNumber = 1;
        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                template[i][j] = tileNumber++;
            }
        }

        int emptySimRow = sizeRow - 1;
        int emptySimCol = sizeCol - 1;
        int movesLeft = 10001;
        while (movesLeft > 0) {
            int direction = random.nextInt(4) + 1;
            int swapped;
            if (direction == 1 && emptySimCol > 0) {
                swapped = template[emptySimRow][emptySimCol - 1];
                template[emptySimRow][emptySimCol - 1] = totalTiles;
                template[emptySimRow][emptySimCol] = swapped;
                emptySimCol--;
                movesLeft--;
            } else if (direction == 2 && emptySimRow > 0) {
                swapped = template[emptySimRow - 1][emptySimCol];
                template[emptySimRow - 1][emptySimCol] = totalTiles;
                template[emptySimRow][emptySimCol] = swapped;
                emptySimRow--;
                movesLeft--;
            } else if (direction == 3 && emptySimCol < sizeCol - 1) {
                swapped = template[emptySimRow][emptySimCol + 1];
                template[emptySimRow][emptySimCol + 1] = totalTiles;
                template[emptySimRow][emptySimCol] = swapped;
                emptySimCol++;
                movesLeft--;
            } else if (direction == 4 && emptySimRow < sizeRow - 1) {
                swapped = template[emptySimRow + 1][emptySimCol];
                template[emptySimRow + 1][emptySimCol] = totalTiles;
                template[emptySimRow][emptySimCol] = swapped;
                emptySimRow++;
                movesLeft--;
            }
        }

        // Map the shuffled index layout back onto the existing tile buttons
        JButton[][] shuffledBoard = new JButton[sizeRow][sizeCol];
        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                for (int k = 0; k < sizeRow; k++) {
                    for (int l = 0; l < sizeCol; l++) {
                        if (template[i][j] == Integer.parseInt(board[k][l].getText())) {
                            shuffledBoard[i][j] = board[k][l];
                        }
                    }
                }
            }
        }
        board = shuffledBoard;

        frame.getContentPane().removeAll();
        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                frame.getContentPane().add(board[i][j]);
                if (board[i][j].getText().equals(Integer.toString(totalTiles))) {
                    emptyRow = i;
                    emptyCol = j;
                }
            }
        }

        isShuffled = true;
    }

    private void check() {
        if (isSolved()) {
            showCongratulationsDialog();
        }
    }

    private boolean isSolved() {
        int expected = 1;
        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                if (expected++ != Integer.parseInt(board[i][j].getText())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showCongratulationsDialog() {
        JFrame congrats = new JFrame();
        congrats.setIconImage(new ImageIcon(ICON_PATH).getImage());
        congrats.setLayout(null);
        congrats.setSize(400, 200);
        congrats.getContentPane().setBackground(new Color(140, 10, 110));
        congrats.setLocationRelativeTo(null);

        JLabel label = new JLabel("Congratulations! Movements:" + movementCounter);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setBounds(50, 30, 400, 50);
        congrats.add(label);

        JButton newGameButton = new JButton("New Game?");
        newGameButton.setBounds(140, 80, 120, 60);
        newGameButton.setFont(new Font("Arial", Font.BOLD, 10));
        newGameButton.setBackground(new Color(250, 250, 250));
        newGameButton.addActionListener(e -> {
            soundPlayer.playValidMove();
            movementCounter = 0;
            isShuffled = false;
            frame.getContentPane().removeAll();
            setType();
            frame.setVisible(false);
            congrats.dispose();
        });
        congrats.add(newGameButton);

        frame.setVisible(false);
        isShuffled = false;
        congrats.setVisible(true);
    }

    private void createPuzzle() {
        frame.setIconImage(new ImageIcon(ICON_PATH).getImage());
        board = new JButton[sizeRow][sizeCol];
        frame.getContentPane().setLayout(new GridLayout(sizeRow, sizeCol));

        int totalTiles = sizeRow * sizeCol;
        int tileNumber = 1;
        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                if (tileNumber < totalTiles) {
                    JButton tile = new JButton(String.valueOf(tileNumber));
                    tile.setBackground(new Color(110, 10, 140));
                    tile.setForeground(Color.WHITE);
                    tile.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    tile.setFocusPainted(false);
                    tile.setFont(tile.getFont().deriveFont(isPicture ? 0f : 20.0f));
                    tile.addMouseListener(this);
                    board[i][j] = tile;
                } else {
                    emptyButton.setVisible(false);
                    emptyButton.setText(Integer.toString(totalTiles));
                    board[i][j] = emptyButton;
                }
                tileNumber++;
            }
        }

        for (int i = 0; i < sizeRow; i++) {
            for (int j = 0; j < sizeCol; j++) {
                frame.getContentPane().add(board[i][j]);
            }
        }

        emptyRow = sizeRow - 1;
        emptyCol = sizeCol - 1;
        frame.setVisible(true);
    }

    private void setPuzzle() {
        JFrame sizeChooser = new JFrame("Set Size and Picture");
        sizeChooser.setIconImage(new ImageIcon(ICON_PATH).getImage());
        sizeChooser.getContentPane().setBackground(new Color(150, 150, 250));
        sizeChooser.setSize(500, 340);
        sizeChooser.setLayout(null);
        sizeChooser.setResizable(false);
        sizeChooser.setLocationRelativeTo(null);

        JLabel label = new JLabel("Choose Puzzle's Size");
        label.setForeground(new Color(150, 30, 250));
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBounds(160, 40, 200, 20);
        sizeChooser.add(label);

        int[] sizes = {3, 4, 5, 6, 7, 8};
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            JButton sizeButton = new JButton(size + "x" + size);
            styleOptionButton(sizeButton);
            sizeButton.setBounds(40 + (i % 3) * 140, 80 + (i / 3) * 110, 120, 90);
            sizeButton.addActionListener(e -> {
                soundPlayer.playValidMove();
                sizeRow = size;
                sizeCol = size;
                setType();
                sizeChooser.dispose();
            });
            sizeChooser.add(sizeButton);
        }

        sizeChooser.setVisible(true);
    }

    private void setType() {
        JFrame typeChooser = new JFrame("Choose Type");
        typeChooser.setIconImage(new ImageIcon(ICON_PATH).getImage());
        typeChooser.getContentPane().setBackground(new Color(229, 204, 255));
        typeChooser.setSize(500, 340);
        typeChooser.setLayout(null);
        typeChooser.setResizable(false);
        typeChooser.setLocationRelativeTo(null);

        JLabel label = new JLabel("Choose Type:");
        label.setForeground(new Color(150, 30, 250));
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBounds(160, 40, 200, 20);
        typeChooser.add(label);

        JButton numberButton = new JButton("Number");
        styleOptionButton(numberButton);
        numberButton.setBounds(30, 100, 205, 100);
        numberButton.addActionListener(e -> {
            soundPlayer.playValidMove();
            isPicture = false;
            createPuzzle();
            typeChooser.dispose();
        });
        typeChooser.add(numberButton);

        JButton pictureButton = new JButton("Picture");
        styleOptionButton(pictureButton);
        pictureButton.setBounds(265, 100, 205, 100);
        pictureButton.addActionListener(e -> {
            soundPlayer.playValidMove();
            isPicture = true;
            choosePic();
            typeChooser.dispose();
        });
        typeChooser.add(pictureButton);

        typeChooser.setVisible(true);
    }

    private void choosePic() {
        JFrame pictureChooser = new JFrame("Choose Picture");
        pictureChooser.setIconImage(new ImageIcon(ICON_PATH).getImage());
        pictureChooser.getContentPane().setBackground(new Color(229, 204, 255));
        pictureChooser.setSize(500, 340);
        pictureChooser.setLayout(null);
        pictureChooser.setResizable(false);
        pictureChooser.setLocationRelativeTo(null);

        JLabel label = new JLabel("Choose A Picture Below:");
        label.setForeground(new Color(150, 30, 250));
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBounds(160, 40, 200, 20);
        pictureChooser.add(label);

        for (int i = 0; i < PICTURE_PATHS.length; i++) {
            ImageIcon thumbnail = new ImageIcon(PICTURE_PATHS[i]);
            JButton pictureButton = new JButton(thumbnail);
            styleOptionButton(pictureButton);
            pictureButton.setBounds(40 + (i % 3) * 140, 80 + (i / 3) * 110, 120, 90);
            pictureButton.addActionListener(e -> {
                soundPlayer.playValidMove();
                isPicture = true;
                createPuzzle();
                addPicture(thumbnail);
                pictureChooser.dispose();
            });
            pictureChooser.add(pictureButton);
        }

        pictureChooser.setVisible(true);
    }

    private void styleOptionButton(JButton button) {
        button.setBackground(new Color(225, 160, 250));
        button.setForeground(new Color(150, 30, 250));
        button.setFont(new Font("Arial", Font.BOLD, 16));
    }

    /**
     * Slices the given image into sizeCol x sizeCol pieces and assigns
     * one piece as the icon of each tile button on the board.
     */
    private void addPicture(ImageIcon sourceImage) {
        Image image = sourceImage.getImage();
        int size = sizeCol;
        int pieceWidth = sourceImage.getIconWidth() / size;
        int pieceHeight = sourceImage.getIconHeight() / size;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                BufferedImage pieceImage = new BufferedImage(pieceWidth, pieceHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = pieceImage.createGraphics();
                g2d.drawImage(image, 0, 0, pieceWidth, pieceHeight,
                        pieceWidth * j, pieceHeight * i, pieceWidth * (j + 1), pieceHeight * (i + 1), null);
                g2d.dispose();
                board[i][j].setIcon(new ImageIcon(pieceImage));
            }
        }
    }
}
