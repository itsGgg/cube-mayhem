package model.level.tiles;

import model.tools.Constants;
import model.tools.Direction;
import model.tools.LoadSave;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class TileBank {
    private static Map<TileType, BufferedImage> tiles = new HashMap<>();

    public static void loadTiles(String imageName) {
        BufferedImage tileSprite = LoadSave.GetSprite(imageName);

        for (int i = 0; i < Constants.Tile.COUNT; i++) {
            BufferedImage tileImage = tileSprite.getSubimage(i * Constants.Tile.SIZE, 0, Constants.Tile.SIZE,
                    Constants.Tile.SIZE);
            tiles.put(TileType.values()[i], tileImage);
        }
    }

    public static BufferedImage getTileImage(TileType type) {
        return tiles.get(type);
    }

    public static BufferedImage getTileImage(TileType type, Direction direction) {
        if(direction == null) return getTileImage(type);

        BufferedImage tileImage = getTileImage(type);

        int width = tileImage.getWidth(); // 100
        int height = tileImage.getHeight();
        AffineTransform transform = new AffineTransform();
        transform.translate(width / 2, height / 2);
        transform.rotate(direction.toRadians());
        transform.translate(-width / 2, -height / 2);

        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(tileImage, null);
    }

    public static Tile createTile(TileType type, Direction direction) {
        return new Tile(type, direction);
    }

    public static Tile createTile(TileType type) {
        return new Tile(type);
    }

    public static Tile checkDirection(Tile copyTile) {
        return new Tile(copyTile.getTileType(), copyTile.getDirection() == null ? Direction.NORTH : copyTile.getDirection());
    }
}