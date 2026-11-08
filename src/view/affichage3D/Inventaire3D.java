package view.affichage3D;

import java.awt.AlphaComposite;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import model.UI.GameButton;
import model.level.GameState.GameMode;
import model.level.tiles.TileBank;
import model.level.tiles.TileType;
import model.tools.Direction;
import view.Window;
import view.affichage3D.Panel3D.GamePanel3D;
import view.affichage3D.polygone.Tile3D;
import controller.MenuController;

public class Inventaire3D extends JPanel {

    private static TileType selectedTile = null;
    private static Tile3D currentTile = null;
    private GamePanel3D gamePanel;
    private DisposingInventory3D disposingIn;
    private ExecutingInventory3D executingIn;
    private CardLayout cardLayout;
    MenuController menuController;

    public Inventaire3D(Map<TileType, Integer> tilesAvailable, MenuController menuController, GamePanel3D gamePanel) {

        super();
        this.menuController = menuController;
        this.gamePanel = gamePanel;
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setOpaque(false);

        position();

        disposingIn = new DisposingInventory3D(tilesAvailable);
        executingIn = new ExecutingInventory3D();

        add(disposingIn, "DISPOSING");
        add(executingIn, "EXECUTING");

        cardLayout.show(this, "DISPOSING");
        revalidate();
        repaint();
    }

    public static void setSelectedTile(TileType tileType) {
        selectedTile = tileType;
    }

    public static TileType getSelectedTile() {
        return selectedTile;
    }

    public static void setCurrentTile(Tile3D tile) {
        currentTile = tile;
    }

    public static Tile3D getCurrentTile() {
        return currentTile;
    }

    public void update() {
        disposingIn.update();
    }

    public void restart(Map<TileType, Integer> tilesAvailable, GamePanel3D gamePanel3D) {
        gamePanel = gamePanel3D;
        menuController.getGameState().setCurrentState(GameMode.DISPOSING);
        switchToDisposing();
        disposingIn.restart(tilesAvailable);
    }

    /** Pour afficher le disposingIn */
    public void switchToDisposing() {
        if (menuController.getGameState().getCurrentState() == GameMode.DISPOSING) {
            gamePanel.addMouseListener3D();
            cardLayout.show(this, "DISPOSING");
            revalidate();
            repaint();
        } else {
            throw new IllegalStateException("Le currentState doit être GameMode.DISPOSING");
        }
    }

    /** Pour afficher le executingIn */
    public void switchToExecuting() {
        if (menuController.getGameState().getCurrentState() == GameMode.EXECUTING) {
            gamePanel.removeMouseListener3D();
            cardLayout.show(this, "EXECUTING");
            revalidate();
            repaint();
        } else {
            throw new IllegalStateException("Le currentState doit être GameMode.EXECUTING");
        }
    }

    /** Définit la position de l'inventaire 3D */
    public void position() {
        int x = (int) Window.width / 32;
        int y = (int) Window.height * 5 / 6;
        setBounds(x, y, (int) Window.width * 30 / 32, (int) Window.height / 10);
        setPreferredSize(new Dimension(600, 100));
        if (executingIn != null)
            executingIn.resize();
        if (disposingIn != null)
            disposingIn.resize();
    }

    /** Le panel avec le boutons "end" qui s'affiche quand on lance le jeu */
    class ExecutingInventory3D extends JPanel {

        private GameButton endButton;

        public ExecutingInventory3D() {
            super();

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setOpaque(false);

            endButton = new GameButton("startPauseButtons", 3, 0, 0, 88, 88, 1);
            endButton.addActionListener(e -> {
                if (menuController.getGameState().getCurrentState() == GameMode.EXECUTING) {
                    menuController.getGameState().setCurrentState(GameMode.DISPOSING);
                    menuController.getGameController().getCubeController().restartCubePosition();
                    switchToDisposing();
                }
            });
            endButton.setPreferredSize(new Dimension(100, 100));
            endButton.setMaximumSize(new Dimension(100, 100));
            this.add(endButton);
            endButton.setVisible(true);
        }

        public void resize() {
            if (endButton != null) {
                endButton.setPreferredSize(new Dimension(Inventaire3D.this.getHeight(), Inventaire3D.this.getHeight()));
                endButton.setMaximumSize(new Dimension(Inventaire3D.this.getHeight(), Inventaire3D.this.getHeight()));
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            endButton.update();
        }

    }

