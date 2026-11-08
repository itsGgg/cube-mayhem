package view.affichage3D.polygone;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JComponent;

import model.level.tiles.Tile;
import model.level.tiles.TileBank;
import model.level.tiles.TileType;
import model.tools.Direction;
import view.affichage3D.Coord3D;
import view.affichage3D.PointOfVue;

public class Polygon3D extends JComponent {

    /** Les points du polygone */
    private ArrayList<Coord3D> points;
    /** Normale au plan contenant le polygon */
    private Coord3D normal;

    private BufferedImage image;

    private AffineTransform transform;

    private Color color;

    private Polygon poly;

    /**
     * Constructeur de Polynome3D
     * @param p Points du polynome
     */
    public Polygon3D(ArrayList<Coord3D> p, Color c) {
        points = p;
        //calculNormal();
        color = c;
        setVisible(true);
        image = null;
        poly = projection2D();
    }

    /**
     * Constructeur qui l'initialise avec un ArrayList vide
     */
    public Polygon3D(Color c) {
        this(new ArrayList<>(), c);
    }

    public Polygon3D() {
        this(new ArrayList<>(), null);
    }

    public ArrayList<Coord3D> getPoints() {
        return points;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }

    /**
     * Ajout d'un image dans le Polygon3D (principalement pour afficher une tuile)
     * @param direction La direction de l'image
     */
    public void setImage(Tile tile) {
        try{
                if(tile.getTileType() == TileType.SPIN) tile.setDirection(Direction.EAST);
                image = TileBank.getTileImage(tile.getTileType(), tile.getDirection());
                initialiseTransform(tile);
            }
            catch(Exception e){ 
                e.printStackTrace();
            }
    }

    public void removeImage() {
        image = null;
        transform = null;
    }

    public BufferedImage getImage() {
        return image;
    }    

    public AffineTransform getTransform() {
        return transform;
    }

    public Polygon getPolygon2D() {
        return poly;
    }
    
    /**
     * Indique si p est un des points du polygone3D
     * @param p Le point qu'on veut vérifier
     * @return True si ub des points de this, sinon false
     */
    public boolean isPoint(Coord3D p) {
        return points.contains(p);
    }

    /**
     * Ajoute un nouvel point à l'ArrayList des coordonnées du polynome
     * @param x Coordonée sur l'axe des x
     * @param y Coordonnée sur l'axe des y 
     * @param z Coordonnée sur l'axe des z
     */
    public void addPoint(double x, double y, double z) {
        points.add(new Coord3D(x, y, z));
    }

    /**
     * Ajoute un nouvel point à l'ArrayList des coordonnées du polynome
     * @param point Le point qu'on veut ajouter au polynome / à la liste des points 
     */
    public void addPoint(Coord3D point) {
        points.add(point);
    }

    /**
     * Fait une copie de this, c-à-d, crée un Polygon3D avec les mêmes paramètres que this
     * @return La copie de this
     */
    public Polygon3D copy() {
        ArrayList<Coord3D> newPoints = new ArrayList<>();
        for(Coord3D p : points) {
            newPoints.add(p.copy());
        }
        return new Polygon3D(newPoints, color);
    }

    /**
     * Construit la projection de polynome3D (this) dans le plan 2D
     */
    public Polygon projection2D() {
        poly = new Polygon();
        for(Coord3D point : points) {
            int x = (int) point.getX();
            int y = (int) point.getY();
            poly.addPoint(x, y);
        }
        return poly;
    }

    /**
     * Calcule une normale indispendable pour l'identification des faces avant et arrière
     */
    public void calculNormal() {
        if(points.size() < 3) throw new IllegalStateException("Un polygone doit avoir au moins 3 points pour calculer une normale");
        Coord3D p0p1 = points.get(0).substract(points.get(1));
        Coord3D p0p2 = points.get(0).substract(points.get(2));
        
        normal = p0p1.produitVect(p0p2);
        normal.normalize();
    }

    /**
     * Ajouste la normale (on doit changer son signe si elle va vers l'exterieur du polyèdre)
     * @param Q Un sommet quelconque n'appartenant pas au polygone, mais du polyèdre
     */
    public void adjustNormal(Coord3D Q) {
        if(Q == null) throw new IllegalArgumentException("Q est null");
        calculNormal();
        normal.normalize();
        double d = normal.produitScal(points.get(0).substract(Q));
        if(d<0) {
            normal = new Coord3D(-normal.getX(), -normal.getY(), -normal.getZ());
        }
    }

    /**
     * Indique si on voit la face (donc this polygone3D)
     * @param pointOfVue Le point de vue
     * @return True, si visible, false sinon
     */
    public boolean isFront(PointOfVue pointOfVue) {
        if(normal == null) throw new IllegalStateException("La normale du polygone est nulle");
        Coord3D vue = pointOfVue.substract(points.get(0));
        vue.normalize();
        
        double produitScal = vue.produitScal(normal);
        
        return produitScal > 0;
    }

    /**
     * Initialise une transformationAffine pour correctement afficher l'image dans le Polygon3D
     */
    public void initialiseTransform(Tile tile) {
        //Polygon p = projection2D(new PointOfVue(0, 0, -5000));
        int[] xPoints = poly.xpoints;
        int[] yPoints = poly.ypoints;

        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);

        double cx0 = xPoints[0], cy0 = yPoints[0];  // Coin supérieur gauche
        double cx1 = xPoints[1], cy1 = yPoints[1];  // Coin supérieur droit
        double cx3 = xPoints[3], cy3 = yPoints[3];  // Coin inférieur droit
        
        transform = new AffineTransform(
            (cx1 - cx0) / imgWidth, (cy1 - cy0) / imgWidth,  
            (cx3 - cx0) / imgHeight, (cy3 - cy0) / imgHeight,  
            cx0, cy0
        );

        rotateImage(tile.getDirection());
    }

    public boolean isTranformEmpty() {
        if(transform == null) throw new IllegalArgumentException("transformation null");
        double[] m = new double[6];
        transform.getMatrix(m); 
        boolean allZero = true;
        for (double val : m) {
            if (val != 0.0) {
                allZero = false;
                break;
            }
        }
        return allZero;
    }

    /**
     * Comme affichage l'affichage est un peu différent en 3D, on doit inverser la tuile, si 
     * sa direction est EAST ou WEST
     * @param direction La direction de l'image
     */
    public void rotateImage(Direction direction) {

        if(direction == Direction.EAST || direction == Direction.WEST) {
            double centerX = image.getWidth(null) / 2.0;
            double centerY = image.getHeight(null) / 2.0;

            transform.rotate(Math.PI, centerX, centerY);
            transform.scale(1, -1);
            transform.translate(0, -image.getHeight(null));
        }
    }

    public void setImageDirection(Tile tile, int direction) {
        try{
                image = null;
                image = TileBank.getTileImage(tile.getTileType(), Direction.values()[direction]);
                initialiseTransform(tile);
                //rotateImage(tile.getDirection());
            }
            catch(Exception e){ 
                e.printStackTrace();
            }
    }

}

