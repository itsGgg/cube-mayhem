package view.affichage3D.polygone;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.Timer;
import java.util.ArrayList;

import model.level.Cube;
import model.level.plate.Case;
import model.level.tiles.TileType;
import model.tools.Direction;
import view.Window;
import view.affichage3D.Coord3D;
import view.affichage3D.Plate3D;
import view.affichage3D.PointOfVue;
import view.affichage3D.Transformation3D;

public class GameCube3D extends Cube3D {

        private Cube cube;
        private Direction direction;
        private Tile3D tile;
        private Timer animationTimer;
        private LinkedList<Tile3D[]> animationQueue = new LinkedList<>();
        private LinkedList<Direction> rotationDirectionQueue = new LinkedList<>();
        private boolean falling = false;
        private boolean fallingOutside = false;
        private boolean visible = false;
        private boolean isAnimating = false;

        /**
         * Le constructeur de Cube3D
         * 
         * @param tile La tuile sur laquelle on souhaite placer le cube au départ
         * @param cube Le cube du jeu
         */
        public GameCube3D(Tile3D tile, Cube cube) {
                super(tile.faces.get(1).copy());
                this.tile = tile;
                for(Polygon3D f : faces) f.setColor(Color.WHITE);
                this.cube = cube;
                direction = cube.getFacingDirection();
        }

        public Tile3D getTile3D() {
                return tile;
        }

        public Case getCase() {
                return cube.getCase();
        }

        public boolean getFalling() {
                return falling;
        }

        public boolean isVisible() {
                return visible;
        }

        public boolean isAnimationFinished() {
                return animationQueue.isEmpty() ;
        }

        /**
         * Affichage du cube en 3D
         * 
         * @param g2D
         * @param pov
         */
        public void renderGameCube3D(Graphics2D g2D, PointOfVue pov) {
                g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                adjustFacesNormal();
                for (Polygon3D face : faces) {
                        Polygon p = face.projection2D();
            
                        if (p != null && p.npoints > 0) {    
                            if(face.isFront(pov) && face.getColor()!=null) {
                                g2D.setColor(face.getColor());
                                g2D.fillPolygon(p);
                                g2D.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                                g2D.setColor(Color.BLACK);
                                g2D.drawPolygon(p);
                            }
                        } else { 
                            System.out.println("Face vide ou projection non valide");
                        } 
                }
        }

        /**
         * Indique si la position du cube a changé
         * 
         * @return True si on a déplacé le cube
         */
        public boolean positionChanged() {
                return (tile.getCase() != cube.getCase() || (tile.getTile()!=null && tile.getTile().isType(TileType.SPIN) && direction!=cube.getFacingDirection()));
        }

        /**
         * Mise a jour de la position du cube
         * 
         * @param newTile La tuile sur laquelle se situe le cube
         */
        public void updatePosition(Tile3D newTile) {
                System.out.println("in");
                if(!visible) {
                        visible = true;
                        animationQueue.add(new Tile3D[] {tile, tile});
                }
                if(animationQueue.size() == 1) {
                        adaptMove();
                }
        
                animationQueue.add(new Tile3D[] {tile, newTile});
                System.out.println(tile.getCase().getI() + ", "+ tile.getCase().getJ() + "-->" + newTile.getCase().getI() + ", "+  newTile.getCase().getJ());

                if(tile.getTile() == null || (tile.getTile() != null && ((!tile.getTile().isType(TileType.TELEPORTATION) && 
                        !tile.getTile().isType(TileType.END) && !tile.getTile().isType(TileType.SLIDE)) || 
                        (tile.getTile().isType(TileType.TELEPORTATION) && (newTile.getTile() == null || !newTile.getTile().isType(TileType.TELEPORTATION)))))) {
                        rotationDirectionQueue.add(direction);
                }

                if(newTile.getTile()!=null && newTile.getTile().isType(TileType.SPIN)) {
                        animationQueue.add(new Tile3D[] {newTile, newTile});
                }

                if(tile != newTile) {
                        tile = newTile;
                } else throw new IllegalStateException("pb avec deplacement en 3D, mauvaises tuiles indiquées");
                
                //if(direction != cube.getFacingDirection()) System.out.println(cube.getFacingDirection());
                direction = cube.getFacingDirection();
                System.out.println("out");
        }

