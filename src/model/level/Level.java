package model.level;

import java.io.FileNotFoundException;
import java.util.Map;

import model.level.GameState.DisplayMode;
import model.level.plate.Plate;
import model.level.tiles.TileDefinition;
import model.level.tiles.TileType;
/*
 * Classe représentant un niveau du jeu
 */

public class Level {
    protected Plate plate;
    protected boolean isDone;
    protected int levelNumber;
    protected boolean isEditorLevel;
    protected GameState gameState;

    public Level(int levelNumber) throws FileNotFoundException {
        this.levelNumber = levelNumber;
        this.isDone = false;
        this.isEditorLevel = false;
        this.plate = LevelFileManager.loadLevel(levelNumber);
    }

    // Constructeur pour l'éditeur de niveau
    public Level(int rows, int cols) {
        this.levelNumber = -1;
        this.isDone = false;
        this.plate = new Plate(rows, cols);
        this.isEditorLevel = true;
    }

    public Level(Plate plate) {
        this.levelNumber = -1;
        this.plate = plate;
        this.isDone = false;
        this.isEditorLevel = false;
    }

    /**
     * Méthode permettant de poser la solution sur le plateau
     * 
     * @return La Map des tuiles disponibles après le placement de la solution
     */
    public Map<TileType, Integer> placeTheSolution() {
        plate.clearBoard();
        for (TileDefinition tileDef : plate.getPlateSolution()) {
            int i = tileDef.getI();
            int j = tileDef.getJ();

            TileType type = tileDef.getType();
            plate.place(type, i, j, tileDef.getDirection(), DisplayMode.TERMINAL);
        }
        return plate.getTilesAvailable();
    }

    // Getters et Setters
    public Plate getPlate() {
        return plate;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public boolean isEditorLevel() {
        return isEditorLevel;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
}