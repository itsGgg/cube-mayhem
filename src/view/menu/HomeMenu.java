package view.menu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import controller.MenuController;
import model.UI.BaseGameMenu;
import model.UI.GameButton;
import model.level.GameState;
import model.level.GameState.DisplayMode;
import model.level.GameState.GameMode;
import model.tools.Constants;
import model.tools.LoadSave;
import view.Window;

/**
 * Menu principal du jeu avec les options comme new game, extras etc.
 */
public class HomeMenu extends BaseGameMenu {

    private MenuController menuController;
    private GameState gameState;
    private JPanel centerPanel;
    private ControlButtons controlButtons;
    private BufferedImage backgroundImg;

    // Dimensions par défaut
    private static final int DEFAULT_BACKGROUND_WIDTH = 1600;
    private static final int DEFAULT_BACKGROUND_HEIGHT = 1000;

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    private GameButton[] buttons; // Tableau des boutons du menu

    public HomeMenu(MenuController menuController, GameState gameState) {
        super(menuController, gameState);
        backgroundImg = LoadSave.GetSprite("bckgHomeMenu");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateScaling();
                repositionButtons();
                revalidate();
                repaint();
            }
        });
    }

    private void updateScaling() {
        if (backgroundImg != null) {
            scaleX = (float) getWidth() / DEFAULT_BACKGROUND_WIDTH;
            scaleY = (float) getHeight() / DEFAULT_BACKGROUND_HEIGHT;
        }
    }

    private void repositionButtons() {
        if (buttons == null)
            return;

        for (GameButton button : getButtons()) {
            button.resize(scaleX, scaleY);
        }
    }

    @Override
    public void initializeComponents() {
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        initButtons();
        add(centerPanel, new GridBagConstraints());

        // Initialisation des boutons de contrôle après les autres composants
        initializeControlButtons();
    }

    private void initButtons() {
        // Configuration des boutons du menu principal
        Object[][] buttonConfigs = {
                { "LOAD", "spriteMenu", 88, 0, GameMode.SAVE_LOAD },
                { "NEW GAME", "newGameButton", 132, 0, GameMode.NEW_GAME },
                // { "LEVELS", GameMode.LEVEL_SELECTION, 0 },
                { "EXTRAS", "spriteMenu", 88, 1, GameMode.EXTRAS },
                { "QUIT", "spriteMenu", 88, 2, GameMode.QUIT }
        };

        buttons = new GameButton[buttonConfigs.length];
        int i = 0;

        for (Object[] config : buttonConfigs) {
            GameButton button = new GameButton(
                    (String) config[1], // Utiliser le sprite correct pour le menu principal
                    3, // Nombre d'états du bouton
                    0, // Position X initiale
                    0, // Position Y initiale
                    168, // Largeur
                    (int) config[2], // Hauteur
                    (int) config[3], // Index de ligne
                    config[4] // État du jeu associé
            );

            buttons[i++] = button;
            addButton(button);
            centerPanel.add(button);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImg != null) {
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform originalTransform = g2d.getTransform();
            g2d.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);
            g2d.setTransform(originalTransform);
        }

        // Utilisation de la liste de boutons héritée
        for (GameButton button : getButtons()) {
            button.update();
            button.animate();
        }
    }

    @Override
    public void handleNavigation() {
        // La navigation est gérée par les gestionnaires d'événements des boutons
    }

    @Override
    public void applyTheme() {
        setOpaque(false);
    }

    @Override
    public void handleResize() {
        updateScaling();
        repositionButtons();
        if (controlButtons != null) {
            controlButtons.repositionButtons();
        }
        revalidate();
        repaint();
    }
}
