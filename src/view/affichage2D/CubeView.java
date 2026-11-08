package view.affichage2D;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.Timer;

import javax.swing.JPanel;
import java.awt.Color;
import model.level.Cube;
import model.tools.Constants;
import model.tools.Direction;
import model.tools.LoadSave;

public class CubeView extends JPanel {
    private Cube cube;
    private Direction direction;
    private LevelView currLevelView;
    private double rotationAngle = 0;
    private Timer rotationTimer;
    private boolean isAnimating = false;
    private BufferedImage cubeImage;

    private final ReentrantLock gameLock = new ReentrantLock();
    private final Object phaseLock = new Object();

    public CubeView(Cube cube) {
        this.cube = cube;
        this.direction = cube.getFacingDirection();
        cubeImage = LoadSave.GetSprite(Constants.Cube.CUBE_NAME);

        setOpaque(false);
        setBackground(new Color(0, 5, 0, 0));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (cubeImage != null) {
            g.drawImage(cubeImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public void updateCube() {
        cubeImage = LoadSave.GetSprite(Constants.Cube.CUBE_NAME); // Recharge l'image et applique la rotation
        revalidate();
        repaint();
    }

    /*
     * @Override
     * public void paintComponent(Graphics g) {
     * super.paintComponent(g);
     * if (cubeImage != null) {
     * g.drawImage(cubeImage, 0, 0, getWidth()/2, getHeight()/2, this);
     * }
     * }
     */
    /*
     * @Override
     * protected void paintComponent(Graphics g) {
     * super.paintComponent(g);
     * if (cubeImage != null) {
     * Graphics2D g2d = (Graphics2D) g;
     * 
     * Dimension tileSize = currLevelView.getTileSize();
     * int fullTileWidth = tileSize.width;
     * int fullTileHeight = tileSize.height;
     * 
     * int tileWidth = fullTileWidth - Constants.Tile.SPACING;
     * int tileHeight = fullTileHeight - Constants.Tile.SPACING;
     * 
     * int borderOffset = 10;
     * 
     * int cubeX = borderOffset + (cube.getX() * fullTileWidth) - (3 * cube.getX())
     * - 2;
     * int cubeY = borderOffset + (cube.getY() * fullTileHeight) - (2 * cube.getY())
     * - 3;
     * 
     * int offsetX = (fullTileWidth - tileWidth) / 2;
     * int offsetY = (fullTileHeight - tileHeight) / 2;
     * 
     * cubeX += offsetX;
     * cubeY += offsetY;
     * 
     * g2d.drawImage(cubeImage, cubeX, cubeY, tileWidth, tileHeight, this);
     * }
     * }
     * 
     * private double getRotationAngle(Direction direction) {
     * return switch (direction) {
     * case NORTH -> 0;
     * case EAST -> 90;
     * case SOUTH -> 180;
     * case WEST -> 270;
     * };
     * }
     */

    public void updateCubePosition() {

    }

    // public void startRotationAnimation(Direction direction) {

    // gameLock.lock();
    // try {
    // if (isAnimating)
    // return;

    // isAnimating = true;
    // rotationAngle = 0;
    // rotationTimer = new Timer(30, new ActionListener() { // 30 FPS
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // rotationAngle += 10; // Vitesse de rotation
    // if (rotationAngle >= 90) {
    // rotationTimer.stop();
    // rotationAngle = 0;
    // isAnimating = false;
    // }
    // repaint();
    // }
    // });
    // rotationTimer.start();
    // } finally {
    // gameLock.unlock();
    // }

    // }

    public void setModelCube(Cube cube) {
        this.cube = cube;
    }

    public boolean isAnimating() {
        return isAnimating;
    }
}