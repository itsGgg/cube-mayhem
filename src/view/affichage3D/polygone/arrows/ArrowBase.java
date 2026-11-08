package view.affichage3D.polygone.arrows;

import view.affichage3D.Coord3D;
import view.affichage3D.polygone.Polygon3D;

public class ArrowBase extends Polygon3D {

    protected boolean isSelected = false;
    private int height;

    /**
     * ArrowBase est le polygone (fleche) à partir duquel on fait une arrow
     * 
     * @param v0 Le premier point (du cote de tuile)
     * @param v1 Le deuxième point
     * @param direction La direction dont le fleche va pointer (ou le numero de coté de la tuile)
     */
    public ArrowBase(Coord3D v0, Coord3D v1, int direction, int height) {
        super();
        this.height = height;
        initialise(v0, v1, direction);
    }

    public void initialise(Coord3D v0, Coord3D v1, int direction) {
        double distance = v0.distance(v1);
        double x1 = v1.getX();
        double z1 = v1.getZ();
        double x = (v0.getX() + v1.getX()) / 2;
        double z = (v0.getZ() + v1.getZ()) / 2;

        switch(direction) {
            case 0 :
                x1 += (distance/4);
                addPoint(new Coord3D(x1, -height, z1));
                
                z1+=distance/2;
                addPoint(new Coord3D(x1, -height, z1));

                x1-=distance/4;
                addPoint(new Coord3D(x1, -height, z1));

                z+=distance;
                addPoint(new Coord3D(x, -height, z));

                x1+=distance;
                addPoint(new Coord3D(x1, -height, z1));

                x1-=distance/4;
                addPoint(new Coord3D(x1, -height, z1));
                
                z1-=distance/2;
                addPoint(new Coord3D(x1, -height, z1));
                break;
            case 1 :
                z1 += (distance/4);
                addPoint(new Coord3D(x1, -height, z1));

                x1-=distance/2;
                addPoint(new Coord3D(x1, -height, z1));

                z1-=distance/4;
                addPoint(new Coord3D(x1, -height, z1));

                x-=distance;
                addPoint(new Coord3D(x, -height, z));

                z1+=distance;
                addPoint(new Coord3D(x1, -height, z1));

                z1-=distance/4;
                addPoint(new Coord3D(x1, -height, z1));
                
                x1+=distance/2;
                addPoint(new Coord3D(x1, -height, z1));
                break;
            case 2 :
                x1 -= (distance/4);
                addPoint(new Coord3D(x1, -height, z1));

                z1-=distance/2;
                addPoint(new Coord3D(x1, -height, z1));

                x1+=distance/4;
                addPoint(new Coord3D(x1, -height, z1));

                z-=distance;
                addPoint(new Coord3D(x, -height, z));

                x1-=distance;
                addPoint(new Coord3D(x1, -height, z1));

                x1+=distance/4;
                addPoint(new Coord3D(x1, -height, z1));
                
                z1+=distance/2;
                addPoint(new Coord3D(x1, -height, z1));
                break;
            case 3 :
                z1 -= (distance/4);
                addPoint(new Coord3D(x1, -height, z1));

                x1+=distance/2;
                addPoint(new Coord3D(x1, -height, z1));

                z1+=distance/4;
                addPoint(new Coord3D(x1, -height, z1));

                x+=distance;
                addPoint(new Coord3D(x, -height, z));

                z1-=distance;
                addPoint(new Coord3D(x1, -height, z1));

                z1+=distance/4;
                addPoint(new Coord3D(x1, -height, z1));
                
                x1-=distance/2;
                addPoint(new Coord3D(x1, -height, z1));
                break;
            default:
                break;
        }

    }

}
