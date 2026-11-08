package view.affichage2D;

import model.level.GameState.DisplayMode;
import model.level.GameState.GameMode;
import model.UI.GameButton;
import model.level.Level;
import model.level.LevelFileManager;
import model.level.plate.Case;
import model.level.tiles.TileType;
import model.tools.Constants.Path;
import model.tools.Direction;
import view.Window;
import view.menu.ControlButtons;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import controller.MenuController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.io.IOException;
import java.util.Map;

public class Panel2D extends JPanel {

    private boolean isEditorMode = false;
    private Level currentLevel;

    private JPanel controlButtonsPanel;
    private ControlButtons controlButtons;
    private GamePanel2D gamePanel2D;
    private InventoryPanel inventoryPanel2D;

    private MenuController menuController;

    public Panel2D(MenuController menuController) throws IOException {

        super();

        setLayout(new BorderLayout());

        this.menuController = menuController;
        currentLevel = menuController.getCurrentLevel();

        gamePanel2D = new GamePanel2D(currentLevel);
        add(gamePanel2D, BorderLayout.CENTER);

        JPanel controlButtonsPanel = new JPanel();
        controlButtonsPanel.setLayout(null);
        controlButtonsPanel.setPreferredSize(new Dimension(0, 90));
        controlButtons = new ControlButtons(menuController, controlButtonsPanel);
        add(controlButtonsPanel, BorderLayout.NORTH);

        inventoryPanel2D = new InventoryPanel(currentLevel.getPlate().getTilesAvailable(), menuController);
        add(inventoryPanel2D, BorderLayout.SOUTH);

        // add(new JPanel(), BorderLayout.WEST);
        // add(new JPanel(), BorderLayout.EAST);

    }

    public Panel2D(MenuController menuController, boolean isEditorMode) throws IOException {
        super();

        setLayout(new BorderLayout());

        this.menuController = menuController;
        currentLevel = menuController.getCurrentLevel();

        controlButtonsPanel = new JPanel();
        controlButtonsPanel.setLayout(null);
        controlButtonsPanel.setPreferredSize(new Dimension(0, 90));
        controlButtons = new ControlButtons(menuController, controlButtonsPanel);
        add(controlButtonsPanel, BorderLayout.NORTH);

        isEditorMode = true;
    }

    public LevelView getLevelView() {
        return gamePanel2D;
    }

    public InventoryPanel getInventoryPanel() {
        return inventoryPanel2D;
    }

    public boolean isEditorMode() {
        return isEditorMode;
    }

