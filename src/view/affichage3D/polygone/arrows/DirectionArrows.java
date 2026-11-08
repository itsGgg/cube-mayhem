package view.affichage3D.polygone.arrows;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import model.tools.Direction;
import view.affichage3D.Coord3D;
import view.affichage3D.PointOfVue;
import view.affichage3D.Transformation3D;
import view.affichage3D.polygone.Polygon3D;
import view.affichage3D.polygone.Tile3D;

public class DirectionArrows {
    
    private Tile3D tile;
    private ArrayList<Arrow> arrows;
    private Coord3D center;

    public DirectionArrows(Tile3D tile) {

        this.tile = tile;
        int height = tile.getHeight();
        arrows = new ArrayList<>();

        Polygon3D polygon = tile.getFaces().get(1);

        for(int i = 0; i<4; i++) {

            int j = (i==3) ? 0 : i+1;
            Coord3D v0 = polygon.getPoints().get(i).copy();
            Coord3D v1 = polygon.getPoints().get(j).copy();

            ArrowBase ab = new ArrowBase(v0, v1, i, 3*height);
            Arrow arrow = new Arrow(ab, height);
            arrows.add(arrow);
        }

        Coord3D tileCenter = tile.getCenter().copy();
        center = new Coord3D(tileCenter.getX(), -height*3.5, tileCenter.getZ());
    }

    public void applyTransformations(Transformation3D...transformations) {
        for(Arrow a : arrows) {
            a.applyTransformations(transformations);
        }
        center.applyTransformations(transformations);
    }
    
    public void renderArrows(Graphics2D g2D, PointOfVue pov) {
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if(arrows.size() > 0) {
            for(Arrow arr : arrows) {
                arr.renderArrow(g2D, pov);
            }
        }
    }

    public Direction mouseClicked(int x, int y) {
        for(int i = 0; i < arrows.size(); i++) {
            if(arrows.get(i).ab.projection2D().contains(x, y)) {
                tile.setDuringPlacing(false);
                return Direction.getfor3D(i);
            }
        }
        return null;
    }

    public int mouseEntered(int x, int y) {
        int dir = 4;
        for(int i = 0; i < arrows.size(); i++) {
            Arrow arr = arrows.get(i);
            if(arr.ab.projection2D().contains(x, y)) {
                arr.ab.isSelected = true;
                dir = i;
            } else if(arr.ab.isSelected) {
                arr.ab.isSelected = false;
            } 
        }
        return dir;
    }

}
