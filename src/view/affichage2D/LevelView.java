package view.affichage2D;

import model.level.Level;
import model.tools.Constants;

import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.BorderFactory;

import java.io.IOException;
import java.util.HashMap;

public class LevelView extends JPanel {

    private Level level;
    private HashMap<Tuple<Integer, Integer>, CasePanel> casePanel = new HashMap<>();

    public class Tuple<I, J> {
        I i;
        J j;

        Tuple(I i, J j) {
            this.i = i;
            this.j = j;
        }

        Tuple<I, J> getTuple() {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Tuple<?, ?> tuple = (Tuple<?, ?>) o;
            return i.equals(tuple.i) && j.equals(tuple.j);
        }

        @Override
        public int hashCode() {
            return 31 * i.hashCode() + j.hashCode();
        }
    }

    private int height;
    private int width;

    public LevelView(Level level) {
        super();
        this.level = level;

        try {
            showLevel();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        setVisible(true);
    }

    public void updateLevelView(Level level) throws IOException {
        removeAll();
        this.level = level;
        try {
            initialiseGrid();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        revalidate();
        repaint();
    }

    public void showLevel() throws IOException {
        removeAll();
        casePanel.clear(); // Vider la HashMap avant de recréer la grille

        // Configure la grille avec un espacement uniforme
        setLayout(new GridLayout(level.getPlate().getGrid().length,
                level.getPlate().getGrid()[0].length, Constants.Tile.SPACING, Constants.Tile.SPACING));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.BLACK);

        try {
            initialiseGrid();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Force la mise à jour de l'affichage
        revalidate();
        repaint();
    }

    public void initialiseGrid() throws IOException {
        for (int i = 0; i < level.getPlate().getI(); i++) {
            for (int j = 0; j < level.getPlate().getJ(); j++) {
                CasePanel panel = new CasePanel(level.getPlate().getCase(i, j), level.getPlate());
                casePanel.put(new Tuple<>(i, j), panel);
                add(panel);
            }
        }
    }

    public Dimension getTileSize() {
        int rows = level.getPlate().getGrid().length;
        int cols = level.getPlate().getGrid()[0].length;

        width = getWidth() / cols;
        height = getHeight() / rows;
        return new Dimension(width, height);
    }

    public Level getLevel() {
        return level;
    }

    public CasePanel getCasePanel(Integer i, Integer j) {
        return casePanel.get(new Tuple<>(i, j));
    }
}
