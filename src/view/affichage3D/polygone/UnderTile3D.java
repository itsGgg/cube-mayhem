package view.affichage3D.polygone;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import model.tools.Constants;
import view.affichage3D.PointOfVue;

/** Est une block sur lequel on place une tuile */
public class UnderTile3D extends Polyhedron {

    private boolean isVisible = true;

    /**
     * Un block sur lequel on place une tuile 
     * @param poly la face sur laquelle on place une tuile
     */
    public UnderTile3D(Polygon3D poly, int maxHeight, int minHeight) {

        super(poly, minHeight + (maxHeight - minHeight) * Math.random());

    }

    /**
     * On l'utilise dans Tile3D si la case est de type TRANSPARENT, 
     * la UnderTile3D ne sera pas dessinée 
     */
    public void setInvisible() {
        isVisible = false;
    }

    /**
     * Dessine la "sous-tuile" en 3D
     * @param g2D
     * @param pov
     */
    public void renderUnderTile(Graphics2D g2D, PointOfVue pov) {
        if(isVisible) {
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Polygon3D face;
            Polygon p;
            for(int i = 0; i < 6; i++) {
                face = faces.get(i);
                p = face.projection2D();
    
                if (p != null && p.npoints > 0) {    
                    if(face.isFront(pov)) {
                        if(i!=3) g2D.setColor(Constants.Colors3D.UTGRAY);
                        else g2D.setColor(Constants.Colors3D.UTGRAYSHADOW);
                        g2D.fillPolygon(p);
                        //g2D.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                        g2D.setColor(Color.BLACK);
                        g2D.drawPolygon(p);
                    }
                } else { 
                    System.out.println("Face vide ou projection non valide");
                }
            }
        }
    }

}
