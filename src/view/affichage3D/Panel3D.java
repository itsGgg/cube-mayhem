package view.affichage3D;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import controller.MenuController;
import controller.listeners3D.MouseListener3D;
import model.level.Level;
import view.Window;
import view.affichage3D.polygone.GameCube3D;
import view.menu.ControlButtons;

public class Panel3D extends JLayeredPane {

    private Background3D background;
    private GamePanel3D gamePanel3D;
    private Inventaire3D inventaire;
    private JPanel controlButtonsPanel;
    private ControlButtons controlButtons;

    private boolean isEditorMode;
    private static Level currentLevel;
    private static MenuController menuController;
    PointOfVue pov = new PointOfVue(Window.width / 2, -Window.height * 0.55, -5000);

    private static Transformation3D initialTransformatrions[] = {
            Transformation3D.rotationY(-Math.PI / 4),
            Transformation3D.rotationX(Math.PI / 6),
            Transformation3D.translation(Window.width / 2, Window.height * 0.55, 0)
    };

    public Panel3D(MenuController menuController, boolean isEditorMode) {
        super();
        this.currentLevel = menuController.getCurrentLevel();
        Panel3D.menuController = menuController;
        this.isEditorMode = isEditorMode;

        setLayout(null);
        setBounds(0, 0, (int) Window.width, (int) Window.height);

        background = new Background3D();
        gamePanel3D = new GamePanel3D(isEditorMode);
        inventaire = new Inventaire3D(currentLevel.getPlate().getTilesAvailable(), menuController, gamePanel3D);

        // Création du panneau de contrôle
        controlButtonsPanel = new JPanel();
        controlButtonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controlButtonsPanel.setBounds(0, 0, (int) Window.width, (int) Window.height);
        controlButtonsPanel.setOpaque(false);

        add(background, JLayeredPane.DEFAULT_LAYER);
        background.setVisible(true);

        // Initialisation des boutons de contrôle avec le bon mode
        controlButtons = new ControlButtons(menuController, controlButtonsPanel);
        //controlButtons.addLevelControlButtons();

        // Ajout des panneaux au JLayeredPane
        add(gamePanel3D, JLayeredPane.PALETTE_LAYER);
        gamePanel3D.setVisible(true);

        add(inventaire, JLayeredPane.POPUP_LAYER);
        inventaire.setVisible(true);

        add(controlButtonsPanel, JLayeredPane.DRAG_LAYER);
        controlButtonsPanel.setVisible(true);

        setDoubleBuffered(true);
    }

    public static Level getCurrentLevel() {
        return currentLevel;
    }

    public GamePanel3D getGamePanel3D() {
        return gamePanel3D;
    }

    public static Transformation3D[] getInitialTransformations() {
        return initialTransformatrions;
    }

    public static MenuController getMenuController() {
        return menuController;
    }

    public void resize(double previousWidth, double previousHeight) {
        double scale;
        if (Window.width * 5 / 8 > Window.height) {
            scale = Window.height / previousHeight;
        } else {
            scale = Window.width / previousWidth;
        }
        background.resize(scale, previousWidth);
        gamePanel3D.resizeGamePanel(scale);
        inventaire.position();

        // Redimensionner le panneau de contrôle pour qu'il occupe toute la fenêtre
        controlButtonsPanel.setBounds(0, 0, (int) Window.width, (int) Window.height);

        // Repositionner les boutons
        if (controlButtons != null) {
            controlButtons.repositionButtons();
        }

        revalidate();
        repaint();
    }

    /**
     * Mise à jour du niveau/son affichage en 3D
     * 
     * @param level Niveau Actuel
     */
    public void update(MenuController menuController) {
        Level level = menuController.getCurrentLevel();
        if (currentLevel != level) {
            currentLevel = level;
            gamePanel3D.restart();
            inventaire.restart(currentLevel.getPlate().getTilesAvailable(), gamePanel3D);
        } else {
            gamePanel3D.update();
            inventaire.update();
        }
        // System.out.println("Panel:" +getWidth() + "x" + getHeight());
    }

    /**
     * Redemmarade de niveau
     * 
     * @param level Niveau Actuel
     */
    public void restart(Level level) {
        gamePanel3D.restart();
        inventaire.restart(level.getPlate().getTilesAvailable(), gamePanel3D);
        repaint();
    }

    /** C'est le panel qui affiche le plateau du jeu en 3D */
    public class GamePanel3D extends JPanel {
        
        Plate3D plate;
        PointOfVue pov = new PointOfVue(0, 0, -5000);
        private MouseListener3D mmListener;

        // public GamePanel3D() {

        // resizeGamePanel(1);
        // plate = new Plate3D(currentLevel.getPlate());

        // setOpaque(false);
        // setVisible(true);
        // setFocusable(true);
        // requestFocusInWindow();

        // positionPlate();

        // repaint();
        // addMouseListener3D();
        // Plate3D.staticLayerNeedsUpdate = true;
        // }

        public GamePanel3D(boolean isEditorMode) {

            resizeGamePanel(1);
            plate = new Plate3D(currentLevel.getPlate(), isEditorMode);

            setOpaque(false);
            // setBackground(Color.GRAY);
            setVisible(true);
            setFocusable(true);
            requestFocusInWindow();

            positionPlate();

            repaint();

            addMouseListener3D();
            Plate3D.staticLayerNeedsUpdate = true;
        }

        public void addMouseListener3D() {
            mmListener = new MouseListener3D(plate);
            addMouseMotionListener(mmListener);
            addMouseListener(mmListener);
        }

        public void removeMouseListener3D() {
            removeMouseMotionListener(mmListener);
            removeMouseListener(mmListener);
            mmListener = null;
        }

        /**
         * Met à jour la taille du GamePanel3D
         */
        public void resizeGamePanel(double scale) {
            setBounds(0, 0, (int) Window.width, (int) Window.height);
            if (plate != null) {
                if (scale != 1)
                    plate.resize(scale);
                plate.move();
            }
        }

        /**
         * Mise à jour du niveau/son affichage en 3D
         * 
         * @param level Niveau Actuel
         */
        public void update() {
            if (plate != null) {
                plate.update();
            } else
                throw new NullPointerException("Le plateau en 3D est null");
            // System.out.println("GAmePanel:" +getWidth() + "x" + getHeight());
            revalidate();
            repaint();
        }

        /**
         * Redemmarade de niveau
         * 
         * @param level Niveau Actuel
         */
        public void restart() {
            plate = null;
            plate = new Plate3D(currentLevel.getPlate(), false);
            positionPlate();
            removeMouseListener3D();
            addMouseListener3D();
            revalidate();
            repaint();
        }

        /**
         * Pour bien placer le plateau au debut du chaque niveau
         */
        public void positionPlate() {
            plate.applyInitialTransformations();
            repaint();
        }

        @Override
        public void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //g2D.setComposite(AlphaComposite.Clear);
            //g2D.setComposite(AlphaComposite.SrcOver);

            if (plate != null)
                plate.renderPlate3D(g2D, pov);
        }

        public Plate3D getPlate() {
            return plate;
        }
    }

    public GameCube3D getGameCube3D() {
        return gamePanel3D.plate.getGameCube3D();
    }

}
