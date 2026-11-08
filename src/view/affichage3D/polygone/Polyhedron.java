package view.affichage3D.polygone;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JComponent;

import view.affichage3D.Coord3D;
import view.affichage3D.Transformation3D;

public class Polyhedron extends JComponent{

    /** LIstedes faces du polyhedron */
    protected ArrayList<Polygon3D> faces;

    protected ArrayList<Coord3D> points;

    protected Coord3D center;

    protected double height = 0;

    /**
     * Constructeur du polyhedron
     * 
     * @param f Les faces du polyhedron
     */
    public Polyhedron(ArrayList<Polygon3D> f, ArrayList<Coord3D> p) {
        faces = f;
        points = p;
    }

    /**
     * Sert a créer un polyhedron à la base d'un polygone
     * 
     * @param poly la face sur laquelle on place une tuile
     * @param height la hauteur du polyhedron
     */
    public Polyhedron(Polygon3D poly, double height) {

        this();

        this.height = height;
        addFace(poly);
        defineFaces(height);
        initialiseCenter();
        
        adjustFacesNormal();
    }

    /**
     * COnstructeur qui initialise faces comme une liste vide
     */
    public Polyhedron() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public Coord3D getCenter() {
        return center;
    }

    public ArrayList<Coord3D> getPoints() {
        return points;
    }

    public ArrayList<Polygon3D> getFaces() {
        return faces;
    }

    /**
     * Ajoute une nouvelle face du polyhedron
     * @param face Une nouvelle face quon veut ajouter
     */
    public void addFace(Polygon3D face) {
        faces.add(face);
        for(Coord3D p : face.getPoints()) addPoint(p);
    }

    public void addPoint(Coord3D point) {
        if(!points.contains(point)) points.add(point);
    }

    public void initialiseCenter() {
        // il faut aussi inclure arrows
        if(points.size() == 8) {
            double x = (points.get(0).getX() + points.get(1).getX()) / 2;
            double y = (points.get(0).getY() + points.get(4).getY()) / 2;
            double z = (points.get(1).getZ() + points.get(2).getZ()) / 2;
            center = new Coord3D(x, y, z);
        }
    }

    /**
     * @return La liste des faces du polyhedron projectées en 2D
     */
    public ArrayList<Polygon> projecton2D() {
        ArrayList<Polygon> faces2D = new ArrayList<>();
        for(Polygon3D face : faces) faces2D.add(face.projection2D());
        return faces2D;
    }

    public void adjustFacesNormal() {
        for(Polygon3D face : faces) {
            Coord3D Q = null;
            for (Coord3D point : points) {
                if (!face.isPoint(point)) {
                    Q = point;
                    break;
                }
            }
            if (Q != null) {
                face.adjustNormal(Q);
            } else throw new IllegalStateException("Q est null dans ajustFacesNormal de Polyhedron");
        }
    }

    /**
     * Utilisé dans le constructeur avec polygon3D et height, permet de definir ses faces dans l'espace 3D
     */
    public void defineFaces(double height) {
        ArrayList<Coord3D> facePoints = faces.get(0).getPoints();

        Polygon3D lowerFace = new Polygon3D();

        for(Coord3D p : facePoints) {
            lowerFace.addPoint(p.getX(), p.getY() + height, p.getZ());
        }

        addFace(lowerFace);

        int size = lowerFace.getPoints().size();

        for(int i = 0; i < size; i++) {
            int j = (i==size-1) ? 0 : i+1;
            addFace(new Polygon3D(new ArrayList<>(Arrays.asList(points.get(i), points.get(j), points.get(size+j), points.get(size+i))), null));
        } 
    }

    /**
     * Applique des transformations sur un cuboide 
     * 
     * @param transformations Les transformations3D qu'on souhaite appliquer, dans l'ordre souhaité
     */
    public void applyTransformations(Transformation3D...transformations) {
        for(Coord3D point : points) {
            for(Transformation3D t : transformations) t.apply(point);
        }
        if(center!=null) for(Transformation3D t : transformations) t.apply(center);
    }


}
