package view.affichage3D.polygone;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.Timer;

import model.tools.Constants;
import view.Window;
import view.affichage3D.Coord3D;
import view.affichage3D.PointOfVue;
import view.affichage3D.Transformation3D;

public class BgCube3D extends Cube3D {

    private Timer animationTimer;
    private Coord3D animationVector;
    private Random rand = new Random();

    public BgCube3D(Polygon3D polygon) {
        super(polygon);
    }

    public static BgCube3D initialiseBgCube(int x0, int y0, int z0, int size) {
        Coord3D p1 = new Coord3D(x0, y0, z0);
        Coord3D p2 = new Coord3D(x0+size, y0, z0);
        Coord3D p3 = new Coord3D(x0+size, y0, z0+size);
        Coord3D p4 = new Coord3D(x0, y0, z0+size);
        BgCube3D bgCube = new BgCube3D(new Polygon3D(new ArrayList<>(Arrays.asList(p1, p2, p3, p4)), Color.BLUE));
        bgCube.setColor();
        bgCube.initialRotation();
        bgCube.adjustFacesNormal();
        bgCube.adjustAnimationVector();
        return bgCube;
    }

    public void setColor() {
        for(int i = 0; i < 6; i++) {
            Color color;
            if(i==1) color = Constants.Colors3D.BG_CUBE_GRAY3;
            else if(i==2) color = Constants.Colors3D.BG_CUBE_GRAY2;
            else color = Constants.Colors3D.BG_CUBE_GRAY1;
            faces.get(i).setColor(color);
        }
    }

    public void position(double scale, double previousWidth) {
        resize(scale);
        if(Window.width != previousWidth) move(previousWidth);
    } 

    public void resize(double scale) {
        Coord3D cenCopy = center.copy();
        Transformation3D toCenter = Transformation3D.translation(-cenCopy.getX(), -cenCopy.getY(), -cenCopy.getZ());
        Transformation3D resize = Transformation3D.scale(scale, scale, scale);
        Transformation3D back = Transformation3D.translation(cenCopy.getX(), cenCopy.getY(), cenCopy.getZ());
        applyTransformations(toCenter, resize, back);
    }

    public void move(double previousWidth) { //a corriger, va mal reagir a changements de la hauteur et tout
        Coord3D v = new Coord3D((Window.width - previousWidth) /2, 0, 0);
        Transformation3D trans = Transformation3D.translation(v);
        applyTransformations(trans);
    }

    public void initialRotation() {
        Transformation3D rotY = Transformation3D.personalizedRotation(Math.PI/4, rotationPoitsY());
        Transformation3D rotX = Transformation3D.personalizedRotation(-Math.PI/6, rotationPoitsX());
        applyTransformations(rotY, rotX);
    }

    public Coord3D[] rotationPoitsY() {
        Coord3D[] rotationPointsY = new Coord3D[2];
        rotationPointsY[0] = new Coord3D(center.getX(), center.getY() - height, center.getZ());
        rotationPointsY[1] = new Coord3D(center.getX(), center.getY() + height, center.getZ());
        return rotationPointsY;
    }

    public Coord3D[] rotationPoitsX() {
        Coord3D[] rotationPointsX = new Coord3D[2];
        rotationPointsX[0] = new Coord3D(center.getX() - height, center.getY(), center.getZ());
        rotationPointsX[1] = new Coord3D(center.getX() + height, center.getY(), center.getZ());
        return rotationPointsX;
    }

    public void animation() {
        Transformation3D t = Transformation3D.translation(animationVector);

        animationTimer = new Timer(15, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                        MouseInfo.getPointerInfo();
                        
                        applyTransformations(t);

                        if(!isInFrame()) adjustAnimationVector();
                }
        });
        animationTimer.start();
    }

    private void adjustAnimationVector() {
        if(animationVector == null) {
            int direction = rand.nextInt(2);
            double speed = 0.6 + (2.7 - 0.6) * rand.nextDouble();
            if(direction == 1) speed = -speed;
            animationVector = new Coord3D(0, speed, 0);
        } else {
            animationTimer.stop();
            animationVector.setY(-animationVector.getY());;
        }
        animation();
    }

    private boolean isInFrame() {
        if(center.getY() < -Window.height * 0.15 || center.getY() > Window.height * 1.15) return false; // a optimiser
        else return true;
    }

    public void renderBgCube3D(Graphics2D g2D, PointOfVue pov) {
                for (Polygon3D face : faces) {
                        Polygon p = face.projection2D();
            
                        if (p != null && p.npoints > 0) {    
                            if(face.isFront(pov) && face.getColor()!=null) {
                                g2D.setColor(face.getColor());
                                g2D.fillPolygon(p);
                                //g2D.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                                g2D.setColor(Constants.Colors3D.BG_CUBE_OUTLINE);
                                g2D.drawPolygon(p);
                            }
                        } else { 
                            System.out.println("Face vide ou projection non valide");
                        } 
                }
        }
  
    
    
}