    private void handleSave(Level currentLevel) {
        JFileChooser fileChooser = LevelFileManager.configureFileChooser(Path.EDITOR_LEVELS_DIRECTORY,
                "Sauvegarder le niveau", "txt", "Fichiers texte (*.txt)");

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String fileToSave = fileChooser.getSelectedFile().getAbsolutePath();
            // Rajouter l'extension .txt si l'utilisateur ne l'a pas fait
            if (!fileToSave.toLowerCase().endsWith(".txt")) {
                fileToSave += ".txt";
            }
            try {
                currentLevel.getPlate().determinePlateSolution();
                LevelFileManager.exportLevel(currentLevel.getPlate(), fileToSave);
                JOptionPane.showMessageDialog(this, "Niveau sauvegardé avec succès !");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la sauvegarde !");
                ex.printStackTrace();
            }
        }
    }

    public void setEditorMode(MenuController menuController) throws IOException {
        isEditorMode = true;

        // Haut : bouton sauvegarder
        JButton saveButton = new JButton("Sauvegarder");
        saveButton.setBounds(10, 10, 120, 30);
        saveButton.addActionListener(e -> {
            handleSave(menuController.getCurrentLevel());
            menuController.getWindow().goToMenu();
        });
        controlButtonsPanel.add(saveButton);

        // Centre : vue du niveau
        gamePanel2D = new GamePanel2D(menuController.getCurrentLevel());
        add(gamePanel2D, BorderLayout.CENTER);

        // Bas : inventaire
        inventoryPanel2D = new InventoryPanel(menuController.getCurrentLevel().getPlate().getTilesAvailable(),
                menuController);
        add(inventoryPanel2D, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    public void updateView(MenuController menuController) throws IOException {
        if (gamePanel2D == null || gamePanel2D.getLevel() != menuController.getCurrentLevel()) {
            System.out.println("newww in panel 2D");
            currentLevel = menuController.getCurrentLevel();
            if (gamePanel2D != null)
                remove(gamePanel2D);
            if (inventoryPanel2D != null)
                remove(inventoryPanel2D);
            gamePanel2D = new GamePanel2D(currentLevel);
            add(gamePanel2D, BorderLayout.CENTER);
            inventoryPanel2D = new InventoryPanel(currentLevel.getPlate().getTilesAvailable(), menuController);
            add(inventoryPanel2D, BorderLayout.SOUTH);
            revalidate();
            repaint();
        }
        if (gamePanel2D != null) {
            gamePanel2D.updateLevelView(menuController.getCurrentLevel());
        }
    }

    public void addLevelControlButtons() {
        controlButtons.addLevelControlButtons();
    }

    class GamePanel2D extends LevelView {

        GamePanel2D(Level level) {
            super(level);

            setMaximumSize(new Dimension(100, getHeight()));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    try {
                        handleMouseEvent(e);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }

        public void handleMouseEvent(MouseEvent e) throws IOException {
            Point screenPoint = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(screenPoint, this);

            int i = currentLevel.getPlate().getI();
            int j = currentLevel.getPlate().getJ();
            int tileSizeI = (getHeight() - (i - 1) * 5) / i;
            int tileSizeJ = (getWidth() - (j - 1) * 5) / j;
            int gridI = (screenPoint.y - 10) / (tileSizeI + 5);
            int gridJ = (screenPoint.x - 10) / (tileSizeJ + 5);
            // on converti correctement gridJ et gridI avec les espacement qu'il y a entre
            // les cases

            if (gridI < 0 || gridI >= i || gridJ < 0 || gridJ >= j) {
                System.out.println(" Clic hors du plateau : (" + gridI + ", " + gridJ + ")");
                return;
            }

            Map<TileType, Integer> availableTiles = currentLevel.getPlate().getTilesAvailable();
            Case caze = currentLevel.getPlate().getCase(gridI, gridJ);

            if (e.getButton() == MouseEvent.BUTTON3) { // clic droit -> Supprimer une tuile
                if (!caze.isEmpty()) {
                    if ((caze.isStart() || caze.isEnd() || caze.isRestrict() || caze.getTile().getTileType() == TileType.SWITCH_OFF) && !currentLevel.getPlate().isLevelEditor()) {
                        return; // empecher la suppression de start et end
                    }
                    TileType removedTile = caze.getTile().getTileType();
                    caze.removeTile();

                    if (currentLevel.getPlate().isLevelEditor()) {
                        currentLevel.getPlate().lockCase(gridI, gridJ);
                        System.out.println(" Case vérouillée en (" + gridI + ", " + gridJ + ")");
                    }
                    // rajouter x1 à l'inventaire
                    inventoryPanel2D.modifyTileCount(removedTile, +1, availableTiles);
                    System.out.println(" Tuile supprimée en (" + gridI + ", " + gridJ + ")");
                    Panel2D.this.updateView(menuController);
                } else {
                    System.out.println(" Aucune tuile à cet emplacement.");
                }
            }

            if (e.getButton() == MouseEvent.BUTTON1) { // clic gauche -> placer une tuile
                TileType selectedTile = InventoryPanel.getSelectedTileType(); // récupère la sélection

                if (selectedTile == null && inventoryPanel2D.getCurrentCount() != 0) {
                    if (!caze.isEmpty()) {
                        // Vérifier si la case a une tuile et une TileView avant de tourner
                        CasePanel casePanel = getCasePanel(gridI, gridJ);
                        if (casePanel != null && casePanel.getTileView() != null) {
                            if (caze.getTile().isDirectional() || caze.getTile().isType(TileType.START)
                                    && currentLevel.getPlate().isLevelEditor()) {
                                casePanel.getTileView().spinSprite();
                                caze.getTile().rotate();
                            }
                            if (!caze.isEmpty() && caze.getTile().isType(TileType.START)) {
                                Direction dir = caze.getTile().getDirection();
                                System.out.println("Direction actuelle de la tuile START : " + dir);
                            }
                            Panel2D.this.updateView(menuController);
                        }
                        return;
                    }
                    System.out.println(" Aucune tuile sélectionnée !");
                    return;
                }

                if (!caze.isEmpty()) {
                    System.out.println(" Une tuile est déjà présente !");
                    //
                    return;
                }

                // Si le mode éditeur est activé, on peut débloquer la case avant de placer la
                // tuile
                if (currentLevel.getPlate().isLevelEditor()) {
                    currentLevel.getPlate().unlockCase(gridI, gridJ);
                    System.out.println(" Case déverrouillée en (" + gridI + ", " + gridJ + ")");
                }
                // on modifie le compteur de la tuile sélectionnée
                try {
                    currentLevel.getPlate().place(selectedTile, gridI, gridJ, Direction.NORTH,
                            menuController.getGameState().getDisplayMode());
                    inventoryPanel2D.modifyTileCount(selectedTile, -1, availableTiles);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                System.out.println(" Tuile placée : " + selectedTile + " en (" + gridI + ", " + gridJ + ")");

                InventoryPanel.clearSelectedTile(); // réinitialise après placement

                updateView(menuController);
            }
        }

    }
}
