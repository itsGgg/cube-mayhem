package view.affichage3D;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.Timer;

import model.level.Cube;
import model.level.plate.Case;
import model.level.plate.Plate;
import view.Window;
import view.affichage3D.polygone.GameCube3D;
import view.affichage3D.polygone.Polygon3D;
import view.affichage3D.polygone.Tile3D;
import view.affichage3D.polygone.UnderTile3D;
import view.affichage3D.polygone.arrows.DirectionArrows;

public class Plate3D {

    private Plate plate;
    private ArrayList<UnderTile3D> underTiles = new ArrayList<>();
    private ArrayList<Tile3D> tiles3D = new ArrayList<>();
    private boolean isEditorMode = false;

    private boolean isAnimating = false;
    private Timer animationTimer;
    private Timer waitTimer;
    private LinkedList<Tile3D> animationQueue = new LinkedList<>();
    private BufferedImage staticLayer;
    public static boolean staticLayerNeedsUpdate = true;
    private Coord3D center;
    public static boolean isSwitchButtonOn = false;

    private GameCube3D cube3D;

    public Plate3D(Plate plate, boolean isEditorMode) {
        this.plate = plate;
        initialiseCases(plate.getCube());
        this.isEditorMode = isEditorMode;
        //staticLayerNeedsUpdate = true;
    }

    public void addUnderTile(UnderTile3D ut) {
        underTiles.add(ut);
    }

    public void addTile3D(Tile3D tile) {
        tiles3D.add(tile);
    }

    public ArrayList<UnderTile3D> getUnderTiles() {
        return underTiles;
    }

    public ArrayList<Tile3D> getTiles3D() {
        return tiles3D;
    }

    /**
     * Initialise le plateau3D
     */
    public void initialiseCases(Cube cube) {

        int rows = plate.getI();
        int cols = plate.getJ();
        double sideLen;

        Coord3D[] corners = new Coord3D[4]; // permettra de calculer le centre du plateau

        if (Window.height / rows < Window.width / cols)
            sideLen = Window.height * 0.8 / rows;
        else
            sideLen = Window.width * 0.8 / cols;

        double baseX = sideLen * cols / 2;
        double currentX = baseX;
        double currentZ = sideLen * rows / 2;

        int maxUtHeight = (int) sideLen * 2 / 3;
        int minUtHeight = (int) sideLen / 4;
        int tile3DHeight = (int) sideLen / 6;

        for (int i = 0; i < rows; i++) {
            for (int j = cols - 1; j >= 0; j--) {
                Case c = plate.getCase(i, j);

                if (isEditorMode || c.getCaseType().getId() != 0) {
                    ArrayList<Coord3D> casePoints = new ArrayList<>();

                    casePoints.add(new Coord3D(currentX, 0, currentZ));
                    casePoints.add(new Coord3D(currentX - sideLen, 0, currentZ));
                    casePoints.add(new Coord3D(currentX - sideLen, 0, currentZ - sideLen));
                    casePoints.add(new Coord3D(currentX, 0, currentZ - sideLen));

                    UnderTile3D ut = new UnderTile3D(new Polygon3D(casePoints, null), maxUtHeight, minUtHeight);
                    Tile3D td = new Tile3D(ut, c, tile3DHeight);

                    td.adjustColor(c.getCaseType().getId());
                    addUnderTile(ut);
                    addTile3D(td);
                }

                if (!isEditorMode && cube != null && cube.getCase() == c)
                    cube3D = new GameCube3D(tiles3D.get(tiles3D.size() - 1), cube);
                currentX -= sideLen;

                if (i == 0) {
                    if (j == cols - 1)
                        corners[0] = new Coord3D(currentX + sideLen, 0, currentZ);
                    else if (j == 0)
                        corners[1] = new Coord3D(currentX, 0, currentZ);
                } else if (i == rows - 1) {
                    if (j == cols - 1)
                        corners[3] = new Coord3D(currentX + sideLen, 0, currentZ - sideLen);
                    else if (j == 0)
                        corners[2] = new Coord3D(currentX, 0, currentZ - sideLen);
                }
            }
            currentX = baseX;
            currentZ -= sideLen;
        }
        initialiseCenter(corners);
        staticLayerNeedsUpdate = true;
    }

