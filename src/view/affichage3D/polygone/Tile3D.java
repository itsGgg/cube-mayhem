package view.affichage3D.polygone;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import model.level.plate.Case;
import model.level.plate.CaseType;
import model.level.tiles.Tile;
import model.tools.Constants;
import model.level.tiles.TileType;
import model.tools.Direction;
import view.affichage3D.Coord3D;
import view.affichage3D.Inventaire3D;
import view.affichage3D.Plate3D;
import view.affichage3D.PointOfVue;
import view.affichage3D.polygone.arrows.DirectionArrows;

/** Tuile en 3D */
public class Tile3D extends Polyhedron {

    private Case c;
    private Tile t;
    private UnderTile3D underT;
    private Direction tileDirection;
    private int height;
    private DirectionArrows dirArrows;
    private SwitchButton3D switchButton3D;

    private boolean visible = true;
    private boolean duringPlacing = false;
    private boolean hasChanged;
    private boolean duringAnimation = false;

    /**
     * Constructeur de Tile3D
     * 
     * @param underTile L'UnderTile3D sur laquelle pn veut placer la Tile3D
     */
    public Tile3D(UnderTile3D underTile, Case c, int height) {
        super(underTile.getFaces().get(0).copy(), -height);

        this.c = c;
        this.height = height;
        underT = underTile;

        adjustColor(c.getCaseType().getId());
        if(c.getTile() != null) {
            t= c.getTile();
            placeTile(t);
            if (t.getTileType() == TileType.TRANSPARENT_INVISIBLE) {
                underT.setInvisible();
                this.visible = false;
            } else if (t.getTileType() == TileType.TRANSPARENT_VISIBLE) {
                this.visible = true;
            }
        } else if (c.getCaseType() == CaseType.TRANSPARENT && !c.isVisible()) {
            underT.setInvisible();
            this.visible = false;
        }
        dirArrows = new DirectionArrows(this);
    }

    public Case getCase() {
        return c;
    }

    public Tile getTile() {
        return t;
    }

    public int getHeight() {
        return height;
    }

    public DirectionArrows getDirArrows() {
        return dirArrows;
    }

    public void setDuringPlacing(boolean b) {
        duringPlacing = b;
    }

    public boolean getDuringPlacing() {
        return duringPlacing;
    }

    public UnderTile3D getUnderTile3D() {
        return underT;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setDuringAnimation(boolean b) {
        duringAnimation = b;
    }

    public boolean hasButton() {
        return switchButton3D != null;
    }

    public SwitchButton3D getSwitchButton3D() {
        return switchButton3D;
    }

    public void update() {
        if (c.getTile() != null && t == null) {
            t = c.getTile();
            tileDirection = t.getDirection();
            placeTile(t);
            Plate3D.staticLayerNeedsUpdate = true;
        } else if (c.getTile() == null && t != null) {
            removeTile();
            revalidate();
            Plate3D.staticLayerNeedsUpdate = true;
        } else if (c.getTile() != null && t != null) {
            if(c.getTile() != t) {
                removeTile();
                t = c.getTile();
                placeTile(t);
            }
            if (t.getDirection() != tileDirection) {
                tileDirection = t.getDirection();
                Plate3D.staticLayerNeedsUpdate = true;
            }
        }
        if (c.getCaseType() == CaseType.TRANSPARENT && visible != c.isVisible()) {
            this.visible = c.isVisible();
            adjustColor(c.getCaseType().getId());
            Plate3D.staticLayerNeedsUpdate = true;
        }
    }

    /**
     * Change la couleur de la case selon sa fonction
     * 
     * @param id CaseType
     */
    public void adjustColor(int id) {
        Color color = null;
        switch (id) {
            case 1: // PATH
                if (c.getI() % 2 == c.getJ() % 2) {
                    color = Constants.Case.WHITE;
                    faces.get(3).setColor(Constants.Colors3D.WHITESHADOW);
                } else {
                    color = Constants.Case.GRAY;
                    faces.get(3).setColor(Constants.Colors3D.GRAYSHADOW);
                }
                break;
            case 2: // TRANSPARENT
                if (c.isVisible()) {
                    visible = true;
                    color = Constants.Case.TRANSPARENT_VISIBLE;
                } else {
                    visible = false;
                    color = Constants.Case.TRANSPARENT;
                }
                break;
            case 3: // RESTRICT
                if (c.getI() % 2 == c.getJ() % 2) {
                    color = Constants.Case.WHITE;
                    faces.get(3).setColor(Constants.Colors3D.WHITESHADOW);
                } else {
                    color = Constants.Case.GRAY;
                    faces.get(3).setColor(Constants.Colors3D.GRAYSHADOW);
                }
                break;
            default:
                color = Color.YELLOW; // a changer, pour remarquer des changements
                break;
        }
        for (int i = 1; i < 6; i++) {
            if (i != 3)
                faces.get(i).setColor(color);
        }
        Plate3D.staticLayerNeedsUpdate = true;
    }

    /**
     * Place l'image de la tuile sur la tuile
     * 
     * @param tile La tuile à placer
     */
    public void placeTile(Tile tile) {
        if(tile.getTileType() != TileType.SWITCH_OFF) faces.get(1).setImage(tile);
        switch (tile.getTileType()) {
            case ARROW:
                for (int i = 1; i < 6; i++)
                    faces.get(i).setColor(Constants.Color.ARROW);
                faces.get(3).setColor(Constants.Color.ARROW_SHADOW);
                break;
            case SLIDE:
                for (int i = 1; i < 6; i++)
                    faces.get(i).setColor(Constants.Color.SLIDE);
                faces.get(3).setColor(Constants.Color.SLIDE_SHADOW);
                break;
            case SPIN:
                for (int i = 1; i < 6; i++)
                    faces.get(i).setColor(Constants.Color.SPIN);
                faces.get(3).setColor(Constants.Color.SPIN_SHADOW);
                break;
            case TELEPORTATION:
                for (int i = 1; i < 6; i++)
                    faces.get(i).setColor(Constants.Color.TELEPORTATION);
                faces.get(3).setColor(Constants.Color.TELEPORTATION_SHADOW);
                break;
            case SWITCH_ON:
                System.out.println("onnnnn");
                //switchButton3D = new SwitchButton3D(this);
                if(switchButton3D != null) switchButton3D.setOn(true);
                else throw new IllegalStateException("switchButton3D est null");
                /*
                for (int i = 1; i < 6; i++)
                    faces.get(i).setColor(Constants.Color.SWITCH_OFF);
                */
                break;
            case SWITCH_OFF:
                if(switchButton3D == null) switchButton3D = new SwitchButton3D(this);
                else switchButton3D.setOn(false);
                for (int i = 1; i < 6; i++)
                    faces.get(i).setColor(Constants.Color.SWITCH_OFF);
                break;
            default:
                break;
        }
        Plate3D.staticLayerNeedsUpdate = true;
    }

    public void removeTile() {
        faces.get(1).removeImage();
        adjustColor(c.getCaseType().getId());
        t = null;
        Plate3D.staticLayerNeedsUpdate = true;
    }

    /**
     * Dessine la tuile 3D
     * 
     * @param g2D
     * @param pov
     */
    public void renderTile3D(Graphics2D g2D, PointOfVue pov) {
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Polygon3D face : faces) {
            Polygon p = face.projection2D();

            if (p != null && p.npoints > 0) {
                if(!visible) {
                    if (face.getColor() != null) {
                        g2D.setColor(face.getColor());
                        g2D.fillPolygon(p);
                        g2D.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                        g2D.setColor(Constants.Case.WHITE);
                        g2D.drawPolygon(p);
                    }
                }
                else if (face.isFront(pov)) {
                    if (face.getColor() != null) {
                        g2D.setColor(face.getColor());
                        g2D.fillPolygon(p);
                        g2D.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                        g2D.setColor(Color.BLACK);
                        g2D.drawPolygon(p);
                    }
                    if (face.getImage() != null && !duringAnimation) {
                        if(face.isTranformEmpty()) face.initialiseTransform(t);
                        g2D.drawImage(face.getImage(), face.getTransform(), face);
                    }
                }
                if(switchButton3D != null) switchButton3D.renderSwitchButton3D(g2D, pov);
            } else {
                System.out.println("Face vide ou projection non valide");
            }
        }
        // hasChanged = false;
    }

