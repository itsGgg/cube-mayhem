package view.menu;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JPanel;

import controller.MenuController;
import model.UI.BaseGameMenu;
import model.UI.GameButton;
import model.level.GameState;
import model.level.GameState.GameMode;
import model.tools.LoadSave;

/**
 * Menu des fonctionnalités supplémentaires (éditeur, crédits)
 */
public class ExtrasMenu extends BaseGameMenu {
    private JPanel centerPanel;
    private BufferedImage backgroundImg;

    // Dimensions par défaut
    private static final int DEFAULT_BACKGROUND_WIDTH = 1600;
    private static final int DEFAULT_BACKGROUND_HEIGHT = 1000;

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private GameButton[] menuButtons;

    public ExtrasMenu(MenuController menuController, GameState gameState) {
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
        if (menuButtons == null)
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

        // Ajout des boutons de contrôle
        controlButtons = new ControlButtons(menuController, this);
    }

    private void initButtons() {
        GameMode[] buttonStates = {
                GameMode.SPEED_RUN,
                GameMode.LEVEL_EDITOR,
                GameMode.CREDITS,
                GameMode.MAIN_SCREEN
        };

        menuButtons = new GameButton[buttonStates.length];

        for (int i = 0; i < buttonStates.length; i++) {
            GameButton button = new GameButton(
                    "extrasMenu",
                    3,
                    0,
                    0,
                    168,
                    88,
                    i,
                    buttonStates[i]);

            menuButtons[i] = button;
            addButton(button);
            centerPanel.add(button);
            if (i < buttonStates.length - 1) {
                centerPanel.add(Box.createVerticalStrut(10));
            }
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

        // Mise à jour des boutons
        for (GameButton button : getButtons()) {
            button.update();
            button.animate();
        }
    }

    @Override
    public void handleNavigation() {
        // La navigation est gérée par les boutons
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

    @Override
    public void updateComponents() {
        super.updateComponents();
        if (controlButtons != null) {
            controlButtons.repositionButtons();
        }
    }
}