package model.level.plate;

import model.exceptions.WrongDimensionException;
import model.exceptions.mismatch.EndCaseException;
import model.exceptions.mismatch.NoTilesAvailableException;
import model.exceptions.mismatch.StartCaseException;
import model.exceptions.mismatch.TileOnSpecialCaseException;
import model.level.Cube;
import model.level.tiles.Tile;
import model.level.tiles.TileDefinition;
import model.level.tiles.TileType;
import model.tools.Direction;
import model.level.GameState.DisplayMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Plateau de jeu
 * Représente la grille de jeu
 * Chaque case peut contenir une tuile
 */
public class Plate {
    private final Case[][] grid;
    private final int i; // ROWS = I = Y
    private final int j; // COLS = J = X
    private final Map<TileType, Integer> tilesAvailable;
    private final List<TileDefinition> plateSolution;
    private Cube cube;
    private boolean isLevelEditor;

    public Plate(int[][] gridIds,
            Map<TileType, Integer> tilesAvailable,
            List<TileDefinition> plateSolution,
            List<Case> caseList) {
        this.i = gridIds.length;
        this.j = gridIds[0].length;
        this.grid = new Case[i][j];
        this.isLevelEditor = false;

        // positionnement des cases à leurs coordonées
        for (Case c : caseList) {
            grid[c.getI()][c.getJ()] = c;
        }

        if (tilesAvailable == null) {
            throw new IllegalArgumentException(
                    "La liste des tuiles disponibles n'est pas nulle lors de l'initialisation du plateau.");
        }
        Case startCase = getStartCase();
        if (startCase != null) {
            this.cube = new Cube(this, startCase, startCase.getDirection());
        } else {
            throw new IllegalStateException("Aucune case de départ trouvée dans le niveau.");
        }

        // Garder une Map Linked pour l'affichage
        this.tilesAvailable = new LinkedHashMap<>(tilesAvailable);
        this.plateSolution = new ArrayList<>(plateSolution);
    }

    // Constructeur pour l'éditeur de niveau
    public Plate(int i, int j) {
        this.i = i;
        this.j = j;
        this.grid = new Case[i][j];
        this.tilesAvailable = new LinkedHashMap<>();
        this.plateSolution = new ArrayList<>();

        for (int iIndex = 0; iIndex < this.i; iIndex++) {
            for (int jIndex = 0; jIndex < this.j; jIndex++) {
                grid[iIndex][jIndex] = new Case(iIndex, jIndex);
            }
        }
        initLevelEditorTiles();
        this.isLevelEditor = true;

        // // Initialiser le cube avec une case de départ par défaut
        // Case startCase = new Case(CaseType.PATH, 0, 0);
        // startCase.setDirection(Direction.NORTH);
        // grid[0][0] = startCase;
        // this.cube = new Cube(this, startCase);
    }

    public void initLevelEditorTiles() {
        for (TileType type : TileType.values()) {
            if (type == TileType.START || type == TileType.END) {
                tilesAvailable.put(type, 1);
            } else if (type == TileType.TELEPORTATION) {
                tilesAvailable.put(type, 2);
            } else {
                tilesAvailable.put(type, Integer.MAX_VALUE);
            }
        }
    }

    /**
     * Vérifie si i est bien dans l'intervalle [0;i[
     *
     * @param i l'entier à verifier
     */
    public boolean checkI(int i) {
        return i >= 0 && i < this.i;
    }

    /**
     * Vérifie si j est bien dans l'intervalle [0;j[
     * 
     * @param j l'entier à verifier
     */
    public boolean checkJ(int j) {
        return j >= 0 && j < this.j;
    }

    /**
     * Getters
     */

