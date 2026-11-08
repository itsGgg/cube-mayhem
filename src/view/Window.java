package view;

import java.awt.CardLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import controller.MenuController;
import model.level.Level;
import model.level.GameState;
import model.level.GameState.DisplayMode;
import model.tools.Constants;
import view.affichage2D.Panel2D;
import view.affichage3D.Panel3D;
import view.menu.MenuView;
import view.menu.WinMessagePanel;

/** C'est une classe pour tester les éléments d'affichage 3D */
public class Window extends JFrame {

    private boolean isEditorMode;
    private CardLayout cardLayout;
    private JLayeredPane layeredPane;
    private JPanel mainPanel;

    private MenuView menuView;
    private Panel2D panel2D;
    private Panel3D panel3D;
    private WinMessagePanel winMessagePanel;
    private MenuController menuController;

    public static double width;

    public static double height;

    public Window(MenuController menuController) {
        super();
        this.menuController = menuController;
        setTitle(Constants.Window.TITLE);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.Window.DEFAULT_WIDTH, Constants.Window.DEFAULT_HEIGHT);
        width = Constants.Window.DEFAULT_WIDTH;
        height = Constants.Window.DEFAULT_HEIGHT;

        cardLayout = new CardLayout();

        mainPanel = new JPanel(cardLayout);
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        getContentPane().add(layeredPane);
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        mainPanel.setBounds(0, 0, (int) width, (int) height);

        // Création et initialisation du MenuView
        menuView = new MenuView(menuController);
        menuView.initialize();
        mainPanel.add(menuView, "MENU");
        cardLayout.show(mainPanel, "MENU");

        setLocationRelativeTo(null);
        setVisible(true);
        setUpComponentListener();
    }

    /*
     * public Window(Level level, boolean isEditorMode, MenuController gc) {
     * super();
     * setTitle("CubeMayhem en 3D");
     * setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     * setSize((int) width, (int) height);
     * 
     * setLayout(new BorderLayout());
     * panel3D = new Panel3D(level, isEditorMode, gc);
     * add(panel3D, BorderLayout.CENTER);
     * setLocationRelativeTo(null);
     * setVisible(true);
     * setUpComponentListener();
     * }
     */

    public void setUpComponentListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                double previousWidth = width;
                double previousHeight = height;
                width = getWidth();
                height = getHeight();

                mainPanel.setBounds(0, 0, (int) width, (int) height);
                if (panel3D != null) {
                    panel3D.resize(previousWidth, previousHeight);
                }
            }
        });
    }

    public MenuView getMenuView() {
        return menuView;
    }

    public Panel2D getPanel2D() {
        return panel2D;
    }

    public Panel3D getPanel3D() {
        return panel3D;
    }

    /**
     * On l'utilise si isEditorMode, car normallement une nouvelle instance de
     * Panel2D est crée
     * après appel à switchTo2D
     * 
     * @param menuController
     * @param isEditorMode
     * @throws IOException
     */
    public void initialisePanel2D(MenuController menuController, boolean isEditorMode) throws IOException {
        panel2D = new Panel2D(menuController, isEditorMode);
    }

    public void goToMenu() {
        cardLayout.show(mainPanel, "MENU");
        if (panel2D != null)
            mainPanel.remove(panel2D);
        panel2D = null;
        if (panel3D != null)
            mainPanel.remove(panel3D);
        panel3D = null;
        revalidate();
        repaint();
    }

    public void switchToDisplay(MenuController menuController, boolean isEditorMode) {
        try {
            DisplayMode displayMode = menuController.getDisplayMode();
            System.out.println("switchToDisplayWithEditor: " + displayMode);

            if (displayMode == DisplayMode.GUI_2D) {
                if (panel2D == null) {
                    panel2D = new Panel2D(menuController, isEditorMode);
                }
                panel2D.setVisible(true);
                mainPanel.add(panel2D, "2D");
                cardLayout.show(mainPanel, "2D");
            } else if (displayMode == DisplayMode.GUI_3D) {
                if (panel3D == null) {
                    panel3D = new Panel3D(menuController, isEditorMode);
                }
                panel3D.setVisible(true);
                mainPanel.add(panel3D, "3D");
                cardLayout.show(mainPanel, "3D");
            }

            mainPanel.revalidate();
            mainPanel.repaint();
        } catch (Exception ex) {
            System.out.println("Erreur lors du chargement de la vue : " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Mise à jour du niveau/son affichage en 3D
     * 
     * @param level Niveau Actuel
     */
    public void update(MenuController menuController) {
        try {
            DisplayMode displayMode = menuController.getDisplayMode();
            if (displayMode == DisplayMode.GUI_2D) {
                if (panel2D != null) {
                    panel2D.updateView(menuController);
                }
            } else if (displayMode == DisplayMode.GUI_3D) {
                if (panel3D != null) {
                    panel3D.update(menuController);
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour de la vue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean panelIsEmpty(MenuController menuController) {
        DisplayMode displayMode = menuController.getDisplayMode();
        if (displayMode == DisplayMode.GUI_2D) {
            return panel2D == null;
        } else if (displayMode == DisplayMode.GUI_3D) {
            return panel3D == null;
        }
        return false;
    }

    /**
     * Redemmarade de niveau
     * 
     * @param level Niveau Actuel
     */
    public void restart(Level level) {
        System.out.println("heyooo");
        panel3D.restart(level);
    }

    public void showWinMessage() {
        if (winMessagePanel == null) {
            winMessagePanel = new WinMessagePanel(menuController, menuController.getGameState());
            winMessagePanel.initialize();
            layeredPane.add(winMessagePanel, JLayeredPane.MODAL_LAYER);

            // Définir les dimensions exactes du panneau
            winMessagePanel.setBounds(0, 0, getWidth(), getHeight());
            winMessagePanel.setPreferredSize(new java.awt.Dimension(getWidth(), getHeight()));
            winMessagePanel.setVisible(true);

            // Forcer le redimensionnement des composants
            winMessagePanel.handleResize();
            winMessagePanel.repaint();

            System.out.println("WinMessagePanel créé dans Window");
        } else {
            System.out.println("WinMessagePanel déjà existant");
            winMessagePanel.setVisible(true);
        }
    }

    public void hideWinMessage() {
        System.out.println("Fermeture du message de victoire");
        if (winMessagePanel != null) {
            System.out.println("Nettoyage du WinMessagePanel");
            winMessagePanel.cleanup();
            layeredPane.remove(winMessagePanel);
            winMessagePanel = null;
            layeredPane.revalidate();
            layeredPane.repaint();

            // S'assurer que le panneau 2D est bien visible après la fermeture
            if (panel2D != null) {
                panel2D.setVisible(true);
                panel2D.repaint();
            }

            // Forcer la mise à jour de l'affichage
            mainPanel.revalidate();
            mainPanel.repaint();
            // System.out.println("Message de victoire fermé avec succès");
        }
    }

}
