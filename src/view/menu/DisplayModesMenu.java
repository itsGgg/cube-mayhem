package view.menu;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import controller.MenuController;
import model.UI.BaseGameMenu;
import model.UI.GameButton;
import model.level.GameState;
import model.level.GameState.DisplayMode;
import model.tools.LoadSave;

/**
 * Menu de sélection du mode d'affichage (Terminal, 2D, 3D)
 */
public class DisplayModesMenu extends BaseGameMenu {
    private BufferedImage backgroundImg;
    private JPanel centerPanel;

    // Dimensions par défaut
    private static final int DEFAULT_BACKGROUND_WIDTH = 1600;
    private static final int DEFAULT_BACKGROUND_HEIGHT = 1000;

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private GameButton[] displayButtons;

    public DisplayModesMenu(MenuController menuController, GameState gameState) {
        super(menuController, gameState);
        backgroundImg = LoadSave.GetSprite("bckgMenu");
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
        if (displayButtons == null)
            return;

        for (GameButton button : getButtons()) {
            button.resize(scaleX, scaleY);
        }
    }

    @Override
    public void initializeComponents() {
        centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        initButtons();
        add(centerPanel, new GridBagConstraints());
    }

    private void initButtons() {
        // On exclut le mode MAIN_DISPLAY des options
        int nbButton = DisplayMode.values().length - 1;
        displayButtons = new GameButton[nbButton];

        for (int i = 0; i < nbButton; i++) {
            final DisplayMode mode = DisplayMode.values()[i];
            GameButton button = new GameButton(
                    "spriteDisplayMode",
                    3,
                    0,
                    0,
                    168,
                    88,
                    i,
                    mode);

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleDisplayModeChange(mode);
                }
            });

            displayButtons[i] = button;
            addButton(button);
            centerPanel.add(button);
        }
    }

    private void handleDisplayModeChange(DisplayMode mode) {
        gameState.setDisplayMode(mode);
        menuController.getWindow().update(menuController);
    }

    @Override
    public void handleNavigation() {
        // La navigation est gérée par les boutons via leurs états
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
}