    public Case[][] getGrid() {
        return grid;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public List<TileDefinition> getPlateSolution() {
        return plateSolution;
    }

    public void determinePlateSolution() {
        Map<Tile, TileDefinition> tempTeleportationDefs = new HashMap<>();

        TileDefinition teleporterA = null;
        TileDefinition teleporterB = null;

        for (int i = 0; i < this.i; i++) {
            for (int j = 0; j < this.j; j++) {
                Tile tile = this.grid[i][j].getTile();
                if (tile == null)
                    continue;

                TileType type = tile.getTileType();

                if (type == TileType.START || type == TileType.END ||
                        type == TileType.WHITE || type == TileType.GRAY ||
                        type == TileType.WHITE_BLOCKED || type == TileType.GRAY_BLOCKED ||
                        type == TileType.SWITCH_OFF || type == TileType.TRANSPARENT_VISIBLE ||
                        type == TileType.TRANSPARENT_INVISIBLE) {
                    continue;
                }

                TileDefinition def;

                if (type == TileType.TELEPORTATION) {
                    def = new TileDefinition(type, i, j, null, null);
                    if (teleporterA == null) {
                        teleporterA = def;
                    } else {
                        teleporterB = def;
                    }
                } else if (type == TileType.SPIN) {
                    def = new TileDefinition(type, i, j, null, null);
                } else {
                    def = new TileDefinition(type, i, j, tile.getDirection(), null);
                }

                plateSolution.add(def);
            }
        }

        // Relier les deux téléporteurs
        if (teleporterA != null && teleporterB != null) {
            teleporterA.setLinkedTile(teleporterB);
            teleporterB.setLinkedTile(teleporterA);

        }
    }

    /**
     * Récupère la case du plateau avec les coordoonées
     *
     * @param i un entier dans l'intervalle [0;i[
     * @param j un entier dans l'intervalle [0;j[
     */
    public Case getCase(int i, int j) {
        if (!checkI(i) || !checkJ(j)) {
            throw new WrongDimensionException("Les coordonnées entrées dépassent la limite du plateau.");
        }
        return this.grid[i][j];
    }

    /**
     * Renvoie la case aux coordonnées (i, j) ou null si les coordonnées sont hors
     * limites.
     * 
     * @param i l'indice de ligne
     * @param j l'indice de colonne
     * @return la case à la position (i, j) ou null si les coordonnées sont
     *         invalides
     */
    public Case getCaseOrNull(int i, int j) {
        if (!checkI(i) || !checkJ(j)) {
            return null; // Retourne null au lieu de lever une exception
        }
        return this.grid[i][j];
    }

    /**
     * Place la tuile aux coordonnées indiquées correspondant à une case du chemin
     * 
     * @param tileType  le type de tuile
     * @param i         un entier dans l'intervalle [0;i[
     * @param j         un entier dans l'intervalle [0;j[
     * @param direction l'orientation de la tuile
     */
    public boolean place(TileType tileType, int i, int j, Direction direction, DisplayMode displayMode) {
        if (!checkI(i) || !checkJ(j)) {
            throw new WrongDimensionException("Les coordonnées dépassent la taille du plateau.");
        }
        Case targetCase = getCase(i, j);

        // Cas spéciaux interdits à toute pose
        if (targetCase.isStart() || targetCase.isEnd()) {
            throw new TileOnSpecialCaseException("Impossible de poser une tuile sur START ou END.");
        }

        if (!targetCase.isEmpty()) {
            throw new TileOnSpecialCaseException("Une tuile est déjà présente sur cette case.");
        }

        // Vérifie la disponibilité de la tuile
        if (!contains(tileType) || tilesAvailable.get(tileType) <= 0) {
            throw new NoTilesAvailableException();
        }

        boolean isEditor = this.isLevelEditor;

        // En mode éditeur : autorise la pose sur toute case vide
        // En mode normal : n’autorise que sur des PATH vides
        if (isEditor || targetCase.isType(CaseType.PATH)) {
            targetCase.placeTile(tileType, direction);
            putTile(tileType, tilesAvailable.get(tileType) - 1);
            info(i, j, displayMode);
            return true;
        }

        throw new TileOnSpecialCaseException("Vous ne pouvez pas poser une tuile sur une case non accessible.");
    }

    /**
     * Retire la tuile aux coordonnées indiquées
     *
     * @param i un entier dans l'intervalle [0;i[
     * @param j un entier dans l'intervalle [0;j[
     */
    public void removeTile(int i, int j) {
        if (!checkI(i) || !checkJ(j)) {
            throw new WrongDimensionException("Les coordonnées entrées dépassent la limite du plateau.");
        }
        Case currentCase = getCase(i, j);

        currentCase.check(Case::isEmpty, targetCase -> {
            System.out.println("Il n'y a pas de tuile sur la case " + targetCase.getStrCoords() + ".");
            return false;
        });
        TileType currentTileType = currentCase.getTile().getTileType();

        currentCase.removeTile();
        putTile(currentTileType, tilesAvailable.get(currentTileType) + 1);
    }

    public void clearBoard() {
        for (int i = 0; i < this.i; i++) {
            for (int j = 0; j < this.j; j++) {
                Case currentCase = getCase(i, j);

                if (currentCase != null && !currentCase.isEmpty() && !currentCase.isStart() && !currentCase.isEnd()) {
                    Tile currentTile = currentCase.getTile();
                    if (currentTile != null && !currentTile.isType(TileType.WHITE_BLOCKED) &&
                            !currentTile.isType(TileType.GRAY_BLOCKED) && !currentTile.isType(TileType.SWITCH_OFF) &&
                            !currentTile.isType(TileType.TRANSPARENT_INVISIBLE) &&
                            !currentTile.isType(TileType.TRANSPARENT_VISIBLE)) {
                        try {
                            this.removeTile(i, j);
                        } catch (WrongDimensionException e) {
                            System.err.println(
                                    "Erreur de dimension sur" + currentCase.getStrCoords() + ":" + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    public void resetEditorPlate() {
        // Parcourir toutes les cases
        for (int i = 0; i < this.getI(); i++) {
            for (int j = 0; j < this.getJ(); j++) {
                Case currentCase = this.getCase(i, j);

                // Supprimer toutes les tuiles, y compris START et END
                if (!currentCase.isEmpty()) {
                    currentCase.removeTile();
                }

                currentCase.setCaseType(CaseType.BLOCKED);
            }
        }
        tilesAvailable.clear();
        initLevelEditorTiles();
    }

    public Case getStartCase() throws WrongDimensionException, StartCaseException {
        Case startCase = null;

        for (int i = 0; i < this.i; i++) {
            for (int j = 0; j < this.j; j++) {
                Case currentCase = getCase(i, j);
                if (currentCase == null)
                    continue;
                if (currentCase.getTile() != null && currentCase.getTile().isType(TileType.START)) {
                    startCase = currentCase;
                    if (startCase.getTile().getDirection() == null) {
                        startCase.getTile().setDirection(Direction.NORTH);
                    }
                    // S'assurer que la case a une direction valide
                    if (startCase.getDirection() == null) {
                        startCase.setDirection(startCase.getTile().getDirection());
                    }
                }
            }
        }

        if (startCase == null) {
            throw new StartCaseException();
        }
        return startCase;
    }

    public Case getEndCase() throws WrongDimensionException, EndCaseException {
        Case endCase = null;
        for (int i = 0; i < this.i; i++) {
            for (int j = 0; j < this.j; j++) {
                Case currentCase = getCase(i, j);
                if (currentCase != null && currentCase.getTile() != null
                        && currentCase.getTile().isType(TileType.END)) {
                    endCase = currentCase;
                }
            }
        }
        if (endCase == null)
            throw new EndCaseException();
        return endCase;
    }

    /**
     * Obtient les informations d'une case à partir de ses coordonnées
     *
     * @param i un entier dans l'intervalle [0;i[
     * @param j un entier dans l'intervalle [0;j[
     */
    public void info(int i, int j, DisplayMode displayMode) {
        if (!checkI(i) || !checkJ(j)) {
            throw new WrongDimensionException("Les coordonnées entrées dépassent la limite du plateau.");
        }
        getCase(i, j).info(displayMode);
    }

    /**
     * Obtient les informations de toutes les cases du plateau
     */
    public void infos(DisplayMode displayMode) {
        if (displayMode == DisplayMode.TERMINAL) {
            for (int i = 0; i < this.i; i++) {
                for (int j = 0; j < this.j; j++) {
                    info(i, j, displayMode);
                }
            }
        }
    }

    /**
     * Affiche le nombre de tuiles disponible
     */
    public void showTilesAvailable(DisplayMode displayMode) {
        if (displayMode == DisplayMode.TERMINAL) {
            for (Map.Entry<TileType, Integer> entry : this.tilesAvailable.entrySet()) {
                TileType tileType = entry.getKey();
                int value = entry.getValue();

                System.out.println(tileType.getCompleteName() + " : " + value);
            }
        }
    }

    /**
     * Affiche le plateau
     */
    public void printMap(DisplayMode displayMode) {
        if (displayMode == DisplayMode.TERMINAL) {
            for (int i = 0; i < this.i; i++) {
                for (int j = 0; j < this.j; j++) {
                    if (cube != null && cube.isOnCase(getCase(i, j))) {
                        System.out.print("□ ");
                    } else {
                        System.out.print(getCase(i, j) + " ");
                    }
                }
                System.out.println();
            }
        }
    }

    /**
     * Affiche l'état du jeu
     */
    public void print(DisplayMode displayMode) {
        if (displayMode == DisplayMode.TERMINAL) {
            printMap(displayMode);
            showTilesAvailable(displayMode);
        }
    }

    /**
     * Ajoute une quantité précise d'une unique tuile
     *
     * @param tileType un type de tuile
     * @param value    le nombre de tuile
     */
    public void putTile(TileType tileType, int value) {
        this.tilesAvailable.put(tileType, value);
    }

    public boolean contains(TileType tileType) {
        return tilesAvailable.containsKey(tileType);
    }

    public boolean stillHas(TileType tileType) {
        if (contains(tileType)) {
            int value = tilesAvailable.get(tileType);
            return value > 0;
        }
        return false;
    }

    public Map<TileType, Integer> getTilesAvailable() {
        return tilesAvailable;
    }

    public Cube getCube() {
        return cube;
    }

    public void unlockCase(int i, int j) {
        if (isLevelEditor) {
            getCase(i, j).setCaseType(CaseType.PATH);
        }
    }

    public void lockCase(int i, int j) {
        if (isLevelEditor) {
            getCase(i, j).setCaseType(CaseType.BLOCKED);
        }
    }

    public boolean isLevelEditor() {
        return isLevelEditor;
    }

    public void setLevelEditor(boolean b) {

        isLevelEditor = b;
    }
}