package controller;

import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.Map;

import model.exceptions.mismatch.TileOnSpecialCaseException;
import model.level.GameState;
import model.level.GameState.DisplayMode;

import model.level.Level;
import model.level.GameState.GameMode;
import model.level.tiles.Tile;
import model.level.tiles.TileBank;
import model.level.tiles.TileType;
import model.tools.Direction;

/**
 * Contrôleur de niveau
 * Permet de gérer les niveaux du jeu
 * et de les initialiser
 */
public class LevelController {
    public final int maxLevels = 22;

    private int currentLevelNumber = 1;
    private int maxLevelUnlocked = 1;
    private Level currentLevel;
    private GameState gameState;
    private DisplayMode displayMode;

    public LevelController(GameState gameState) {
        this.gameState = gameState;
        this.displayMode = gameState.getDisplayMode();
        if (currentLevelNumber != 0) {
            this.currentLevel = loadLevel();
        }
    }

    public Level loadLevel() {
        try {
            if (currentLevelNumber > maxLevelUnlocked) {
                System.out.println("Vous n'avez pas encore débloqué le niveau : " + currentLevelNumber
                        + ".Veuillez d'abord compléter le niveau " + maxLevelUnlocked + ".");
            }
            return new Level(currentLevelNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appeler cette méthode pour initialiser le niveau
     */
    public void initializeLevel(Level level) {
        // level.setLevelState(LevelState.DISPOSING);
        this.gameState.setCurrentState(GameMode.DISPOSING);
        System.out.println("Le niveau " + level.getLevelNumber() + " est prêt !");
    }

    public Map<TileType, Integer> placeTheSolution() {
        return currentLevel.placeTheSolution();
    }

    /**
     * Méthode permettant de rédemarrer le niveau actuel en enlevant l'ensemble des
     * tuiles du plateau et positionnant le cube a sa position originelle
     */
    public void restartLevel() {
        System.out.println("Redémarrage du niveau...");
        currentLevel = loadLevel(); // Recharge le niveau
        initializeLevel(currentLevel);
    }

    public void loadNextLevel() {
        int nextLevelNumber = currentLevelNumber + 1;

        // Vérifier si le niveau suivant est débloqué
        if (nextLevelNumber > maxLevelUnlocked) {
            throw new RuntimeException("Le niveau " + nextLevelNumber + " n'est pas encore débloqué.");
        }

        currentLevelNumber = nextLevelNumber;
        Level nextLevel = loadLevel();

        if (nextLevel != null) {
            currentLevel = nextLevel;
            initializeLevel(currentLevel);
        } else {
            System.out.println("Vous avez atteint le dernier niveau disponible.");
        }
    }

    /* Méthodes de gestion pour aider l'initialisation des niveaux */
    /*------------------------------------------------------------- */

    public int checkCoordinate(Scanner scanner, boolean isCheckingI) {
        int value = 0;
        while (true) {
            try {
                System.out.print("Donnez sa coordonnée " + (isCheckingI ? "i" : "j") + " : ");
                String nextline = scanner.nextLine();
                value = Integer.parseInt(nextline);

                if (isCheckingI ? currentLevel.getPlate().checkI(value) : currentLevel.getPlate().checkJ(value)) {
                    return value;
                } else {
                    System.out.println(
                            "Veuillez entrer une coordonnée valide dans les limites du plateau. La coordonée doit être comprise entre 0 et "
                                    + (isCheckingI ? currentLevel.getPlate().getI() - 1
                                            : currentLevel.getPlate().getJ() - 1));
                }
            } catch (NumberFormatException e) {
                System.out.println("Veuillez entrer un entier valide.");
            }
        }
    }

    public int checkI(Scanner scanner) {
        return checkCoordinate(scanner, true);
    }

    public int checkJ(Scanner scanner) {
        return checkCoordinate(scanner, false);
    }

    public Direction checkIfDirectionTile(Scanner sc, Tile tile) {
        if (tile.isDirectional()) {
            while (true) {
                System.out.println(
                        "De combien de quart de tours voulez-vous tourner dans le sens horaire? (pointe NORTH par défaut) ");
                String nextline = sc.nextLine();
                try {
                    int k = Integer.parseInt(nextline);
                    return tile.rotate(k);
                } catch (NumberFormatException e) {
                    System.out.println("Votré entrée n'est pas un nombre");
                }
            }
        }
        return null;
    }

    public void placeTile(Scanner placeScanner) {
        System.out.println("[=] Fleche");
        System.out.println("[~] Slide");
        System.out.println("[@] Spin");
        System.out.println("[+] Teleportation");
        System.out.println("Quelle tuile voulez-vous poser?");
        String tileSymbolOrName = placeScanner.nextLine();
        if (tileSymbolOrName.isEmpty())
            return;

        TileType tileType;

        if (TileType.fromSymbol(tileSymbolOrName) == null) {
            tileType = TileType.fromName(tileSymbolOrName);

            if (tileType == null) {
                System.out.println("La tuile n'existe pas...");
                return;
            }

        } else {
            tileType = TileType.fromSymbol(tileSymbolOrName);
        }

        if (!currentLevel.getPlate().stillHas(tileType)) {
            System.out.println("Il n'y a plus de tuiles de ce type encore disponible...");
            return;
        }
        int i = checkI(placeScanner);

        int j = checkJ(placeScanner);

        Direction direction = checkIfDirectionTile(placeScanner, TileBank.createTile(tileType));

        try {
            if (currentLevel.getPlate().place(tileType, i, j, direction, displayMode)) {
                System.out.println("La tuile " + tileType.getCompleteName() + " est placée avec succès !");
                System.out.println("Direction? " + i + ", " + j + ", " + direction);
            }
            currentLevel.getPlate().print(displayMode);

        } catch (TileOnSpecialCaseException e) {
            System.out.println("La tuile ne peut pas se placer sur une case condamnée.");
        }
    }

    public void removeTile(Scanner removeScanner) {
        int i = checkI(removeScanner);

        int j = checkJ(removeScanner);

        if (currentLevel.getPlate().getCase(i, j).isEmpty()) {
            System.out.println("Il n'y a aucune tuile sur cette case !");
            return;
        }

        TileType tileToRemove = currentLevel.getPlate().getCase(i, j).getTile().getTileType();
        if (tileToRemove != TileType.START && tileToRemove != TileType.END && tileToRemove != TileType.WHITE_BLOCKED
                && tileToRemove != TileType.GRAY_BLOCKED) {
            currentLevel.getPlate().removeTile(i, j);
            System.out.println(
                    "La tuile " + tileToRemove.getCompleteName() + " de la case (" + i + ", " + j + ") a été retirée.");
        } else {
            System.out.println("Vous ne pouvez pas rétirer une tuile START/END");
        }
    }

    public void info(Scanner infoScanner) {
        int i = checkI(infoScanner);
        int j = checkJ(infoScanner);

        // System.out.println("Mode d'affichage actuel : " + displayMode);

        currentLevel.getPlate().info(i, j, displayMode);
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    /* Getters et setters */
    public boolean isLevelComplete(Level level) {
        return level.isDone();
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
    }

    public void setCurrentLevelNumber(int currentLevelNumber) {
        this.currentLevelNumber = currentLevelNumber;
    }

    public int getMaxLevelUnlocked() {
        return maxLevelUnlocked;
    }

    public void setMaxLevelUnlocked(int maxLevelUnlocked) {
        this.maxLevelUnlocked = maxLevelUnlocked;
    }

    public void increaseMaxLevelUnlocked() {
        this.maxLevelUnlocked += 1;
    }

    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }

    public void increaseCurrentLevel() {
        if (currentLevelNumber >= maxLevelUnlocked) {
            throw new IllegalArgumentException(
                    "Le niveau actuel ne peut pas être plus grand que le niveau max débloqué.");
        }
        currentLevelNumber += 1;
    }

    public boolean isLevelUnlocked(int level) {
        return level <= maxLevelUnlocked;
    }

    public int getMaxLevels() {
        return maxLevels;
    }
}