        /**
         * Adapte le type d'animation au déplacement du cube
         */
        public void adaptMove() {
                if(!isAnimating) {
                        if(!animationQueue.isEmpty()) {
                                Tile3D peek0 = animationQueue.peek()[0];
                                Tile3D peek1 = animationQueue.peek()[1];
        
                                if(peek0.getTile()!=null && peek0.getTile().isType(TileType.SLIDE)) {
                                        if(!peek1.isStart()) {
                                                slideAnimation(animationQueue.peek());
                                        } else slideOff(peek0);
                                } 
                                else if (peek1.getTile()==peek0.getTile() && peek0.getTile()!=null && peek0.getTile().isType(TileType.SPIN)) {
                                        spinAnimation(peek0);
                                }
                                else if (peek0.getTile()!=null && peek0.getTile().isType(TileType.TELEPORTATION) && peek1.getTile()!=null && peek1.getTile().isType(TileType.TELEPORTATION)) {
                                        teleportationAnimation(animationQueue.peek());
                                }
                                else if (peek0.getTile()!=null && peek1.getTile() != null && peek0.getTile().isType(TileType.START) && peek1.getTile().isType(TileType.START)){
                                        startEnd(true);
                                }
                                else if (!rotationDirectionQueue.isEmpty()) {
                                        if(!peek1.isStart()) {
                                                rotationAnimation(peek1, rotationDirectionQueue.peek());
                                        } else rollOff(peek0);      
                                }
                        } else {
                                startEnd(false);
                        }
                }
        }

        /************ ROTATION (LE DÉPLACEMENT DE BASE) *****************/

        /**
         * Definit la transformation3D pour la rotation du cube
         * 
         * @param angle l'angle de la rotation
         * @param rotationPoints les points qui définissent l'axe de la rotation
         * @param reverse true is direction du cube est WEST, faut inverser l'angle de rotation
         */
        public void rotation(double angle, Coord3D[] rotationPoints, boolean reverse) {
                Transformation3D rot;
                if(reverse) {
                        rot = Transformation3D.personalizedRotation(-angle, rotationPoints[0].copy(), rotationPoints[1].copy());
                } else {
                        rot = Transformation3D.personalizedRotation(angle, rotationPoints[0].copy(), rotationPoints[1].copy());
                }
                applyTransformations(rot);
        }

        public void rotateAndFall(double angle, Coord3D[] rotationPoints, boolean reverse, Coord3D fall) {
                rotation(angle, rotationPoints, reverse);
                Transformation3D t = Transformation3D.translation(fall);
                applyTransformations(t);
                t.apply(rotationPoints[0]);
                t.apply(rotationPoints[1]);
        }

        /**
         * Permet de définir l'axe autour duquel le cube va faire une rotation
         * 
         * @param newTile La tuile vers laquelle le cube de déplace
         * @return Une paire de points qui permettent de définir l'axe de rotation
         */
        public Coord3D[] getRotationPoints(Tile3D newTile) {
                int i = 0;
                Coord3D []rotationPoints = new Coord3D[2];
                for(Coord3D tilePoint : newTile.faces.get(1).getPoints()) {
                        for(Coord3D cubePoint : points) {
                                if(cubePoint.equals(tilePoint) && i<2) {
                                        rotationPoints[i] = cubePoint;
                                        i++;
                                } else if(cubePoint.equals(tilePoint)) {
                                        throw new IllegalStateException("Il y a plus que deux points communs entre la prochaine tuile et le cube");
                                }
                        }
                }
                if(i == 0) {
                        throw new IllegalStateException("getRotationPoints avec coord3D nulls, newTile null ? " 
                        + (newTile==null) + ", tile null? " + (tile==null));
                }
                return rotationPoints;
        }

