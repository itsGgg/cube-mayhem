package view.menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Timer;

import controller.MenuController;
import model.UI.GameButton;
import model.level.GameState;
import model.tools.Constants;

public class CreditsMenu extends JPanel {
    private MenuController menuController;
    private GameState gameState;
    private GameButton backButton;
    private List<CreditText> creditTexts;
    private Timer animationTimer;
    private int scrollOffset = 0;
    private static final int SCROLL_SPEED = 1;
    private static final int ANIMATION_DELAY = 16; // ~60 FPS

    private class CreditText {
        String text;
        int y;
        boolean isTitle;
        float alpha;
        float targetAlpha;
        float alphaSpeed;

        public CreditText(String text, int y, boolean isTitle) {
            this.text = text;
            this.y = y;
            this.isTitle = isTitle;
            this.alpha = 0;
            this.targetAlpha = 1.0f;
            this.alphaSpeed = 0.02f;
        }

        public void update() {
            if (alpha < targetAlpha) {
                alpha += alphaSpeed;
                if (alpha > targetAlpha)
                    alpha = targetAlpha;
            }
        }
    }

    public CreditsMenu(MenuController menuController, GameState gameState) {
        super();
        this.menuController = menuController;
        this.gameState = gameState;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(Color.BLACK);
    }

    private void initBackButton() {
        backButton = new GameButton("extrasMenu", 1, 0, 0, 168, 88, 3, "RETOUR");

        backButton.addActionListener(e -> {
            menuController.getGameState().setCurrentState(GameState.GameMode.EXTRAS);
            menuController.getWindow().getMenuView().showExtrasMenu();
        });

        add(backButton);
    }

    private void initCreditTexts() {
        creditTexts = new ArrayList<>();
        int y = 100;

        // Titre
        creditTexts.add(new CreditText("CRÉDITS", y, true));
        y += 60;

        // Développement
        creditTexts.add(new CreditText("[DÉVELOPPEMENT]", y, true));
        y += 40;
        creditTexts.add(new CreditText("Producteur : ..............", y, false));
        y += 30;
        creditTexts.add(new CreditText("Game Design : ..............", y, false));
        y += 30;
        creditTexts.add(new CreditText("Programmeurs :", y, false));
        y += 30;
        creditTexts.add(new CreditText("Georgel Gritar", y, false));
        y += 30;
        creditTexts.add(new CreditText("Borine Thibault Hem", y, false));
        y += 30;
        creditTexts.add(new CreditText("Jakub Kowalczuk", y, false));
        y += 30;
        creditTexts.add(new CreditText("Lilian Linder", y, false));
        y += 30;
        creditTexts.add(new CreditText("Adem Ben Marzouk", y, false));
        y += 30;
        creditTexts.add(new CreditText("Art 2D/3D : Jakub Kowalczuk (3D), Adem Ben Marzouk (2D)", y, false));
        y += 60;

        // Son & Musique
        creditTexts.add(new CreditText("[SON & MUSIQUE]", y, true));
        y += 40;
        creditTexts.add(new CreditText("Sound Design : Borine Thibault Hem", y, false));
        y += 60;

        // Remerciements
        creditTexts.add(new CreditText("[REMERCIEMENTS]", y, true));
        y += 40;
        creditTexts.add(new CreditText("Merci à ChatGPT !", y, false));
        y += 30;
        creditTexts.add(new CreditText("Inspiré par rotate et Kaaring gaming", y, false));
        y += 60;

        // Licences
        creditTexts.add(new CreditText("[LICENCES]", y, true));
        y += 40;
        creditTexts.add(new CreditText("Musique :................", y, false));
        y += 30;
        creditTexts.add(new CreditText("Modèles :................", y, false));
    }

    private void startAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        scrollOffset = 0;
        for (CreditText text : creditTexts) {
            text.alpha = 0;
        }
        animationTimer = new Timer(ANIMATION_DELAY, e -> {
            scrollOffset += SCROLL_SPEED;
            for (CreditText text : creditTexts) {
                text.update();
            }
            repaint();
        });
        animationTimer.start();
    }

    public void resetAnimation() {
        startAnimation();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int centerX = getWidth() / 2;

        for (CreditText credit : creditTexts) {
            int y = credit.y - scrollOffset;
            if (y > -50 && y < getHeight() + 50) {
                g2d.setColor(new Color(1, 1, 1, credit.alpha));
                Font font = new Font("Press Start 2P", Font.PLAIN, credit.isTitle ? 32 : 20);
                g2d.setFont(font);

                String[] lines = credit.text.split("\n");
                for (String line : lines) {
                    int textWidth = g2d.getFontMetrics().stringWidth(line);
                    g2d.drawString(line, centerX - textWidth / 2, y);
                }
            }
        }

        backButton.update();
        backButton.animate();
    }

    public void updateButtons() {
        if (backButton != null) {
            backButton.update();
            backButton.animate();
        }
    }

    public void initialize() {
        initBackButton();
        initCreditTexts();
        startAnimation();
    }

    public void updateComponents() {
        updateButtons();
        repaint();
    }

    public void handleResize() {
        resetAnimation();
    }
}