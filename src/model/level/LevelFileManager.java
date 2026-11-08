package model.level;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.level.plate.Case;
import model.level.plate.CaseType;
import model.level.plate.Plate;
import model.level.tiles.Tile;
import model.level.tiles.TileDefinition;
import model.level.tiles.TileType;
import model.tools.Constants;
import model.tools.Constants.Path;
import model.tools.Direction;

/**
 * Classe permettant de charger un niveau depuis un fichier texte
 */
public class LevelFileManager {

    /**
     * Méthode permettant de charger un niveau depuis un fichier .txt ainsi que sa
     * solution
     * 
     * @param levelNumber le numéro de niveau à charger
     * @throws FileNotFoundException si le fichier n'existe pas
     */
    public static Plate loadLevel(int levelNumber) throws FileNotFoundException {
        if (levelNumber < 0) {
            throw new IllegalArgumentException("Le numéro du niveau doit être supérieur ou égal à 0."); // A changer si
                                                                                                        // numéro de
                                                                                                        // niveau >0
        }
        try (Scanner sc = new Scanner(new File(Constants.Path.LEVEL + "Level" + levelNumber + ".txt"))) { // Lire un
                                                                                                          // fichier
                                                                                                          // .txt de la
            // forme LevelN.txt, N>=0
            int[] dimensions = null;
            Map<TileType, Integer> inventory = null;
            List<TileDefinition> solution = null;
            List<Case> caseList = null;
            while (sc.hasNextLine()) {
                String currentLine = sc.nextLine().trim();
                if (currentLine.equals("======Dimensions======")) {
                    dimensions = parseDimensions(sc);

                } else if (currentLine.equals("======Plate======")) {
                    caseList = parsePlate(sc);

                } else if (currentLine.equals("======Inventory======")) {
                    inventory = parseInventory(sc);

                } else if (currentLine.equals("======LevelSolution======")) {
                    solution = parseLevelSolution(sc);
                }
            }
            return new Plate(new int[dimensions[0]][dimensions[1]], inventory, solution, caseList);
        }
    }

