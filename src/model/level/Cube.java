package model.level;

import model.exceptions.WrongDimensionException;
import model.level.GameState.DisplayMode;
import model.level.plate.Case;
import model.level.plate.Plate;
import model.level.tiles.Tile;
import model.level.tiles.TileType;
import model.tools.Debug;
import model.tools.Direction;

import java.util.ArrayList;
import java.util.List;

public class Cube {
    private int i, j;
    private int angle;
    private Case currentCase;
    private Direction facingDirection;
    private boolean isArrived;
    private Direction savedDirection; // var pour sauvegarder la direction
    private boolean isOnSlide;
    private Plate plate;
    private boolean hasJustTeleported = false;
    private boolean onTeleportation = false;
    private GameState gameState;

    /**
     * Initialise le cube sur la case de départ
     *
     * @param startCase
     */
    public Cube(Plate plate, Case startCase) {
        this(plate, startCase, startCase.getDirection() != null ? startCase.getDirection() : Direction.NORTH);
    }

    public Cube(Plate plate, Case currentCase, Direction direction) {
        this.plate = plate;
        this.i = currentCase.getI();
        this.j = currentCase.getJ();
        this.currentCase = currentCase;
        this.facingDirection = direction != null ? direction : Direction.NORTH;
        this.angle = 0;
    }

    /*
     * Constructeur permettant de créer une copie d'un cube
     */
    public Cube(Cube original, Plate newPlate) {
        this.i = original.getI();
        this.j = original.getJ();
        this.currentCase = newPlate.getCase(i, j);
        this.facingDirection = original.getFacingDirection();
        this.isArrived = original.isArrived();
        this.savedDirection = original.getSavedDirection();
        this.isOnSlide = original.isOnSlide();
        this.plate = newPlate; // Utilise le plateau fourni, pas de nouvelle copie
    }

    public Direction getSavedDirection() {
        return savedDirection;
    }