    public void initialiseCenter(Coord3D[] corners) {
        double x = (corners[0].getX() + corners[1].getX()) / 2;
        double z = (corners[1].getZ() + corners[2].getZ()) / 2;
        center = new Coord3D(x, 0, z);
    }

    /**
     * Applique des Transformations3D sur les objets qui constituent le plateau
     * 
     * @param transformations Les transformations3D qu'on souhaite appliquer, dans
     *                        l'ordre souhaité
     */
    public void applyInitialTransformations() {
        Transformation3D transformations[] = Panel3D.getInitialTransformations();
        for (UnderTile3D ut : underTiles) {
            ut.applyTransformations(transformations);
            ut.adjustFacesNormal();
        }
        for (Tile3D tile : tiles3D) {
            tile.applyTransformations(transformations);
            tile.getDirArrows().applyTransformations(transformations);
            if(tile.hasButton()) {
                System.out.println("hello");
                tile.getSwitchButton3D().applyTransformations(transformations);
                tile.getSwitchButton3D().adjustFacesNormal();
            }
            tile.adjustFacesNormal();
        }
        if (cube3D != null) {
            cube3D.applyTransformations(transformations);
            cube3D.adjustFacesNormal();
        }
        center.applyTransformations(transformations);
        staticLayerNeedsUpdate = true;
    }

    /**
     * Applique des Transformations3D sur les objets qui constituent le plateau
     * 
     * @param transformations Les transformations3D qu'on souhaite appliquer, dans
     *                        l'ordre souhaité
     */
    public void applyTransformations(Transformation3D... transformations) {
        for (UnderTile3D ut : underTiles) {
            ut.applyTransformations(transformations);
        }
        for (Tile3D tile : tiles3D) {
            tile.applyTransformations(transformations);
            tile.getDirArrows().applyTransformations(transformations);
            if(tile.hasButton()) {
                tile.getSwitchButton3D().applyTransformations(transformations);
            }
        }
        cube3D.applyTransformations(transformations);
        center.applyTransformations(transformations);
        staticLayerNeedsUpdate = true;
    }

    /**
     * Dessine /affiche le plateau en 3D
     * 
     * @param g2D
     * @param pov
     */
    public void renderPlate3D(Graphics2D g2D, PointOfVue pov) {
        if (staticLayerNeedsUpdate || staticLayer == null) {

            if (cube3D == null || !cube3D.getFalling()) {
                generateStaticLayer(pov);

            } else { // le cas où le cube doit se cacher derrière tuiles devant
                Tile3D t;
                DirectionArrows arrows = null;
                for (int i = 0; i < underTiles.size(); i++) {
                    underTiles.get(i).renderUnderTile(g2D, pov);

                    t = tiles3D.get(i);

                    t.renderTile3D(g2D, pov);
                    if (t.getDuringPlacing())
                        arrows = t.getDirArrows();

                    if (cube3D != null && cube3D.getTile3D() == t && cube3D.isVisible()) {
                        cube3D.renderGameCube3D(g2D, pov);
                    }
                    if (arrows != null)
                        arrows.renderArrows(g2D, pov);
                }
            }
        }
        g2D.drawImage(staticLayer, 0, 0, null);
        if (cube3D != null && cube3D.isVisible())
            cube3D.renderGameCube3D(g2D, pov);
    }