    /**
     * Méthode permettant de parcourir les dimensions du fichier .txt
     * 
     * @param sc le scanner parcourant le fichier .txt
     * @return les dimensions du plateau sous la forme d'un int[]
     */
    private static int[] parseDimensions(Scanner sc) {
        if (sc.hasNextLine()) {
            String dimensions = sc.nextLine();
            String[] parts = dimensions.split(" ");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Il ne s'agit pas des dimensions d'une supérficie.");
            }
            int[] result = new int[2];
            result[0] = Integer.parseInt(parts[0]);
            result[1] = Integer.parseInt(parts[1]);

            return result;
        }
        return null;
    }

    /**
     * Méthode permettant de parcourir et créer la composition des cases du plateau
     * 
     * @param sc le scanner parcourant le fichier .txt
     * @return une liste des cases composant le plateau
     * @throws IllegalArgumentException si une ligne ne respecte pas le format
     *                                  attendu
     */
    private static List<Case> parsePlate(Scanner sc) {
        List<Case> result = new ArrayList<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            // Rajout d'un délimiteur END pour marquer la fin de lecture de la partie
            // ======Plate======
            if (line.startsWith("END")) {
                break;
            }
            if (!line.startsWith("Case")) {
                throw new IllegalArgumentException(
                        "La ligne : " + line + " n'est pas censé être gérée par parsePlate()");
            }
            String[] parts = line.trim().split(" ");
            // Vérifier si la ligne contient "->" si oui il s'agit d'une boucle
            if (line.contains("->")) {
                result.addAll(manageLoopParsePlate(parts));
            } else {
                // Vérification supplémentaire
                if (parts.length > 10) {
                    throw new IllegalArgumentException("La ligne : " + String.join(" ", parts)
                            + " , n'est pas bien formée. Veuillez séparer les mots par des espaces.");
                }
                int i = Integer.parseInt(parts[4]);
                int j = Integer.parseInt(parts[6]);

                if (parts[2].equals("START")) {
                    Direction direction = Direction.valueOf(parts[8]);
                    Case newCase = new Case(CaseType.PATH, i, j);
                    newCase.setTile(new Tile(TileType.START, direction));
                    result.add(newCase);

                } else if (parts[2].equals("END")) {
                    TileType tileType = TileType.END;
                    Case newCase = new Case(CaseType.PATH, i, j);
                    newCase.setTile(new Tile(tileType));
                    result.add(newCase);

                } else if (parts[2].equals("RESTRICT")) {
                    TileType tileType;
                    if (i % 2 == j % 2)
                        tileType = TileType.WHITE_BLOCKED;
                    else
                        tileType = TileType.GRAY_BLOCKED;
                    Case newCase = new Case(CaseType.RESTRICT, i, j);
                    newCase.setTile(new Tile(tileType));
                    result.add(newCase);

                } else if (parts[2].equals("SWITCH_OFF")) {
                    TileType tileType = TileType.SWITCH_OFF;
                    Case newCase = new Case(CaseType.PATH, i, j);
                    newCase.setTile(new Tile(tileType));
                    result.add(newCase);
                } else if (parts[2].equals("TRANSPARENT")) {
                    TileType tileType = TileType.TRANSPARENT_INVISIBLE;
                    Case newCase = new Case(CaseType.TRANSPARENT, i, j);
                    newCase.setTile(new Tile(tileType));
                    result.add(newCase);
                } else {
                    // Gestion des autres cas
                    CaseType caseType = CaseType.valueOf(parts[2]);
                    Case newCase = new Case(caseType, i, j);
                    result.add(newCase);
                }
            }
        }
        return result;

    }

    /**
     * Méthode permettant gérer les lignes contenant "->" pour créer une liste des
     * cases entre deux points ayant une coordonée en commun
     * 
     * @param parts un tableau de String séparé avec des " "
     * @return une liste des cases se trouvant entre deux points qui ont une
     *         coordonée en commun
     * @throws IllegalArgumentException si la ligne ne contient pas "->" ou les
     *                                  coordonnées sont mal formés
     */
    private static List<Case> manageLoopParsePlate(String[] parts) {
        if (parts.length < 11 || !parts[7].equals("->")) {
            throw new IllegalArgumentException("La ligne ne contient pas un ->. Ligne : " + String.join(" ", parts));
        }
        if (parts[2].equals("START") || parts[2].equals("END")) {
            throw new IllegalArgumentException(" La ligne : " + String.join(" ", parts)
                    + " . contient -> pour une case START/END. Un -> ne peut être utilisé que pour une case PATH/BLOCKED.");
        }

        List<Case> result = new ArrayList<>();

        CaseType caseType = CaseType.valueOf(parts[2]);
        int startI = Integer.parseInt(parts[4]);
        int startJ = Integer.parseInt(parts[6]);
        int endI = Integer.parseInt(parts[8]);
        int endJ = Integer.parseInt(parts[10]);
        if (startI != endI && startJ != endJ) {
            throw new IllegalArgumentException(
                    "Au moins une coordonée de début et respectivement fin doit être la même. Ligne concernée : "
                            + String.join(" ", parts));
        }
        // Utilisation de Math.min pour l'indice de départ et Math.max pour l'indice
        // d'arrivé pour éviter startX>endX
        for (int i = Math.min(startI, endI); i <= Math.max(startI, endI); i++) {
            for (int j = Math.min(startJ, endJ); j <= Math.max(startJ, endJ); j++) {
                Case newCase = new Case(caseType, i, j);
                result.add(newCase);
            }
        }
        return result;
    }

    /**
     * Méthode permettant de récuperer les tuiles et leurs quantités que le joueur
     * posera sur le plateau, depuis le même fichier .txt
     * 
     * @param sc le scanner parcourant le fichier .txt
     * @return la liste des tuiles ainsi que leur quantités
     */
    private static Map<TileType, Integer> parseInventory(Scanner sc) {
        Map<TileType, Integer> inventory = new LinkedHashMap<>();

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.startsWith("END")) {
                break;
            }
            String[] parts = line.split(" ");
            String tileType = parts[0].toUpperCase();
            int quantity = Integer.parseInt(parts[1]);
            inventory.put(createTileTypeFromString(tileType), quantity);
        }
        return inventory;
    }

    /**
     * Méthode permettant de parser la partie solution du fichier .txt
     * 
     * @param sc le scanner parcourant le fichier .txt
     * @return une liste des TileDefinition à partir de laquelle les Tiles sont
     *         crées
     * @throws IllegalArgumentException si la méthode gére une ligne ne commençant
     *                                  pas par "Tile"
     */
    private static List<TileDefinition> parseLevelSolution(Scanner sc) {
        List<TileDefinition> solution = new ArrayList<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (!line.startsWith("Tile")) {
                throw new IllegalArgumentException(
                        "La ligne : " + line + " n'est pas censé être gérée par parseLevelSolution()");
            }
            String[] parts = line.trim().split(" ");
            solution.addAll(createTileDefinition(parts));
        }
        return solution;
    }

    /**
     * Méthode permettant de convertir chaque ligne de la partie solution du fichier
     * .txt en une Liste<TileDefinition> qui sera utilisé pour la création des Tiles
     * sur le plateau
     * 
     * @param parts un tableau de String répresentant une ligne séparé par des " "
     * @return une liste de TileDefinition
     * @throws IllegalArgumentException si le format attendu n'est pas correct
     */
    private static List<TileDefinition> createTileDefinition(String[] parts) {
        List<TileDefinition> result = new ArrayList<>();
        TileType tileType = null;
        Direction direction = null;
        int i = 0, j = 0;

        switch (parts.length) {
            case 8:
                if (!parts[2].equals("SPIN")) {
                    System.out.println(parts[2]);
                    throw new IllegalArgumentException(
                            "La ligne : " + String.join(" ", parts) + " n'est pas formaté correctement.");
                }
                tileType = TileType.valueOf((parts[2]).toUpperCase());
                i = Integer.parseInt(parts[4]);
                j = Integer.parseInt(parts[6]);
                result.add(new TileDefinition(tileType, i, j, null, null));
                break;
            case 10:
                if (!parts[2].equals("ARROW") && !parts[2].equals("SLIDE")) {
                    throw new IllegalArgumentException(
                            "La ligne : " + String.join(" ", parts) + " n'est pas formaté correctement.");
                }
                tileType = TileType.valueOf((parts[2]).toUpperCase());
                i = Integer.parseInt(parts[4]);
                j = Integer.parseInt(parts[6]);
                direction = Direction.valueOf(parts[8].toUpperCase()); // Conversion en majuscules pour les cas où on a
                                                                       // south à la place de SOUTH
                result.add(new TileDefinition(tileType, i, j, direction, null));
                break;
            case 14:
                if (!parts[2].equals("TELEPORTATION") && !parts[8].equals("TELEPORTATION")) {
                    throw new IllegalArgumentException(
                            "La ligne : " + String.join(" ", parts) + " n'est pas formaté correctement.");
                }
                tileType = TileType.valueOf((parts[2]).toUpperCase());
                i = Integer.parseInt(parts[4]);
                j = Integer.parseInt(parts[6]);
                int i2 = Integer.parseInt(parts[10]);
                int j2 = Integer.parseInt(parts[12]);
                TileDefinition def1 = new TileDefinition(tileType, i, j, null, null);
                TileDefinition def2 = new TileDefinition(tileType, i2, j2, null, null);
                def1.setLinkedTile(def2);
                def2.setLinkedTile(def1);
                result.add(def1);
                result.add(def2);
                break;
            default:
                throw new IllegalArgumentException("Format de ligne inconnu : " + String.join(" ", parts));
        }
        return result;
    }

    /**
     * Méthode permettant de créer un type de tuile à partir d'une chaîne de
     * caractères.
     * 
     * @param tile la chaîne de caractères représentant le type de tuile.
     * @return l'objet TileType correspondant à la chaîne de caractères, ou null si
     *         aucun type de tuile ne correspond
     * @throws IllegalArgumentException si la chaîne de caractères ne correspond à
     *                                  aucun type de tuile connu parmis ceux
     *                                  pouvant se trouver dans la solution du
     *                                  plateau.
     */
    private static TileType createTileTypeFromString(String tile) {
        switch (tile) {
            case "ARROW":
                return TileType.ARROW;
            case "SLIDE":
                return TileType.SLIDE;
            case "SPIN":
                return TileType.SPIN;
            case "TELEPORTATION":
                return TileType.TELEPORTATION;
            default:
                throw new IllegalArgumentException(
                        "Le type de tile donné ne fait pas partie de la liste actuelle des tiles.");
        }
    }

    /**
     * Exporte un plateau dans un fichier .txt
     * 
     * @param plate    le plateau à exporter
     * @param filePath le chemin du fichier où le plateau sera sauvegardé
     * @throws IOException si une erreur a lieu lors de l'écriture
     */
    public static void exportLevel(Plate plate, String filePath) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath))) {
            // ======Dimensions======
            exportDimensions(bufferedWriter, plate);

            // ====== Plate ======
            exportPlate(bufferedWriter, plate);

            // ====== Inventory ======
            exportInventory(bufferedWriter, plate);

            // ====== Level Solution ======
            exportLevelSolution(bufferedWriter, plate);

            System.out.println("Niveau sauvegardé avec succès dans : " + filePath);

        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du niveau : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Exporter les dimensions du plateau dans un fichier .txt
     * 
     * @param bufferedWriter le BufferedWriter dans lequel écrire les dimensions
     * @param plate          le plateau contenant les dimensions à exporter
     * @throws IOException
     */
    public static void exportDimensions(BufferedWriter bufferedWriter, Plate plate) throws IOException {
        writeLine(bufferedWriter, "======Dimensions======");
        writeLine(bufferedWriter, plate.getI() + " " + plate.getJ());
    }

    /**
     * Exporte les cases composant le plateau. Utilise "->" pour désigner une plage
     * de cases similaires se trouvant sur une rangée
     * horizontale/verticale
     * 
     * @param bufferedWriter le BufferedWriter dans lequel écrire les informations
     *                       sur les cases
     * @param plate          le plateau à exporter
     * @throws IOException
     */
    public static void exportPlate(BufferedWriter bufferedWriter, Plate plate) throws IOException {
        writeLine(bufferedWriter, "======Plate======");
        Case[][] plateGrid = plate.getGrid();
        boolean[][] visited = new boolean[plate.getI()][plate.getJ()];
        String line = "";
        // Ecrire d'abord les cases START/END
        writeSpecialCases(bufferedWriter, plate, visited);
        // Ecrire les autres cases
        for (int i = 0; i < plate.getI(); i++) {
            for (int j = 0; j < plate.getJ(); j++) {
                // Vérifier que la case n'a pas été visitée
                if (!visited[i][j]) {
                    Case[][] grid = plate.getGrid();
                    Case current = grid[i][j];
                    CaseType caseType = current.getCaseType();
                    TileType tileType = (current.getTile() != null) ? current.getTile().getTileType() : null;

                    // Recuperer la longueur horizontale/verticale
                    int horizontalLength = getHorizontalRange(grid, visited, i, j, caseType);
                    int verticalLength = getVerticalRange(grid, visited, i, j, caseType);

                    // Utiliser "->" pour des rangées >=3
                    if (horizontalLength >= 3 || verticalLength >= 3) {
                        writeRangeSelection(bufferedWriter, plateGrid, visited, grid[i][j], verticalLength,
                                horizontalLength);
                    } else { // Sinon écrire les cases normalement
                        writeSingleCase(bufferedWriter, visited, grid[i][j]);
                    }

                }
            }
        }

        writeLine(bufferedWriter, "END");
    }

    /**
     * Ecrit le format d'une seule case du plateau
     * 
     * @param bufferedWriter le BufferedWriter utilisé
     * @param visited        un tableau des cases visités
     * @param c              la case à écrire dans le fichier
     * @throws IOException lors d'un problème d'écriture dans le fichier
     */
    private static void writeSingleCase(BufferedWriter bufferedWriter,
            boolean[][] visited, Case c) throws IOException {

        String line;
        Tile tile = c.getTile();
        TileType tileType = (tile != null) ? tile.getTileType() : null;

        if (tileType == TileType.WHITE_BLOCKED || tileType == TileType.GRAY_BLOCKED) {
            line = String.format("Case ( RESTRICT , %d , %d )", c.getI(), c.getJ());

        } else if (tileType == TileType.SWITCH_OFF) {
            line = String.format("Case ( SWITCH_OFF , %d , %d )", c.getI(), c.getJ());

        } else if (tileType == TileType.TRANSPARENT_INVISIBLE) {
            line = String.format("Case ( TRANSPARENT , %d , %d )", c.getI(), c.getJ());

        } else {
            line = String.format("Case ( %s , %d , %d )", c.getCaseType().name(), c.getI(), c.getJ());
        }
        writeLine(bufferedWriter, line);
        visited[c.getI()][c.getJ()] = true;
    }

    /**
     * Méthode permettant d'écrire une rangée des cases de même type sur une ligne
     * horizontale/verticale
     * 
     * @param bufferedWriter  le BufferedWriter utilisé
     * @param grid            la grille contenant les cases du plateau
     * @param visited         un tableau des cases visités
     * @param c               la case à partir de la quelle il faut écrire la rangée
     * @param verticalRange   la longueur de la rangeé verticale
     * @param horizontalRange la longueur de la rangée horizontale
     * @throws IOException
     */
    private static void writeRangeSelection(BufferedWriter bufferedWriter, Case[][] grid,
            boolean[][] visited, Case c, int verticalRange, int horizontalRange) throws IOException {
        boolean isHorizontal = horizontalRange >= verticalRange;

        if (isHorizontal) {
            writeHorizontalRange(bufferedWriter, grid, visited, c.getI(), c.getJ(), horizontalRange);
        } else {
            writeVerticalRange(bufferedWriter, grid, visited, c.getI(), c.getJ(), verticalRange);
        }
    }

    /**
     * Méthode permettant d'écrire les cases spéciaux (START/END) dans le fichier
     * 
     * @param bufferedWriter le BufferedWriter utilisé
     * @param plate          le plateau à exporter
     * @param visited        un tableau des cases visités du plateau
     * @throws IOException lors d'un problème d'écriture dans le fichier
     */
    private static void writeSpecialCases(BufferedWriter bufferedWriter, Plate plate, boolean[][] visited)
            throws IOException {
        writeStartCase(bufferedWriter, plate, visited);
        writeEndCase(bufferedWriter, plate, visited);
    }

    /**
     * Méthode permettant d'écrire la case START dans le fichier .txt
     * 
     * @param bufferedWriter le BufferedWriter utilisé
     *                       le plateau à exporter
     * @param visited        un tableau des cases visités du plateau
     * @throws IOException lors d'un problème d'écriture dans le fichier
     */
    private static void writeStartCase(BufferedWriter bufferedWriter, Plate plate, boolean[][] visited)
            throws IOException {
        Case startCase = plate.getStartCase();
        visited[startCase.getI()][startCase.getJ()] = true;
        String line = String.format("Case ( %s , %d , %d , %s )", startCase.getTile().getTileType(), startCase.getI(),
                startCase.getJ(), startCase.getTile().getDirection());
        writeLine(bufferedWriter, line);
    }

    /**
     * Méthode permettant d'écrire la case END dans le fichier .txt
     * 
     * @param bufferedWriter le BufferedWriter utilisé
     *                       le plateau à exporter
     * @param visited        un tableau des cases visités du plateau
     * @throws IOException lors d'un problème d'écriture dans le fichier
     */
    private static void writeEndCase(BufferedWriter bufferedWriter, Plate plate, boolean[][] visited)
            throws IOException {
        Case endCase = plate.getEndCase();
        visited[endCase.getI()][endCase.getJ()] = true;
        String line = String.format("Case ( %s , %d , %d )", endCase.getTile().getTileType(), endCase.getI(),
                endCase.getJ());
        writeLine(bufferedWriter, line);
    }

    private static boolean isCompressible(Case c, CaseType expectedType) {
        if (c.isStart() || c.isEnd() || c.getCaseType() != expectedType)
            return false;

        Tile tile = c.getTile();
        if (tile == null)
            return true;

        TileType tileType = tile.getTileType();
        if (tileType == null)
            return true;

        return switch (tileType) {
            case WHITE_BLOCKED, GRAY_BLOCKED,
                    TRANSPARENT_INVISIBLE, TRANSPARENT_VISIBLE,
                    SWITCH_OFF ->
                false;
            default -> true;
        };
    }

    /**
     * Méthode permettant de calculer la longueur maximale des cases adjacentes
     * du même type se trouvant sur une rangée horizontale.
     * 
     * @param grid    la grille contenant les cases du plateau
     * @param visited une grille booléenne indiquant si une case a été
     *                visitée
     * @param i       la ligne de la case de départ
     * @param j       la colonne de la case de départ
     * @param type    le type de case à vérifier
     * @return la longueur maximale des cases adjacentes du même type sur la rangée
     *         horizontale
     */
    private static int getHorizontalRange(Case[][] grid, boolean[][] visited, int i, int j, CaseType type) {
        int length = 0;
        while (j + length < grid[0].length &&
                !visited[i][j + length] &&
                isCompressible(grid[i][j + length], type)) {
            length++;
        }
        return length;
    }

    /**
     * Écrit une plage horizontale de cases dans un fichier via un BufferedWriter
     *
     * @param bufferedWriter le BufferedWriter utilisé
     * @param grid           la grille contenant les objets Case
     * @param visited        une grille booléenne indiquant si une case a été
     *                       visitée
     * @param i              la coordonnée i de la ligne horizontale à écrire
     * @param startJ         la coordonnée j de début de la plage horizontale
     * @param length         la longueur de la plage horizontale
     *                       {@link #getHorizontalRange}
     * @throws IOException lors d'un problème d'écriture dans le fichier
     */
    private static void writeHorizontalRange(BufferedWriter bufferedWriter, Case[][] grid, boolean[][] visited, int i,
            int startJ,
            int length) throws IOException {
        int endJ = startJ + length - 1;
        String line = String.format("Case ( %s , %d , %d -> %d , %d )",
                grid[i][startJ].getCaseType().name(), i, startJ, i, endJ);

        for (int j = startJ; j <= endJ; j++)
            visited[i][j] = true;
        writeLine(bufferedWriter, line);
    }

    /**
     * Methode permettant de récuperer la longueur maximale des cases du même type
     * se trouvant sur une rangée verticale
     * 
     * @param grid    la grille contenant les cases du plateau
     * @param visited une grille booléenne indiquant si une case a été
     *                visitée
     * @param i       la coordonnée verticale (ligne) de la case
     *                de départ
     * @param j       la coordonnée horizontale (colonne) de la case de départ
     * @param type    le type de case à vérifier
     * @return la longueur maximale des cases adjacentes du même type sur la rangée
     *         verticale
     */
    private static int getVerticalRange(Case[][] grid, boolean[][] visited, int i, int j, CaseType type) {
        int length = 0;
        while (i + length < grid.length &&
                !visited[i + length][j] &&
                isCompressible(grid[i + length][j], type)) {
            length++;
        }
        return length;
    }

    /**
     * Écrit une plage verticale de cases dans un fichier via un BufferedWriter
     * 
     * @param bufferedWriter le BufferedWriter utilisé pour écrire dans le fichier
     * @param grid           la grille contenant les objets Case
     * @param visited        une grille booléenne indiquant si une case a déjà été
     *                       visitée
     * @param startI         la coordonnée verticale (ligne) de début de la plage
     * @param j              la coordonnée horizontale (colonne) de la plage
     *                       verticale à écrire
     * @param length         la longueur de la plage verticale obtenue via
     *                       {@link #getVerticalRange}.
     * @throws IOException si une erreur survient lors de l'écriture dans le
     *                     fichier
     */
    private static void writeVerticalRange(BufferedWriter bufferedWriter, Case[][] grid, boolean[][] visited,
            int startI, int j,
            int length) throws IOException {
        int endI = startI + length - 1;
        String line = String.format("Case ( %s , %d , %d -> %d , %d )",
                grid[startI][j].getCaseType().name(), startI, j, endI, j);

        for (int i = startI; i <= endI; i++)
            visited[i][j] = true;
        writeLine(bufferedWriter, line);
    }

    /**
     * Exporte l'inventaire du niveau dans un fichier .txt
     * 
     * @param bufferedWriter le BufferedWriter utilisé
     * @param plate          le plateau contenant l'inventaire à exporter
     * @throws IOException
     */
    public static void exportInventory(BufferedWriter bufferedWriter, Plate plate) throws IOException {
        writeLine(bufferedWriter, "======Inventory======");
        Map<TileType, Integer> inventoryMap = new LinkedHashMap<>();
        for (int i = 0; i < plate.getI(); i++) {
            for (int j = 0; j < plate.getJ(); j++) {
                Case currentCase = plate.getCase(i, j);
                // Si la case contient une tuile, ajouter au compte
                if (!currentCase.isEmpty()) {
                    Tile tile = currentCase.getTile(); // Obtenir le type de la tuile (ex: ARROW, BLOCKED, etc.)
                    if (tile != null) {
                        TileType tileType = tile.getTileType(); // Récupérer le type de la tuile (TileType)

                        // Exclure les tuiles de type START et END
                        if (tileType != TileType.START && tileType != TileType.END && tileType != TileType.WHITE &&
                                tileType != TileType.WHITE_BLOCKED && tileType != TileType.GRAY
                                && tileType != TileType.GRAY_BLOCKED && tileType != TileType.SWITCH_OFF
                                && tileType != TileType.TRANSPARENT_INVISIBLE) {
                            // Ajouter ou mettre à jour le nombre de tuiles dans la map
                            inventoryMap.put(tileType, inventoryMap.getOrDefault(tileType, 0) + 1);
                        }
                    }
                }
            }
        }
        if (inventoryMap.size() != 0) {
            for (Map.Entry<TileType, Integer> entry : inventoryMap.entrySet()) {
                writeLine(bufferedWriter, entry.getKey().name() + " " + entry.getValue());
            }
        } else { // Si aucune tuile n'a été posée, récuperer celles tilesAvailables de la classe
                 // Plate
            for (Map.Entry<TileType, Integer> entry : plate.getTilesAvailable().entrySet()) {
                writeLine(bufferedWriter, entry.getKey().name() + " " + entry.getValue());
            }
        }
        writeLine(bufferedWriter, "END");
    }

    /**
     * Permet d'exporter la solution d'un plateau
     * 
     * @param bufferedWriter le BufferedWriter utilisé
     * @param plate          le plateau contenant les dimensions à exporter
     * @throws IOException
     */
    public static void exportLevelSolution(BufferedWriter bufferedWriter, Plate plate) throws IOException {
        writeLine(bufferedWriter, "======LevelSolution======");
        // Parcours la liste des tuiles et les rajoute dans le fichier
        List<TileDefinition> plateSolution = plate.getPlateSolution();
        int size = plateSolution.size();
        for (int i = 0; i < size; i++) {
            TileDefinition tileDefinition = plateSolution.get(i);
            String line;
            // Gere le cas des tuiles directionnelles
            if (tileDefinition.isDirectional()) {
                if (tileDefinition.isTeleportation()) {
                    throw new IllegalArgumentException("Une tuile directionelle ne peut pas être teleportatrice");
                }
                line = "Tile ( " + tileDefinition.getBasicInfo() + ", " + tileDefinition.getDirection() + " )";
                // Gere le cas des tuiles teleportation
            } else if (tileDefinition.isTeleportation()) {
                if (tileDefinition.isDirectional() || tileDefinition.getLinkedTile().isDirectional()) {
                    throw new IllegalArgumentException(
                            "Une tuile teleportatrice ne peut pas être directionelle." + tileDefinition.toString());
                }
                line = "Tile ( " + tileDefinition.getBasicInfo() + ", "
                        + tileDefinition.getLinkedTile().getBasicInfo() + ")";
                i++;
                // Gere les autres types de tuiles
            } else {
                if (tileDefinition.isDirectional() || tileDefinition.isTeleportation()) {
                    throw new IllegalArgumentException("La tuile " + tileDefinition.toString()
                            + " ne doit pas être de type Direction/Teleportation");
                }
                line = "Tile ( " + tileDefinition.getBasicInfo() + ")";
            }
            if (i == size - 1) {
                bufferedWriter.write(line);
            } else {
                writeLine(bufferedWriter, line);
            }
        }
    }

    /**
     * Permet de écrire écrire une ligne dans un fichier donné et rajouter le retour
     * à la ligne
     * 
     * @param bufferedWriter le BufferedWriter utilisé
     * @param line           la ligne à écrire
     * @throws IOException
     */
    public static void writeLine(BufferedWriter bufferedWriter, String line) throws IOException {
        if (bufferedWriter == null) {
            throw new IllegalArgumentException("BufferedWriter ne peut pas être null");
        }
        bufferedWriter.write(line);
        bufferedWriter.newLine();
    }

    /**
     * Méthode permettant de configurer le JFileChooser pour la selection du
     * fichier
     * 
     * @return JFileChooser
     */
    public static JFileChooser configureFileChooser(String directory, String fileChooserTitle, String extension,
            String fileChooserDescription) {
        JFileChooser fileChooser = new JFileChooser(directory);
        fileChooser.setDialogTitle(fileChooserTitle);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(fileChooserDescription, extension);
        fileChooser.setFileFilter(filter);

        return fileChooser;
    }

    /**
     * Initialise les répertoires nécessaires pour le progres du joueur et l'éditeur
     * de niveaux
     */
    public static void initializeDirectories() {
        new File(Path.GAME_PROGRESS_DIRECTORY).mkdirs();
        new File(Path.EDITOR_LEVELS_DIRECTORY).mkdirs();
    }

    public static Plate loadLevelFromFile(String filePath) throws FileNotFoundException {
        try (Scanner sc = new Scanner(new File(filePath))) {
            int[] dimensions = null;
            Map<TileType, Integer> inventory = null;
            List<TileDefinition> solution = null;
            List<Case> caseList = null;

            while (sc.hasNextLine()) {
                String currentLine = sc.nextLine().trim();
                if (currentLine.equals("======Dimensions======")) {
                    dimensions = parseDimensions(sc);
                } else if (currentLine.equals("======Plate======")) {
                    caseList = parsePlate(sc);
                } else if (currentLine.equals("======Inventory======")) {
                    inventory = parseInventory(sc);
                } else if (currentLine.equals("======LevelSolution======")) {
                    solution = parseLevelSolution(sc);
                }
            }

            return new Plate(new int[dimensions[0]][dimensions[1]], inventory, solution, caseList);
        }
    }

}
