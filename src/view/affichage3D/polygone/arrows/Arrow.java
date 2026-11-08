package view.affichage3D.polygone.arrows;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import view.affichage3D.PointOfVue;
import view.affichage3D.polygone.Polyhedron;

public class Arrow extends Polyhedron {

    private Color colorBase = new Color(30, 120, 175);
    private Color colorSelected = new Color(80, 155, 200);
    private Color colorSides = new Color(15, 90, 135);
    private Color colorSidesSelected = new Color(30, 120, 175);
    protected ArrowBase ab; 

    public Arrow(ArrowBase ab, int height) {
        super(ab, -height);
        this.ab = ab;
    }

    public ArrowBase getArrowBase() {
        return ab;
    }

    public void renderArrow(Graphics2D g2D, PointOfVue pov) {
        for (int i = faces.size()-1; i > 0; i--) {
            
            Polygon p = faces.get(i).projection2D();

            if (p != null && p.npoints > 0) {    
                if(i == 1) g2D.setColor(ab.isSelected ? colorSelected : colorBase);
                else g2D.setColor(ab.isSelected ? colorSidesSelected : colorSides);
                g2D.fillPolygon(p);
            } 
        }
    }

}