        /**
         * Gère les animations de déplacements 
         * 
         * @param newTile La tuile3D vers laquelle le cube se déplace (la prochaine tuile)
         * @param direction La direction du cube
         */
        public void rotationAnimation(Tile3D newTile, Direction direction) {
                isAnimating = true;
                int steps = 30;
                Coord3D[] rotationPoints = getRotationPoints(newTile);

                double angle = -Math.PI /(steps*2);

                final boolean[] reverse = new boolean[1];
                if(direction == Direction.WEST) {
                        reverse[0] = true;
                } else {
                        reverse[0] = false;
                }
                

                animationTimer = new Timer(15, new ActionListener() {
                        int currentStep = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                MouseInfo.getPointerInfo();
                                
                                rotation(angle, rotationPoints, reverse[0]);
                                currentStep++;
                                if(currentStep == steps) {
                                        animationTimer.stop();
                                        isAnimating = false;
                                        animationQueue.poll();
                                        rotationDirectionQueue.poll();

                                        if(newTile.getTile() != null && newTile.getTile().isType(TileType.SWITCH_OFF)) {
                                                if(newTile.hasButton()) {
                                                        System.out.println("btn");
                                                        newTile.getSwitchButton3D().setOn(true);
                                                        Plate3D.isSwitchButtonOn = true;
                                                        Plate3D.staticLayerNeedsUpdate = true;
                                                } else throw new IllegalStateException("button de la tuile est null");
                                        }
                                        
                                        adaptMove();
                                }
                        }
                });
                animationTimer.start();
        }

        /**
         * L'animation si le cube depasse le plateau en roulant (pas avec slide par exemple)
         * @param currentTile la position actuelle du cube
         */
        public void rollOff(Tile3D currentTile) {
                isAnimating = true;
                int steps = 60;
                Coord3D[] rotationPoints = currentTile.getEdgePoints(direction);

                Coord3D sideVect = currentTile.getSideVector(direction);
                Transformation3D side = Transformation3D.translation(sideVect);

                Coord3D[] fallRotationPoints = new Coord3D[2];
                fallRotationPoints[0] = rotationPoints[0].copy();
                fallRotationPoints[1] = rotationPoints[1].copy();

                //On déplace l'axe de rotation a l'exterieur de la tuile courante
                side.apply(fallRotationPoints[0]);                
                side.apply(fallRotationPoints[1]);

                Coord3D v2 = movedCenter(sideVect); // le "fall" tout entier, pas divisé pour les animations
                Coord3D fall = v2.substract(new Coord3D(v2.getX(), Window.height, v2.getZ())).devide(-steps/3);

                double angle = -Math.PI /(steps);

                final boolean[] reverse = new boolean[1];
                if(direction == Direction.WEST) {
                        reverse[0] = true;
                } else {
                        reverse[0] = false;
                }

                animationTimer = new Timer(15, new ActionListener() {
                        int currentStep = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                MouseInfo.getPointerInfo();

                                if(currentStep < 20)rotation(angle, rotationPoints, reverse[0]);
                                else {
                                        if(currentStep == 20) falling = true;
                                        rotateAndFall(angle*2, fallRotationPoints, reverse[0], fall);
                                }
                                currentStep++;
                                if(currentStep == steps) {
                                        animationTimer.stop();
                                        isAnimating = false;
                                        animationQueue.poll();
                                        rotationDirectionQueue.poll();
                                        adaptMove();
                                }
                        }
                });
                animationTimer.start();
        }

        /************ SLIDE *****************/

         /**
         * Déplacement du cube3D
         * 
         * @param v Le déplacement du Cube3D
         */
        public void slide(Coord3D v) {
                Transformation3D t = Transformation3D.translation(v);
                applyTransformations(t);
        }

        public Coord3D movedCenter(Coord3D v) {
                Coord3D centerCopy = center.copy();
                Transformation3D slideOff = Transformation3D.translation(v);
                slideOff.apply(centerCopy);
                return centerCopy;
        }

        /**
         * Animation de "slide"
         * 
         * @param tiles Tableau de tuiles3D avec la tuile actuelle et la suivante
         */
        public void slideAnimation(Tile3D[] tiles) {
                isAnimating = true;
                int steps = 30;

                Coord3D v = tiles[1].center.substract(tiles[0].center);
                Coord3D v2 = new Coord3D(v.getX()/steps, v.getY()/steps, v.getZ()/steps);

                animationTimer = new Timer(15, new ActionListener() {
                        
                        int currentStep = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                MouseInfo.getPointerInfo();
                                
                                slide(v2); 
                                currentStep++;
                                if(currentStep == steps) {
                                        animationTimer.stop();
                                        isAnimating = false;
                                        animationQueue.poll();

                                        adaptMove();
                                }
                        }
                });
                animationTimer.start();
        }

        /**
         * Animation qu'on utilise si le cube "slides off", alors quand la tuile slide 
         * l'emmene à l'éxterieur du plateau
         * @param tile0
         */
        public void slideOff(Tile3D tile0) {
                isAnimating = true;
                int steps = 60;

                Coord3D v = tile0.getSideVector();
                Coord3D slide = v.devide(steps / 2);

                Coord3D v2 = movedCenter(v);
                Coord3D fall = v2.substract(new Coord3D(v2.getX(), Window.height, v2.getZ())).devide(-steps/3);

                animationTimer = new Timer(15, new ActionListener() {
                        
                        int currentStep = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                MouseInfo.getPointerInfo();

                                if(currentStep < 30) slide(slide);
                                else {
                                        if(currentStep == 30) falling = true;
                                        slide(fall);
                                }
                                currentStep++;
                                if(currentStep == steps) {
                                        animationTimer.stop();
                                        isAnimating = false;
                                        animationQueue.poll();

                                        adaptMove();
                                }
                        }
                });
                animationTimer.start();
        }

        /************ TELEPROTATION *****************/

        /**
         * Pour changer la taille du cube
         * @param scale
         */
        public void resize(double scale) {
                Coord3D cenCopy = center.copy();
                Transformation3D toCenter = Transformation3D.translation(-cenCopy.getX(), -cenCopy.getY(), -cenCopy.getZ());
                Transformation3D resize = Transformation3D.scale(scale, scale, scale);
                Transformation3D back = Transformation3D.translation(cenCopy.getX(), cenCopy.getY(), cenCopy.getZ());
                applyTransformations(toCenter, resize, back);
        }

        /**
         * Sert a calculer les vecteurs pour l'animation de teleportation
         * @param tiles La tuile actuelle et suivante
         * @param avStep nobre de petits déplacements par un grand (on devise les 
         * vecteurs par avStep pour avoir une animation fluide)
         * @return Tableau avec des vecteurs pour animer teleportation
         */
        public Coord3D[] teleportatonVectors(Tile3D[] tiles, int avStep) {

                Coord3D[] vectors = new Coord3D[5];

                Coord3D v = center.substract(tiles[0].center);

                vectors[0] = new Coord3D(v.getX()/avStep, v.getY()/avStep, v.getZ()/avStep);
                vectors[1] =  vectors[0].multiply(-1);

                Coord3D diff = tiles[1].center.substract(tiles[0].center);
                vectors[2] = diff;

                Transformation3D t = Transformation3D.translation(diff);
                Coord3D centerCopy = center.copy();
                t.apply(centerCopy);

                Coord3D v2 = centerCopy.substract(tiles[1].center);
                vectors[3] = new Coord3D(v2.getX()/avStep, v2.getY()/avStep, v2.getZ()/avStep);
                vectors[4] = vectors[3].devide(-1);

                return vectors;
        } 

        /**
         * Animation de teleportation
         * @param tiles La tuile courante et suivante (qui sont sous forme d'un tableau dans animationQueue)
         */
        public void teleportationAnimation(Tile3D[] tiles) {
                isAnimating = true;
                int steps = 51;
                
                Coord3D[] vectors = teleportatonVectors(tiles, 5);

                animationTimer = new Timer(15, new ActionListener() {
                        int currentStep = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                MouseInfo.getPointerInfo();
                                if(currentStep < 5) {
                                        slide(vectors[0]);
                                }else if(currentStep < 15) {
                                        resize(0.85);
                                }else if(currentStep < 25) {
                                        slide(vectors[1]);
                                } else if(currentStep == 25) {
                                        slide(vectors[2]);
                                } else if(currentStep < 36) {
                                        slide(vectors[3]);
                                } else if(currentStep < 46) {
                                        resize(1/0.85);
                                } else {
                                        slide(vectors[4]);
                                }
                                currentStep++;

                                if(currentStep == steps) {
                                        animationTimer.stop();
                                        isAnimating = false;
                                        animationQueue.poll();
                                        adaptMove();
                                }
                        }
                });
                animationTimer.start();
        }

        /************ SPIN *****************/

        /**
         * Animation pour le cube qui se tourne (la tuile verte)
         * @param currentTile La tuile actuelle
         */
        public void spinAnimation(Tile3D currentTile) {
                isAnimating = true;
                int steps = 30;

                System.out.println("spin");

                Coord3D[] rotationPoints = {center, currentTile.center};
                double angle = Math.PI /(steps*2);

                animationTimer = new Timer(15, new ActionListener() {
                        int currentStep = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                MouseInfo.getPointerInfo();
                                
                                rotation(angle, rotationPoints, false);
                                currentStep++;
                                if(currentStep == steps) {
                                        animationTimer.stop();
                                        isAnimating = false;
                                        animationQueue.poll();
                                        
                                        adaptMove();
                                }
                        }
                });
                animationTimer.start();
        }

        /************ DEBUT / FIN *****************/

        /**
         * L'animation tout au début / à la fin du jeu (quand le cube apparaît / dissparaît)
         * @param start True si on vient de lancer le jeu, False si c'est la dernière animation
         */
        public void startEnd(boolean start) {
                isAnimating = true;
                int steps = 35;

                if(start) resize(1/Math.pow(1.2, 30));
                Coord3D[] rotationPoints = {center, tile.center};
                double startScale = Math.pow(10, 1.0/5);
                double angle = Math.PI / 5;

                animationTimer = new Timer(15, new ActionListener() {
                        int currentStep = 0;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                MouseInfo.getPointerInfo();
                                
                                if(start && currentStep < 30) resize(1.2);
                                else if(!start && currentStep > 4) {
                                        resize(0.9);
                                        rotation(angle, rotationPoints, false);
                                }
                                currentStep++;

                                if(currentStep == steps) {
                                        if(!start) visible = false;
                                        animationTimer.stop();
                                        isAnimating = false;
                                        if(start) animationQueue.poll();
                                        adaptMove();
                                }
                        }
                });
                animationTimer.start();
        }

}

