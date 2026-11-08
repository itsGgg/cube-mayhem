package view.affichage3D.polygone;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import view.affichage3D.Coord3D;
import view.affichage3D.PointOfVue;
import view.affichage3D.Transformation3D;

public class SwitchButton3D extends Polyhedron{

    private Tile3D tile;
    private boolean isOn = false;

    public SwitchButton3D(Tile3D tile) {
        super(tile.getFaces().get(1).copy(), -tile.getHeight());
        initialResize();
        for(Polygon3D face : faces) face.setColor(Color.RED);
    }

    public void setOn(boolean b) {
        isOn = b;
    }

    public boolean isOn() {
        return isOn;
    }

    public void initialResize(){
        Coord3D cenCopy = center.copy();
        Transformation3D toCenter = Transformation3D.translation(-cenCopy.getX(), -cenCopy.getY(), -cenCopy.getZ());
        Transformation3D resize = Transformation3D.scale(0.75, 0.75, 0.75);
        Transformation3D back = Transformation3D.translation(cenCopy.getX(), cenCopy.getY(), cenCopy.getZ());
        Transformation3D putOnTile = Transformation3D.translation(0, -height/4, 0);
        applyTransformations(toCenter, resize, back, putOnTile);
    }
    

    public void resize() {

    }

    public void renderSwitchButton3D(Graphics2D g2D, PointOfVue pov) {
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        Polygon p;
        if(!isOn) {
            for(Polygon3D face : faces) {
                p = face.projection2D();
                if(face.isFront(pov)) {
                    g2D.setColor(Color.RED);
                    g2D.fillPolygon(p);
                    g2D.setColor(Color.BLACK);
                    g2D.drawPolygon(p);
                }
            }
        } else {
            p = faces.get(0).projection2D();
            g2D.setColor(Color.RED);
            g2D.fillPolygon(p);
            g2D.setColor(Color.BLACK);
            g2D.drawPolygon(p);
        }
        
    }

}
