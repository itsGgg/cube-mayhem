package view.affichage3D;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;

import model.tools.Constants;
import view.Window;
import view.affichage3D.polygone.BgCube3D;

public class Background3D extends JPanel {

    private Random rand = new Random();

    private Point center;
    private Polygon[] bgPolygons = new Polygon[3];
    private BufferedImage staticLayer;
    public static boolean staticLayerNeedsUpdate = true;
    
    private ArrayList<BgCube3D> backgroundCubes;

    public Background3D() {
        super();

        setBounds(0, 0, (int)Window.width, (int)Window.height);

        initialiseCenter();
        generateStaticLayer();

        //backgroundCubes = new ArrayList<>();
        //initialiseBgCubes();
    }

    private void initialiseCenter() {
        center = new Point((int)Window.width/2, (int) (Window.height * 0.55));
    }

    private void initialiseBgCubes() {

        int maxSize = (int)Window.width / 20;
        int minSize = (int)Window.width / 40;
        int diff = (int)Window.width / 10;

        int size;
        int x0;
        int y0;
        int currentX;
        int currentZ = 1500;

        for(int i = 0; i < 3; i++) {
            currentX = 0;
            for(int j = 0; j <10; j++) {
                
                size = rand.nextInt(maxSize - minSize + 1) + minSize;
                x0 = currentX + rand.nextInt(maxSize - minSize + 1) + minSize;
                y0 = rand.nextInt((int)(Window.height*1.3)) - (int)(Window.height*0.15);
                
                backgroundCubes.add(BgCube3D.initialiseBgCube(x0, y0, currentZ, size));

                currentX += diff;
            }
            currentZ -= maxSize;
        }
    }

    public void resize(double scale, double previousWidth) {
        setBounds(0, 0, (int)Window.width, (int)Window.height);

        initialiseCenter();
        generateStaticLayer();
    
        //for(BgCube3D bgCube : backgroundCubes) bgCube.position(scale, previousWidth);
    }

    private void generateStaticLayer() { // MISE A JOUR A AJOUTER APRES CHANGEMENT DE LA TAILLE
        staticLayer = new BufferedImage((int)Window.width, (int)Window.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gStatic = staticLayer.createGraphics();

        int centerX = (int)center.getX();
        int centerY = (int)center.getY();
    
        int[] xPoints0 = {0, centerX, centerX, 0};
        int[] yPoints0 = {0, 0, centerY, (int)Window.height};
        bgPolygons[0] = new Polygon(xPoints0, yPoints0, 4);

        int[] xPoints1 = {centerX, (int)Window.width, (int)Window.width, centerX};
        int[] yPoints1 = {0, 0, (int)Window.height, centerY};
        bgPolygons[1] = new Polygon(xPoints1, yPoints1, 4);

        int[] xPoints2 = {0, centerX, (int)Window.width};
        int[] yPoints2 = {(int)Window.height, centerY, (int)Window.height};
        bgPolygons[2] = new Polygon(xPoints2, yPoints2, 3);
        
        for(int i = 0; i < 3; i++) {
            Polygon p = bgPolygons[i];
            Color color;
            if(i == 0) color = Constants.Colors3D.BG_GRAY3;
            else if(i == 1) color = Constants.Colors3D.BG_GRAY2;
            else color = Constants.Colors3D.BG_GRAY1;
            gStatic.setColor(color);
            gStatic.fillPolygon(p);
        }
    
        gStatic.dispose();
        staticLayerNeedsUpdate = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;
        //g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 

        //g2D.setComposite(AlphaComposite.Clear);
        
        g2D.drawImage(staticLayer, 0, 0, null);
        //for(BgCube3D cube : backgroundCubes) cube.renderBgCube3D(g2D, new PointOfVue(Window.width/2, -Window.height * 0.55, -5000)); // Pov a remplacer par celui du panel3D
    }

}
