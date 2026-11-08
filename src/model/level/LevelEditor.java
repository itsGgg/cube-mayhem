package model.level;

import java.io.IOException;

import model.level.plate.Plate;
import model.level.tiles.TileDefinition;
import model.level.tiles.TileType;
import model.level.GameState.DisplayMode;;

public class LevelEditor {
    private Level level;
    private Plate plate;

    public LevelEditor(int i, int j) {
        this.plate = new Plate(i, j);
        this.level = new Level(plate);
        plate.printMap(DisplayMode.TERMINAL);
    }

    public LevelEditor(String filePath) throws IOException {
        this.plate = LevelFileManager.loadLevelFromFile(filePath);
        this.plate.setLevelEditor(false);
        this.level = new Level(plate);
        plate.printMap(DisplayMode.TERMINAL);
    }

    public Level getLevel() {
        return level;
    }
}
        