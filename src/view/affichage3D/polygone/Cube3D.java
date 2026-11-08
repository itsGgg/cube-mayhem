package view.affichage3D.polygone;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;

import view.affichage3D.Coord3D;
import view.affichage3D.PointOfVue;

public class Cube3D extends Polyhedron {


    public Cube3D(Polygon3D polygon) {
        super(polygon, calculHeight(polygon));
    }

    public double getCubeHeight() {
        return height;
    }

    /**
     * A utiliser seulement dans le constructeur, crée pour minimaliser le nombre d'appels dans le constucteur.
     * Calcul la ahuteur du cube3D
     * 
     * @param tile
     * @return La hauteur du cube3D
     */
    public static double calculHeight(Polygon3D polygon) {
            ArrayList<Coord3D> polygonPoints = polygon.getPoints();
            return -polygonPoints.get(0).distance(polygonPoints.get(1));
    }

}
