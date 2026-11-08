package model.UI;

import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

import controller.MenuController;
import model.level.GameState;
import view.menu.ControlButtons;

/**
 * Classe de base pour tous les menus du jeu.
 * Fournit une implémentation commune des fonctionnalités de base.
 */
public abstract class BaseGameMenu extends JPanel implements IGameMenu {
    protected MenuController menuController;
    protected GameState gameState;
    protected List<GameButton> buttons;
    protected ControlButtons controlButtons;

    /**
     * Constructeur de base pour tous les menus
     * 
     * @param menuController Le contrôleur de menu
     * @param gameState      L'état du jeu
     */
    public BaseGameMenu(MenuController menuController, GameState gameState) {
        this.menuController = menuController;
        this.gameState = gameState;
        this.buttons = new ArrayList<>();
        setLayout(new GridBagLayout());
    }

    /**
     * Initialise le menu. Cette méthode doit être appelée après la construction.
     */
    public void initialize() {
        if (menuController != null && gameState != null) {
            initializeComponents();
            if (controlButtons != null) {
                controlButtons.repositionButtons();
            }
        }
    }

    /**
     * Initialise les boutons de contrôle
     */
    protected void initializeControlButtons() {
        if (menuController != null) {
            controlButtons = new ControlButtons(menuController, this);
        }
    }

    /**
     * Met à jour les composants du menu
     */
    @Override
    public void updateComponents() {
        for (GameButton button : buttons) {
            button.update();
            button.animate();
        }
        if (controlButtons != null) {
            controlButtons.repositionButtons();
        }
    }

    /**
     * Méthode de dessin commune à tous les menus
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        updateComponents();
    }

    /**
     * Ajoute un bouton à la liste des boutons du menu
     * 
     * @param button Le bouton à ajouter
     */
    protected void addButton(GameButton button) {
        if (button != null) {
            buttons.add(button);
        }
    }

    /**
     * Nettoie les ressources du menu
     */
    public void cleanup() {
        buttons.clear();
        if (controlButtons != null) {
            controlButtons.cleanup();
        }
        removeAll();
    }

    /**
     * Retourne la liste des boutons du menu
     */
    public List<GameButton> getButtons() {
        return buttons;
    }

    /**
     * Retourne le contrôleur de menu
     */
    public MenuController getMenuController() {
        return menuController;
    }

    /**
     * Retourne l'état du jeu
     */
    public GameState getGameState() {
        return gameState;
    }
}