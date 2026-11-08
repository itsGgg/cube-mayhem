package model.UI;

/**
 * Interface définissant le contrat pour tous les menus du jeu.
 * Chaque menu doit implémenter ces méthodes pour assurer une cohérence
 * dans la gestion des composants et des interactions.
 */
public interface IGameMenu {
    /**
     * Initialise tous les composants du menu (boutons, labels, etc.)
     */
    void initializeComponents();

    /**
     * Met à jour l'état visuel des composants du menu
     */
    void updateComponents();

    /**
     * Gère la navigation et les transitions entre les menus
     */
    void handleNavigation();

    /**
     * Applique le thème visuel au menu
     */
    void applyTheme();

    /**
     * Gère le redimensionnement des composants
     */
    void handleResize();
}