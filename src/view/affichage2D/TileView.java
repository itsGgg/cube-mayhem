package view.affichage2D;

import model.level.tiles.Tile;
import model.level.tiles.TileBank;

import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class TileView extends JPanel {
    private BufferedImage originalImage;
    private Tile tile;

    public TileView(Tile tile) {
        super();
        this.tile = TileBank.checkDirection(tile);
        setSprites();
        setVisible(true);
    }

    private void updateSprite(int k) {
        originalImage = TileBank.getTileImage(tile.getTileType(), tile.getDirection().spin(k));
    }

    public void setSprites() {
        updateSprite(0);
    }

    public void spinSprite() {
        updateSprite(1);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (originalImage != null) {
            g.drawImage(originalImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public BufferedImage getImage() {
        return originalImage;
    }

    public Tile getTile() {
        return tile;
    }
}
