package view.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.MenuController;
import model.UI.GameButton;
import model.level.GameState;
import model.level.UserProgress;
import model.tools.Constants;
import model.tools.LoadSave;

public class NewGamePanel extends JPanel {

    private MenuController menuController;
    private BufferedImage backgroundImg;

    private ImagePanel textFieldPanel;

    private GameButton[] buttons;

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    // Dimensions par défaut
    private static final int DEFAULT_BACKGROUND_WIDTH = 1600;
    private static final int DEFAULT_BACKGROUND_HEIGHT = 1000;
    private static final int DEFAULT_NEWGAME_WIDTH = 410;
    private static final int DEFAULT_NEWGAME_HEIGHT = 258;
    private static final int DEFAULT_TEXTFIELD_WIDTH = 336;
    private static final int DEFAULT_TEXTFIELD_HEIGHT = 64;
    private static final int DEFAULT_BUTTON_WIDTH = 150;
    private static final int DEFAULT_BUTTON_HEIGHT = 88;
    private static final int DEFAULT_FONT_SIZE = 30;
    private static final int DEFAULT_TEXTFIELD_FONT_SIZE = 25;

    float minWidth = (float) Math.min(DEFAULT_BACKGROUND_WIDTH, Constants.Window.DEFAULT_WIDTH);
    float minHeight = (float) Math.min(DEFAULT_BACKGROUND_HEIGHT, Constants.Window.DEFAULT_HEIGHT);

    private int calculateFontSize(int baseSize) {
        return (int) (baseSize * Math.min(scaleX, scaleY));
    }

    private int calculateButtonSize(int baseSize) {
        return (int) (baseSize * Math.min(scaleX, scaleY));
    }

    public NewGamePanel(MenuController menuController, GameState gameState) {
        super();
        this.menuController = menuController;
        backgroundImg = LoadSave.GetSprite("bckgHomeMenu");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateScaling();
                revalidate();
                repaint();
            }
        });
    }

    private void updateScaling() {
        if (backgroundImg != null) {
            scaleX = getWidth() / minWidth;
            scaleY = getHeight() / minHeight;
        }
    }

    public void initialize() {
        initialise();
    }

    public void updateComponents() {
        // Mise à jour des composants si nécessaire
        revalidate();
        repaint();
    }

    public void handleResize() {
        updateScaling();
        // Mise à jour des tailles de police et des boutons
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setFont(new Font("Segoe UI", Font.BOLD, calculateFontSize(DEFAULT_FONT_SIZE)));
            } else if (comp instanceof JTextField) {
                JTextField textField = (JTextField) comp;
                textField.setFont(new Font("Segoe UI", Font.PLAIN, calculateFontSize(DEFAULT_TEXTFIELD_FONT_SIZE)));
            }
        }
        // Mise à jour des boutons
        for (GameButton button : buttons) {
            if (button != null) {
                button.resize(scaleX, scaleY);
            }
        }
        revalidate();
        repaint();
    }

    public void initialise() {
        buttons = new GameButton[2];
        setLayout(new GridBagLayout());
        textFieldPanel = new ImagePanel("textField");
        textFieldPanel.setLayout(new BorderLayout());
        textFieldPanel.setOpaque(false);

        JPanel centerPanel = new ImagePanel("newGameBg");
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        add(centerPanel, new GridBagConstraints());

        JLabel usernameLabel = new JLabel("Enter username:");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, calculateFontSize(DEFAULT_FONT_SIZE)));
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameLabel.setBorder(BorderFactory.createEmptyBorder(20, 15, 10, 15));

        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, calculateFontSize(DEFAULT_TEXTFIELD_FONT_SIZE)));
        usernameField.setOpaque(false);
        usernameField.setBorder(BorderFactory.createEmptyBorder(10, 15, 17, 15));
        usernameField.setForeground(Color.WHITE);
        usernameField.setCaretColor(Color.WHITE);

        centerPanel.add(usernameLabel);
        textFieldPanel.add(usernameField, BorderLayout.CENTER);
        centerPanel.add(textFieldPanel);

        // Boutons OK et Annuler
        GameButton okButton = new GameButton("spriteNewGame", 3, 0, 0,
                DEFAULT_BUTTON_WIDTH,
                DEFAULT_BUTTON_HEIGHT, 0);
        GameButton cancelButton = new GameButton("spriteNewGame", 3, 0, 0,
                DEFAULT_BUTTON_WIDTH,
                DEFAULT_BUTTON_HEIGHT, 1);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        buttons[0] = okButton;
        buttons[1] = cancelButton;
        centerPanel.add(buttonPanel);

        // Gestionnaire d'événements pour le bouton OK
        okButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (isValidUsername(username)) {
                UserProgress progress = new UserProgress(username, 1);
                menuController.getGameController().setUserProgress(progress);
                menuController.getLevelController().setMaxLevelUnlocked(progress.getMaxLevelUnlocked());
                menuController.getGameState().setCurrentState(GameState.GameMode.LEVEL_SELECTION);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Nom d'utilisateur invalide. Veuillez utiliser uniquement des lettres, des chiffres et des tirets bas (_).",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Gestionnaire d'événements pour le bouton Annuler
        cancelButton.addActionListener(e -> {
            menuController.getGameState().setCurrentState(GameState.GameMode.MAIN_SCREEN);
        });
    }

    /**
     * Méthode permettant de vérifier si le nom d'utilisateur est valide
     *
     * @param username
     * @return true si nom d'utilisateur valide, false sinon
     */
    public boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9_]+$") && !username.isEmpty();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImg != null) {
            g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);
        }
        for (GameButton button : buttons)
            button.update();
    }

    public void updateButtons() {
        // Pas de boutons à mettre à jour dans ce panneau
    }

    class ImagePanel extends JPanel {

        private BufferedImage backgroundImg;
        private String imageName;

        public ImagePanel(String imageName) {
            super();
            this.imageName = imageName;
            backgroundImg = LoadSave.GetSprite(imageName);
            addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    updateSize();
                }
            });
        }

        private void updateSize() {
            if (backgroundImg != null) {
                int width, height;
                if (imageName.equals("newGameBg")) {
                    width = (int) (DEFAULT_NEWGAME_WIDTH * scaleX);
                    height = (int) (DEFAULT_NEWGAME_HEIGHT * scaleY);
                } else if (imageName.equals("textField")) {
                    width = (int) (DEFAULT_TEXTFIELD_WIDTH * scaleX);
                    height = (int) (DEFAULT_TEXTFIELD_HEIGHT * scaleY);
                } else {
                    width = (int) (backgroundImg.getWidth() * scaleX);
                    height = (int) (backgroundImg.getHeight() * scaleY);
                }
                setPreferredSize(new Dimension(width, height));
                setMinimumSize(new Dimension(width, height));
                setMaximumSize(new Dimension(width, height));
                revalidate();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImg != null) {
                int width = getWidth();
                int height = getHeight();
                g.drawImage(backgroundImg, 0, 0, width, height, this);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            if (backgroundImg != null) {
                if (imageName.equals("newGameBg")) {
                    return new Dimension(
                            (int) (DEFAULT_NEWGAME_WIDTH * scaleX),
                            (int) (DEFAULT_NEWGAME_HEIGHT * scaleY));
                } else if (imageName.equals("textField")) {
                    return new Dimension(
                            (int) (DEFAULT_TEXTFIELD_WIDTH * scaleX),
                            (int) (DEFAULT_TEXTFIELD_HEIGHT * scaleY));
                }
            }
            return super.getPreferredSize();
        }
    }

}