    public boolean isOnSlide() {
        return isOnSlide;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public Case getCase() {
        return currentCase;
    }

    public boolean isArrived() {
        return isArrived;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Remplace la case actuelle par la nouvelle case
     * <p>
     * Instancie la case actuelle si elle est null
     *
     * @param nextCase la nouvelle case
     */
    public void setCase(Case nextCase) {
        this.currentCase = nextCase;
        // this.i = nextCase.getI();
        // this.j = nextCase.getJ();
        if (nextCase.isEmpty()) {
            isOnSlide = false;
            if (savedDirection != null) {
                this.facingDirection = savedDirection;
                savedDirection = null;
            }
            return;
        }
        TileType tileType = nextCase.getTile().getTileType();
        // Vérifier qu'il ne s'agit pas des tuiles START/END
        if (tileType != TileType.START && tileType != TileType.END) {
            switch (tileType) {
                case ARROW -> this.facingDirection = nextCase.getTile().getDirection();
                case SLIDE -> {
                    if (!isOnSlide) {
                        // Premier slide rencontré
                        savedDirection = this.facingDirection;
                        isOnSlide = true;
                    }
                    // Toujours suivre la direction du slide actuel
                    this.facingDirection = nextCase.getTile().getDirection();
                }
                case SPIN -> {
                    Direction tileDirection = nextCase.getTile().getDirection();
                    if (tileDirection != null) {
                        this.facingDirection = this.facingDirection.spin(1);
                    }
                }
                case TELEPORTATION -> {
                    if (hasJustTeleported) {
                        return;
                    }

                    if (!onTeleportation) {
                        onTeleportation = true;
                        return;
                    }

                    List<Case> teleporters = new ArrayList<>();
                    for (int row = 0; row < plate.getI(); row++) {
                        for (int col = 0; col < plate.getJ(); col++) {
                            Case c = plate.getCase(row, col);
                            if (c != null && !c.isEmpty() && c.getTile().getTileType() == TileType.TELEPORTATION) {
                                teleporters.add(c);
                            }
                        }
                    }

                    Case destination = teleporters.get(0).equals(nextCase) ? teleporters.get(1) : teleporters.get(0);

                    this.i = destination.getI();
                    this.j = destination.getJ();

                    hasJustTeleported = true;
                    onTeleportation = false;

                    this.setCase(destination);
                    return;
                }

                // ça ne doit rien faire
                case SWITCH_ON -> {
                }

                case SWITCH_OFF -> {
                    for (int i = 0; i < plate.getI(); i++) {
                        for (int j = 0; j < plate.getJ(); j++) {
                            Case c = plate.getCase(i, j);

                            if (c != null && c.isTransparent()) {
                                c.setVisible(true);
                            }

                            if (c.getTile() != null && c.getTile().getTileType() == TileType.TRANSPARENT_INVISIBLE) {
                                c.setTile(new Tile(TileType.TRANSPARENT_VISIBLE));
                            }
                        }
                    }
                } // à modif

                // case WHITE_BLOCKED -> {
                // moveBack();
                // }
                // case GRAY_BLOCKED -> {
                // moveBack();
                // }
                default -> {
                    // throw new IllegalArgumentException("Unexpected value: " +
                    // nextCase.getTile().getTileType());
                    // Réinitialiser le niveau
                    // this.gameState.setCurrentState(GameState.GameMode.DISPOSING);
                    // gameState.setCurrentState(GameState.GameMode.EXECUTING);
                    // this.i = plate.getStartCase().getI();
                    // this.j = plate.getStartCase().getJ();
                    // this.facingDirection = plate.getStartCase().getDirection();
                    // this.setCase(plate.getStartCase());
                }
            }
        }

        // Si on quitte une séquence de slides
        if (isOnSlide && nextCase.getTile().getTileType() != TileType.SLIDE) {
            isOnSlide = false;
            if (savedDirection != null) {
                this.facingDirection = savedDirection;
                savedDirection = null;
            }
        }

        if (tileType != TileType.TELEPORTATION) {
            onTeleportation = false;
            if (hasJustTeleported) {
                hasJustTeleported = false;
            }
        }
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction newDirection) {
        this.facingDirection = newDirection;
    }

    public void move() throws WrongDimensionException {
        if (this.facingDirection == null) {
            throw new WrongDimensionException("La direction du cube est null");
        }

        if (onTeleportation) {
            this.setCase(plate.getCase(i, j));
            return;
        }

        Case nextCase;

        // Déplacement selon la direction actuelle
        switch (this.facingDirection) {
            case NORTH -> i--;
            case SOUTH -> i++;
            case WEST -> j--;
            case EAST -> j++;
        }
        if (this.currentCase != null) {
            this.currentCase = null;
        }

        nextCase = plate.getCase(i, j);

        // Si la case suivante n'est pas un slide et qu'on avait une direction
        // sauvegardée
        /// idem peut-être pour TP
        if (savedDirection != null && (nextCase.isEmpty() || nextCase.getTile().getTileType() != TileType.SLIDE)) {
            this.facingDirection = savedDirection;
            savedDirection = null;
        }

        nextCase.check(Case::isBlocked, targetCase -> {
            throw new WrongDimensionException("La case " + targetCase.getStrCoords() + " ne suit pas le chemin...");
        });

        nextCase.check(Case::isEnd, targetCase -> {
            isArrived = true;
            System.out.println("Niveau complété !");
            return true;
        });

        if (nextCase.isInvisible()) {
            Debug.send(this.getClass(), "La case " + nextCase.getStrCoords() + " est invisible...");
            throw new WrongDimensionException(
                    "La case " + nextCase.getStrCoords() + " ne suit pas le chemin... (mais elle est invisible)");
        }

        this.setCase(nextCase);
        this.currentCase.info(gameState.getDisplayMode());
    }

    /**
     * Méthode sécondaire de deplacément de cube utilisé pour le test de A*
     * 
     * @return
     */
    public boolean moveCube() {
        if (this.facingDirection == null) {
            throw new WrongDimensionException("La direction du cube est null");
        }

        // Gestion du cas spécial : téléportation
        if (onTeleportation) {
            Case newCase = plate.getCaseOrNull(i, j);
            if (newCase == null || newCase.isBlocked()) {
                System.out.println("Téléportation vers case invalide.");
                return false;
            }
            this.setCase(newCase);
            return true;
        }
        if (currentCase != null)
            currentCase.info(DisplayMode.TERMINAL);

        int newI = i;
        int newJ = j;

        switch (this.facingDirection) {
            case NORTH -> newI--;
            case SOUTH -> newI++;
            case WEST -> newJ--;
            case EAST -> newJ++;
        }

        Case nextCase = plate.getCaseOrNull(newI, newJ);
        if (nextCase == null) {
            System.out.println("Tentative de sortie du plateau vers (" + newI + "," + newJ + ")");
            return false;
        }

        if (nextCase.isBlocked()) {
            System.out.println("La case (" + newI + "," + newJ + ") est bloquée.");
            return false;
        }
        if (savedDirection != null && (nextCase.isEmpty() || nextCase.getTile().getTileType() != TileType.SLIDE)) {
            this.facingDirection = savedDirection;
            savedDirection = null;
        }

        // Gestion de l'arrivée
        if (nextCase.isEnd()) {
            isArrived = true;
            System.out.println("Niveau complété !");
        }

        i = newI;
        j = newJ;
        this.setCase(nextCase);

        // Info case après déplacement
        if (currentCase != null)
            currentCase.info(DisplayMode.TERMINAL);

        return true;
    }

    /*
     * Déplace le cube sur le plateau selon sa direction actuelle.
     * Gère les cas spéciaux comme la téléportation et les slides.
     * Met à jour la position et l'état du cube.
     * 
     * @throws WrongDimensionException si la direction du cube est null ou si le
     * cube tente de se déplacer sur une case bloquée
     */
    public void moveBack() throws WrongDimensionException {
        int i = currentCase.getI();
        int j = currentCase.getJ();

        switch (facingDirection) {
            case NORTH -> i--;
            case SOUTH -> i++;
            case WEST -> j--;
            case EAST -> j++;
        }

        if (!plate.checkI(i) || !plate.checkJ(j)) {
            throw new WrongDimensionException("Le cube a quitté le plateau.");
        }

        this.currentCase = plate.getCase(i, j);
        this.currentCase.info(gameState.getDisplayMode());
    }

    public void rotate() {
        this.angle = normalizeAngle(this.angle + 90); // Tourne le cube de 90 degré
        this.facingDirection = this.facingDirection.spin();
    }

    public int normalizeAngle(int angle) {
        return ((angle % 360) + 360) % 360;
    }

    public boolean isOnCase(Case currentCase) {
        return this.currentCase == currentCase;
    }

    public Plate getPlate() {
        return plate;
    }
}