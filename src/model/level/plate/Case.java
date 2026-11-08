package model.level.plate;

import model.level.GameState.DisplayMode;
import model.level.tiles.Tile;
import model.level.tiles.TileBank;
import model.level.tiles.TileType;
import model.tools.CaseExecution;
import model.tools.Direction;

import java.util.function.Predicate;

public class Case {
    private CaseType caseType;
    private Direction direction;
    private Tile tile;
    private final int i;
    private final int j;
    private boolean transparentVisible;

    public Case(CaseType caseType, int i, int j) {
        this.caseType = caseType;
        this.i = i;
        this.j = j;
    }

    public Case(int i, int j) {
        this(CaseType.BLOCKED, i, j);
    }

    public Case(int i, int j, Tile tile) {
        this(CaseType.BLOCKED, i, j);
        this.tile = tile;
        this.direction = tile.getDirection();
    }

    /**
     * Constructeur permettant de créer une copie d'une case
     */
    public Case(Case original) {
        this.caseType = original.getCaseType();
        this.direction = original.getDirection();
        this.tile = (original.getTile() != null) ? new Tile(original.getTile()) : null; // Gestion du null
        this.i = original.getI();
        this.j = original.getJ();
        this.transparentVisible = original.isVisible();
    }

    public Case(CaseType type) {
        this(type, 0, 0);
    }

    public CaseType getCaseType() {
        return caseType;
    }

    public void setCaseType(CaseType type) {
        this.caseType = type;
    }

    public boolean isType(CaseType type) {
        return this.caseType == type;
    }

    public boolean isBlocked() {
        return isType(CaseType.BLOCKED);
    }

    public boolean isPath() {
        return isType(CaseType.PATH);
    }

    public boolean isTransparent() {
        return isType(CaseType.TRANSPARENT);
    }

    public boolean isRestrict() {
        return isType(CaseType.RESTRICT);
    }

    public boolean isVisible() {
        if (!isTransparent()) {
            return true;
        }
        return transparentVisible;
    }

    public boolean isInvisible() {
        return !isVisible();
    }

    public void setVisible(boolean visible) {
        this.transparentVisible = visible;
    }

    public boolean isStart() {
        return this.tile != null && this.tile.isType(TileType.START);
    }

    public boolean isEnd() {
        return this.tile != null && this.tile.isType(TileType.END);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    /**
     * Vérifier une condition sur la case et executer une action dessus.
     *
     * @param condition La condition à tester.
     * @param exec      L'action à exécuter.
     */
    public void check(Predicate<Case> condition, CaseExecution exec) {
        if (condition.test(this)) {
            exec.execute(this);
        }
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    /**
     * Place la tuile et sa direction sur la case courante
     *
     * @param tileType  une tuile quelconque
     * @param direction la direction de la tuile
     */
    public void placeTile(TileType tileType, Direction direction) {
        if (isPath() && isEmpty()) {
            this.tile = TileBank.createTile(tileType, direction);
        }
    }

    public void removeTile() {
        this.tile = null;
    }

    public boolean isEmpty() {
        return tile == null;
    }

    /**
     * Obtient les informations d'une case
     */
    public void info(DisplayMode displayMode) {
        if (displayMode == DisplayMode.TERMINAL) {
            System.out.println("Informations de la case " + getStrCoords() + " :");
            System.out.println("Case " + isStartCase());
            System.out.println("Tile " + isThereTile());
        }
    }

    public String isStartCase() {
        String result = "";

        if (isStart()) {
            result = "[" + caseType.getId() + "] (" + caseType.getName() + " " + caseType.getDirection() + ")";

        } else {
            result += caseType.getCompleteName();
        }
        return result;
    }

    public String isThereTile() {
        if (isEmpty())
            return "✘";

        String result = "✔ (";
        TileType tileType = tile.getTileType();

        if (tileType.isDirectional()) {
            result += tileType.getName() + " " + tile.getDirection();
        }
        return result + ")";
    }

    public String getStrCoords() {
        return "(" + i + ", " + j + ")";
    }

    @Override
    public String toString() {
        if (tile != null)
            return tile.getTileType().getSymbol();

        return caseType.toString();
    }
}