    public void mouseEntered(int x, int y, TileType selectedTileType) {
        if ((faces.get(1).projection2D().contains(x, y) && Inventaire3D.getCurrentTile() == null) &&
                t == null && c.getTile() == null && c.isType(CaseType.PATH)) {
            placeTile(new Tile(selectedTileType));
        } else if (duringPlacing) {
            int dir = dirArrows.mouseEntered(x, y);
            if (dir != 4)
                faces.get(1).setImageDirection(new Tile(selectedTileType), dir);
            Plate3D.staticLayerNeedsUpdate = true;
        } else if (faces.get(1).getImage() != null && t == null) {
            removeTile();
        }
    }

    public Case mouseClicked(int x, int y) {
        if (faces.get(1).projection2D().contains(x, y)) {
            return c;
        }
        return null;
    }

    public Coord3D getSideVector(Direction dir) {
        Coord3D v = null;
        Direction direction = dir;
        if (t != null)
            direction = t.getDirection();
        switch (direction) {
            case NORTH:
                v = points.get(1).substract(points.get(2));
                break;
            case SOUTH:
                v = points.get(2).substract(points.get(1));
                break;
            case WEST:
                v = points.get(1).substract(points.get(0));
                break;
            case EAST:
                v = points.get(0).substract(points.get(1));
                break;
            default:
                break;
        }
        return v;
    }

    public Coord3D getSideVector() {
        if (t != null) {
            return getSideVector(t.getDirection());
        } else
            throw new IllegalStateException("tile de la tuile3D est null");
    }

    public Coord3D[] getEdgePoints(Direction dir) {
        Coord3D[] edgePoints = new Coord3D[2];
        Direction direction = dir;
        if (t != null)
            direction = t.getDirection();
        switch (direction) {
            case NORTH:
                edgePoints[0] = points.get(5);
                edgePoints[1] = points.get(4);
                break;
            case SOUTH:
                edgePoints[0] = points.get(7);
                edgePoints[1] = points.get(6);
                break;
            case WEST:
                edgePoints[0] = points.get(6);
                edgePoints[1] = points.get(5);
                break;
            case EAST:
                edgePoints[0] = points.get(4);
                edgePoints[1] = points.get(7);
                break;
            default:
                break;
        }
        return edgePoints;
    }

    public boolean isStart() {
        return t != null && t.isType(TileType.START);
    }

}
