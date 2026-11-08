package view.affichage2D;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import javax.swing.JScrollBar;
import javax.swing.Box;
import javax.swing.JLayeredPane;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import model.level.tiles.Tile;
import model.level.tiles.TileType;
import model.tools.Constants;
import view.Window;
import model.UI.GameButton;
import model.level.GameState.GameMode;
import controller.MenuController;
import controller.CubeController;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class InventoryPanel extends JLayeredPane {
    private Window parentWindow;
    private JPanel tilesContainer;
    private JButton leftArrow;
    private JButton rightArrow;
    private JScrollPane scrollPane;
    private static TileType selectedTileType = null;
    private Map<TileType, JLabel> tileLabels = new HashMap<>();
    private List<TileType> orderedTileTypes = new ArrayList<>();
    private GameButton doneButton;
    private GameButton stopButton;
    private MenuController menuController;
    private JPanel mainPanel;

    private int currentCount;

    public InventoryPanel(Map<TileType, Integer> availableTiles, MenuController menuController) throws IOException {
        this.menuController = menuController;
        setLayout(null);
        setPreferredSize(new Dimension(Constants.InventoryPanel.DEFAULT_WIDTH_INVENTORY,
                Constants.InventoryPanel.DEFAULT_HEIGHT_INVENTORY));

        // Création du panel principal
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBounds(0, 0, getPreferredSize().width, getPreferredSize().height);
        mainPanel.setOpaque(false);

        // Création des boutons de navigation
        leftArrow = new JButton("<");
        rightArrow = new JButton(">");
        leftArrow.setEnabled(false);
        rightArrow.setEnabled(false);

        // Positionnement des boutons de contrôle
        int buttonX = getPreferredSize().width - 100;
        int buttonY = getPreferredSize().height - 50;

        // Création des boutons de contrôle
        doneButton = new GameButton("spriteControlButtons", 3, buttonX, buttonY, 44, 44, 0, GameMode.EXECUTING);
        stopButton = new GameButton("spriteControlButtons", 3, buttonX + 50, buttonY, 44, 44, 1, GameMode.DISPOSING);

        // Création du conteneur des tuiles
        tilesContainer = new JPanel();
        tilesContainer.setLayout(new BoxLayout(tilesContainer, BoxLayout.X_AXIS));
        tilesContainer.setBackground(Color.LIGHT_GRAY);

        // Création du JScrollPane
        scrollPane = new JScrollPane(tilesContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        // Ajout des composants au panel principal
        mainPanel.add(leftArrow, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(rightArrow, BorderLayout.EAST);

        // Ajout des composants au JLayeredPane
        add(mainPanel, Integer.valueOf(0));
        add(doneButton, Integer.valueOf(1));
        add(stopButton, Integer.valueOf(1));

        // Gestionnaire d'événements pour les flèches
        leftArrow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentValue = scrollPane.getHorizontalScrollBar().getValue();
                scrollPane.getHorizontalScrollBar().setValue(currentValue - 100);
                updateArrowButtons();
            }
        });

        rightArrow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentValue = scrollPane.getHorizontalScrollBar().getValue();
                scrollPane.getHorizontalScrollBar().setValue(currentValue + 100);
                updateArrowButtons();
            }
        });

        // Ajout d'un ComponentListener pour gérer le redimensionnement
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (getParent() != null) {
                        int parentHeight = getParent().getHeight();
                        int newHeight = parentHeight / 6;
                        setPreferredSize(new Dimension(getParent().getWidth(), newHeight));
                        mainPanel.setBounds(0, 0, getWidth(), getHeight());

                        // Mettre à jour la position des boutons de contrôle
                        // (positionnement relatif à la largeur de CE panneau, pas à la
                        // largeur de la fenêtre entière : sinon les boutons finissent
                        // hors des limites visibles et sont rognés)
                        int panelWidth = getWidth();
                        int doneX = panelWidth - doneButton.getWidth() - 20;
                        int stopX = doneX - stopButton.getWidth() - 10;
                        doneButton.updatePosition(doneX, 10);
                        stopButton.updatePosition(stopX, 10);

                        // Mettre à jour la taille de tous les layeredPanes
                        for (Component comp : getComponents()) {
                            if (comp instanceof JLayeredPane) {
                                JLayeredPane layeredPane = (JLayeredPane) comp;
                                int tileSize = Math.min((newHeight - 10), (getWidth() / 7) - 10);
                                layeredPane.setPreferredSize(new Dimension(tileSize, tileSize));

                                for (Component tileComp : layeredPane.getComponents()) {
                                    if (tileComp instanceof TileView) {
                                        tileComp.setBounds(0, 0, tileSize, tileSize);
                                    } else if (tileComp instanceof JLabel) {
                                        double p = Constants.InventoryPanel.DEFAULT_HEIGHT_INVENTORY / tileSize;
                                        tileComp.setFont(new Font("Arial", Font.BOLD, (int) (16 * p)));
                                        tileComp.setBounds(tileSize - 35, tileSize - 25, 30, 20);
                                    }
                                }
                            }
                        }

                        revalidate();
                        repaint();
                        updateArrowButtons();
                    }
                });
            }
        });

        // Ajout des tuiles dans l'ordre défini par l'énumération TileType
        for (TileType tileType : TileType.values()) {
            Integer number = availableTiles.get(tileType);
            if (number != null && number > 0) {
                orderedTileTypes.add(tileType);
                addTileToInventory(tileType, number);
            }
        }

        // Mise à jour initiale des boutons de navigation
        updateArrowButtons();
        setVisible(true);
    }

    /**
     * Met à jour les boutons de navigation en fonction de la position du scroll
     */
    public void updateArrowButtons() {
        JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
        leftArrow.setEnabled(horizontalScrollBar.getValue() > 0);
        rightArrow.setEnabled(horizontalScrollBar.getValue() < horizontalScrollBar.getMaximum()
                - horizontalScrollBar.getVisibleAmount());
    }

    /**
     * Ajoute une tuile à l'inventaire
     * 
     * @param tileType le type de tuile à ajouter
     * @param count    le nombre de tuiles à ajouter
     */
    private void addTileToInventory(TileType tileType, int count) {
        JLayeredPane layeredPane = new JLayeredPane();
        int tileSize = Math.min((Constants.InventoryPanel.DEFAULT_HEIGHT_INVENTORY - 10),
                (Constants.InventoryPanel.DEFAULT_WIDTH_INVENTORY / 7) - 10);
        layeredPane.setPreferredSize(new Dimension(tileSize, tileSize));

        Tile tile = new Tile(tileType);
        TileView tileView = new TileView(tile);
        Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                tileView.getImage(), new Point(0, 0), "customCursor");
        layeredPane.setCursor(customCursor);
        tileView.setBounds(0, 0, tileSize, tileSize);

        JLabel numberLabel = new JLabel("x " + count, SwingConstants.CENTER);
        numberLabel.setFont(new Font("Arial", Font.BOLD, 16));
        numberLabel.setForeground(Color.BLACK);
        numberLabel.setBackground(new Color(255, 255, 255, 180));
        numberLabel.setBounds(tileSize - 35, tileSize - 25, 30, 20);

        tileLabels.put(tileType, numberLabel);

        layeredPane.add(tileView, Integer.valueOf(0));
        layeredPane.add(numberLabel, Integer.valueOf(1));

        tileView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    selectedTileType = tileType;
                    System.out.println(" Tuile sélectionnée : " + tileType);
                }
            }
        });

        tilesContainer.add(layeredPane);
        tilesContainer.add(Box.createHorizontalStrut(5));
        updateArrowButtons();
    }

    public void modifyTileCount(TileType tileType, int delta, Map<TileType, Integer> availableTiles) {
        JLabel label = tileLabels.get(tileType);
        currentCount = label != null ? Integer.parseInt(label.getText().substring(2).trim()) : 0;
        int newCount = currentCount + delta;

        availableTiles.put(tileType, Math.max(newCount, 0));

        if (newCount > 0) {
            if (label != null) {
                label.setText("x " + newCount);
            } else {
                // Si la tuile n'existe pas encore, trouver sa position correcte selon
                // l'énumération
                int insertIndex = 0;
                for (TileType type : TileType.values()) {
                    if (type == tileType) {
                        break;
                    }
                    if (orderedTileTypes.contains(type)) {
                        insertIndex++;
                    }
                }
                orderedTileTypes.add(insertIndex, tileType);

                // on reecrit l'affichage dans l'ordre
                tilesContainer.removeAll();
                for (TileType type : orderedTileTypes) {
                    int count = availableTiles.get(type);
                    if (count > 0) {
                        if (tileLabels.containsKey(type)) {
                            Component parent = tileLabels.get(type).getParent();
                            tilesContainer.add(parent);
                            tilesContainer.add(Box.createHorizontalStrut(5));
                        } else {
                            addTileToInventory(type, count);
                        }
                    }
                }
            }
        } else {
            // Supprimer la tuile
            Component parent = label.getParent();
            if (parent != null) {
                tilesContainer.remove(parent);
                tilesContainer.remove(tilesContainer.getComponent(tilesContainer.getComponentCount() - 1));
            }
            tileLabels.remove(tileType);
            orderedTileTypes.remove(tileType);
        }

        revalidate();
        repaint();
        updateArrowButtons();
    }

    public static TileType getSelectedTileType() {
        return selectedTileType;
    }

    public static void clearSelectedTile() {
        selectedTileType = null;
    }

    // @Override
    // protected void paintComponent(Graphics g) {
    // super.paintComponent(g);
    // // Dimension size = this.getParent().getParent().getSize();
    // // ;
    // }
    public int getCurrentCount() {
        return currentCount;
    }

    public MenuController getMenuController() {
        return menuController;
    }

    public void updateInventory(Map<TileType, Integer> availableTiles) {
        // Mettre à jour chaque compteur de tuile
        for (Map.Entry<TileType, Integer> entry : availableTiles.entrySet()) {
            TileType type = entry.getKey();
            int count = entry.getValue();
            JLabel label = tileLabels.get(type);
            if (label != null) {
                label.setText("x " + count);
            }
        }
        revalidate();
        repaint();
    }
}