    /** Le panel qui permet de placer les tuiles */
    class DisposingInventory3D extends JPanel {

        private Map<TileType, Integer> tilesAvailable;
        private ArrayList<InventoryButton3D> buttons;
        private GameButton doneButton;
        private int size;

        public DisposingInventory3D(Map<TileType, Integer> tilesAvailable) {
            super();
            setOpaque(false);
            initialise(tilesAvailable);
        }

        /**
         * Initialise les boutons pour l'inventaire 3D
         */
        public void initialiseButtons() {

            // On initialise les boutons des tuiles
            for (Map.Entry<TileType, Integer> entry : this.tilesAvailable.entrySet()) {
                InventoryButton3D button = new InventoryButton3D(entry.getKey(), entry.getValue());
                buttons.add(button);
                this.add(button);
            }

            doneButton = new GameButton("startPauseButtons", 3, 0, 0, 88, 88, 0);
            doneButton.addActionListener(e -> {
                if (menuController.getGameState().getCurrentState() == GameMode.DISPOSING) {
                    menuController.getGameState().setCurrentState(GameMode.EXECUTING);
                    switchToExecuting();
                }
            });
            doneButton.setPreferredSize(new Dimension(100, 100));
            doneButton.setMaximumSize(new Dimension(100, 100));
            add(doneButton);
        }

        /**
         * Initialise l'inventaire3D
         * 
         * @param tilesAvailable
         */
        public void initialise(Map<TileType, Integer> tilesAvailable) {
            this.tilesAvailable = tilesAvailable;
            size = tilesAvailable.size();
            buttons = new ArrayList<>();

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setPreferredSize(new Dimension((size + 1) * 100, 100));
            initialiseButtons();
        }

        public void update() {
            int value;
            int i = 0;
            for (Map.Entry<TileType, Integer> entry : this.tilesAvailable.entrySet()) {
                value = entry.getValue();
                if (buttons.get(i).getQuantity() != value) {
                    buttons.get(i).setQuantity(value);
                }
                if (entry.getKey() == selectedTile && value == 0) {
                    selectedTile = null;
                }
                i++;
            }
        }

        public void restart(Map<TileType, Integer> tilesAvailable) {
            reset();
            removeAll();
            initialise(tilesAvailable);
        }

        public void reset() {
            tilesAvailable = null;
            buttons = null;
            size = 0;
        }

        public void resize() {
            if (buttons != null) {
                for (InventoryButton3D b : buttons) {
                    b.setPreferredSize(new Dimension(Inventaire3D.this.getHeight(), Inventaire3D.this.getHeight()));
                    b.setMaximumSize(new Dimension(Inventaire3D.this.getHeight(), Inventaire3D.this.getHeight()));
                }
            }
            if (doneButton != null) {
                doneButton
                        .setPreferredSize(new Dimension(Inventaire3D.this.getHeight(), Inventaire3D.this.getHeight()));
                doneButton.setMaximumSize(new Dimension(Inventaire3D.this.getHeight(), Inventaire3D.this.getHeight()));
            }
        }

        @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                doneButton.update();
            }

        /** Bouton avec l'image de tuile */
        class InventoryButton3D extends JButton {

            private BufferedImage image;
            private final TileType tileType;
            private int quantity;

            public InventoryButton3D(TileType tileType, int quantity) {
                super();
                this.tileType = tileType;
                this.quantity = quantity;
                setImage();
                setPreferredSize(new Dimension(Inventaire3D.this.getHeight(), Inventaire3D.this.getHeight()));
                setMaximumSize(new Dimension(Inventaire3D.this.getHeight(), Inventaire3D.this.getHeight()));

                addActionListener(e -> {
                    if (selectedTile != tileType && this.quantity > 0) {
                        selectedTile = tileType;
                    } else
                        selectedTile = null;
                });
            }

            public void setImage() {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                try {
                    image = TileBank.getTileImage(tileType, Direction.NORTH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public int getQuantity() {
                return quantity;
            }

            public void setQuantity(int q) {
                quantity = q;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;

                if (image != null) {
                    if (tileType != selectedTile)
                        g2d.setComposite(AlphaComposite.SrcOver.derive(0.7f));
                    g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                    
                }
                g2d.setFont(new Font("Futura", g2d.getFont().getStyle(), 45));
                g2d.drawString("" + quantity, 68, 90);
            }

        }

    }

}