    private void generateStaticLayer(PointOfVue pov) {
        staticLayer = new BufferedImage((int) Window.width, (int) Window.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gStatic = staticLayer.createGraphics();

        for (UnderTile3D ut : underTiles) {
            ut.renderUnderTile(gStatic, pov);
        }

        DirectionArrows arrows = null;

        for (Tile3D t : tiles3D) {
            t.renderTile3D(gStatic, pov);
            if (t.getDuringPlacing()) {
                arrows = t.getDirArrows();
            }
        }

        if (arrows != null) {
            arrows.renderArrows(gStatic, pov);
        }

        gStatic.dispose();
        staticLayerNeedsUpdate = false;
    }

    /**
     * Mise à jour du plateau (interface 3D)
     */
    public void update() {
        for (Tile3D tile : tiles3D) {
            tile.update();
            if (cube3D.positionChanged() && cube3D.getCase() == tile.getCase()) {
                cube3D.updatePosition(tile);
            }
        }
    }

    public void move() {
        Coord3D v = new Coord3D((Window.width / 2 - center.getX()), (Window.height * 0.55 - center.getY()),
                center.getZ());
        Transformation3D trans = Transformation3D.translation(v);
        applyTransformations(trans);
    }

    public void resize(double scale) {
        Coord3D cenCopy = center.copy();
        Transformation3D toCenter = Transformation3D.translation(-cenCopy.getX(), -cenCopy.getY(), -cenCopy.getZ());
        Transformation3D resize = Transformation3D.scale(scale, scale, scale);
        Transformation3D back = Transformation3D.translation(cenCopy.getX(), cenCopy.getY(), cenCopy.getZ());
        applyTransformations(toCenter, resize, back);
    }

    public void addAnimationToQueue(Tile3D tile3D) {
        animationQueue.add(tile3D);
        if (animationQueue.size() == 1) {
            adaptTimer(animationQueue.peek());
        }
    }

    public void adaptTimer(Tile3D tile3D) {
        if(!isAnimating && tile3D.getTile() != null) {
            placeTileAnimation(tile3D);
        } else {
            if (waitTimer != null && waitTimer.isRunning()) return;
            waitTimer = new Timer(15, null);
            waitTimer.addActionListener(e -> {
                if (tile3D.getTile() != null) {
                    waitTimer.stop();
                    placeTileAnimation(tile3D); 
                }
            });
            waitTimer.start();
        }
    }

    public void placeTileAnimation(Tile3D tile3D) {
        if(!isAnimating && tile3D.getTile() != null) {
            isAnimating = true;

            UnderTile3D ut = tile3D.getUnderTile3D();
            tile3D.setDuringAnimation(true);

            int steps = 45;

            Coord3D tCenter = tile3D.getCenter();

            Coord3D v = tCenter
                    .substract(new Coord3D(tCenter.getX(), tCenter.getY() + cube3D.getCubeHeight(), tCenter.getZ()));

            Transformation3D t = Transformation3D.translation(v.multiply(-1));
            tile3D.applyTransformations(t);

            Coord3D v2 = v.devide(steps / 3);
            Transformation3D t2 = Transformation3D.translation(v2);
            Coord3D v3 = v2.multiply(-1);
            Transformation3D t3 = Transformation3D.translation(v3);

            animationTimer = new Timer(15, new ActionListener() {
                int currentStep = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    MouseInfo.getPointerInfo();

                    if (currentStep < 30) {
                        tile3D.applyTransformations(t2);
                        if (currentStep > 14) {
                            ut.applyTransformations(t2);
                        }
                    } else {
                        tile3D.applyTransformations(t3);
                        ut.applyTransformations(t3);
                    }
                    currentStep++;

                    staticLayerNeedsUpdate = true;

                    if (currentStep == steps) {
                        animationTimer.stop();
                        animationQueue.poll();
                        tile3D.setDuringAnimation(false);
                        isAnimating = false;
                        //Plate3D.staticLayerNeedsUpdate = true;
                        if (!animationQueue.isEmpty()) {
                            adaptTimer(animationQueue.peek());
                        }
                    }
                }
            });
            animationTimer.start();          
        }
    }

    public GameCube3D getGameCube3D() {
        return cube3D;
    }

    /*
    public Map<Case, Tile3D> getTileMap() {
        Map<Case, Tile3D> map = new HashMap<>();
        for (Tile3D tile : tiles3D) {
            map.put(tile.getCase(), tile);
        }
        return map;
    }
        */
}
