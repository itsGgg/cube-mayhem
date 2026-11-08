package model.level.plate;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import model.level.Cube;
import model.level.Level;
import model.level.GameState.DisplayMode;
import model.level.tiles.Tile;
import model.level.tiles.TileDefinition;
import model.level.tiles.TileType;
import model.tools.Direction;

public class AStarSolver {

    private Plate plate;
    private static final int MAX_DEPTH = 100000;

    public AStarSolver(Plate plate) {
        this.plate = plate;
    }

    /**
     * Classe interne répresentant un noeud
     * Chaque noeud simule un chemin alternatif du cube sur le même plateau partagé
     */
    private class Node implements Comparable<Node> {
        private Map<TileType, Integer> personalInventory;
        private List<TileDefinition> solutionUsed;
        private float hScore, gScore, fScore; // stocke respectivement h(x),g(x),f(x);
                                              // f(x)=g(x)+h(x)
        // f(n) designé la priorité du noeud dans la file
        // h(n)= calcul de l'heuristique : distance de Manhattant + .. ?
        private Cube cube; // chaque noeud a un cube dont il fait une copie profonde
        private Node parentNode; // le noeud parent
        private Deque<String> lastPositions = new ArrayDeque<>();

        @Override
        public int compareTo(Node other) {
            return Float.compare(this.fScore, other.fScore);
        }
    }

    /**
     * Calcul d'une heuristique h(x) pour l'algorithme.
     * Utilise la distance de Manhattan
     * 
     * @param node
     * @return
     */
    private int calculateHeuristic(Node node) {
        Case endCase = plate.getEndCase();
        int manhattanDistance = Math.abs(node.cube.getI() - endCase.getI()) +
                Math.abs(node.cube.getJ() - endCase.getJ());

        return manhattanDistance;
    }

    /**
     * Implementation de g(x)
     * +1 pour chaque case parcourue
     * 
     * @param node
     * @return
     */
    private float calculateGScore(Node node) {
        return node.parentNode != null ? node.parentNode.gScore + 1 : 0;
    }

    /**
     * Une méthode de hachage de la solution échouante, pour éviter des solutions
     * rebondantes
     * 
     * @param node
     * @return
     */
    private String generateStateKey(Node node) {
        StringBuilder key = new StringBuilder();

        // Représenter les tuiles placées
        for (TileDefinition tile : node.solutionUsed) {
            key.append(tile.getI())
                    .append(':')
                    .append(tile.getJ())
                    .append(':')
                    .append(tile.getType())
                    .append(':')
                    .append(tile.getDirection())
                    .append(':')
                    .append(tile.getLinkedTile() != null ? tile.getLinkedTile().hashCode() : "null")
                    .append('|');
        }

        // Position et direction du cube
        key.append("CUBE:")
                .append(node.cube.getI()).append(',')
                .append(node.cube.getJ()).append(',')
                .append(node.cube.getFacingDirection()).append('|');

        // Inventaire
        key.append("INV:");
        node.personalInventory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> key.append(entry.getKey())
                        .append('=')
                        .append(entry.getValue())
                        .append(','));

        // Tuile active sous le cube (si existante)
        Case currentCase = plate.getCaseOrNull(node.cube.getI(), node.cube.getJ());
        if (currentCase != null) { // Vérifier si la case existe
            Tile tile = currentCase.getTile();
            if (tile != null) {
                key.append("|TILE:")
                        .append(tile.getTileType())
                        .append(':')
                        .append(tile.getDirection());
            }
        }

        return key.toString();
    }

    /**
     * Initialise le premier noeud de l'algorithme
     * Copie entière de cube, inventaire
     * 
     * @return
     */
    private Node initialiseFirstNode() {
        Node firstNode = new Node();
        firstNode.personalInventory = new HashMap<>(plate.getTilesAvailable());
        firstNode.cube = new Cube(plate, plate.getStartCase());
        firstNode.solutionUsed = new ArrayList<>();
        firstNode.hScore = calculateHeuristic(firstNode);
        firstNode.gScore = 0;
        firstNode.fScore = firstNode.gScore + firstNode.hScore;
        firstNode.parentNode = null;
        return firstNode;
    }

    /**
     * Renvoie une case virtuelle d'un noeud des indices i,j
     * 
     * @param current
     * @param i
     * @param j
     * @return
     */
    private Case getVirtualCase(Node current, int i, int j) {
        Case original = plate.getCase(i, j);
        Case virtualCase = new Case(original);

        // Vérifier si une tuile a été placée à cette position dans la solution courante
        TileDefinition tileDef = getTileAt(current.solutionUsed, i, j);
        if (tileDef != null) {
            virtualCase.setTile(new Tile(tileDef.getType(), tileDef.getDirection()));
            System.out.println("Case virtuelle (" + i + "," + j + ") - Tuile trouvée: " + tileDef.getType() + " "
                    + tileDef.getDirection());
        } else {
            System.out
                    .println("Case virtuelle (" + i + "," + j + ") - Aucune tuile trouvée. Libre de poser une tuile !");
        }

        return virtualCase;
    }

    /**
     * Recherche et renvoie une tuile à la position i,j dans la liste des tuiles
     * utilisées
     * 
     * @param solutionUsed liste des tuiles utilisés
     * @param i            cordonnée i de la position recherchée
     * @param j            coordonnée j de la position recherchée
     * @return la tuile de position i,j, ou null si aucune tuile n'est présente
     */
    private TileDefinition getTileAt(List<TileDefinition> solutionUsed, int i, int j) {
        for (TileDefinition tileDefinition : solutionUsed) {
            if (tileDefinition.getI() == i && tileDefinition.getJ() == j) {
                return tileDefinition;
            }
        }
        return null;
    }

    /**
     * Méthode permettant de renvoyer un noeud null
     * 
     * @param parent
     * @param nextCase
     * @return
     */
    private Node generateNullNode(Node parent, int newI, int newJ) {
        Node outOfBoundsSuccessor = new Node();
        outOfBoundsSuccessor.parentNode = parent;
        outOfBoundsSuccessor.cube = new Cube(parent.cube, parent.cube.getPlate());
        outOfBoundsSuccessor.cube.setI(newI);
        outOfBoundsSuccessor.cube.setJ(newJ);

        // Récuperer les autres proprietés
        outOfBoundsSuccessor.personalInventory = new HashMap<>(parent.personalInventory);
        outOfBoundsSuccessor.solutionUsed = new ArrayList<>(parent.solutionUsed);

        // Pénalisation
        outOfBoundsSuccessor.gScore = calculateGScore(outOfBoundsSuccessor) * 2;
        outOfBoundsSuccessor.hScore = calculateHeuristic(outOfBoundsSuccessor) * 2;
        outOfBoundsSuccessor.fScore = outOfBoundsSuccessor.gScore + outOfBoundsSuccessor.hScore;

        return outOfBoundsSuccessor;
    }

    /**
     * Implémente la géneration des noeuds successeurs du noeud actuel
     * 
     * @return
     */
    private List<Node> generateSuccessors(Node current) {
        List<Node> successors = new ArrayList<>();
        // Directions possibles: Est, Sud, Ouest, Nord
        int[][] directions = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

        for (int d = 0; d < directions.length; d++) {
            int newI = current.cube.getI() + directions[d][0];
            int newJ = current.cube.getJ() + directions[d][1];
            // Vérifier si la position est valide
            if (!plate.checkI(newI) || !plate.checkJ(newJ)) {
                // Créer un le successeur mais le pénaliser
                // Node outOfBoundsSuccessor = generateNullNode(current, newI, newJ);
                // successors.add(outOfBoundsSuccessor);
                continue;
            }

            Case nextCase = plate.getCase(newI, newJ);
            // Vérifier si la case est un chemin valide
            if (!nextCase.isPath() && !nextCase.isStart() && !nextCase.isEnd() || nextCase.isBlocked())
                continue;

            // Si la case est vide, on peut y placer une tuile ou simplement se déplacer
            if (nextCase.isEmpty()) {
                // Vérifier si une tuile a déjà été placée dans la solution courante
                if (getTileAt(current.solutionUsed, newI, newJ) != null) {
                    continue; // Case déjà occupée dans la solution
                }

                // Option 1 - Déplacement sans poser de tuile
                Node successorWithoutTile = createMovement(current, nextCase, null);
                if (successorWithoutTile == null) {
                    // Debug
                    // System.out.println("Successeur sans tuile rejeté pour la case (" + newI + ","
                    // + newJ + ")");
                    continue;
                }

                successors.add(successorWithoutTile);
                // Debug
                // System.out.println("Successeur sans tuile ajouté pour la case (" + newI + ","
                // + newJ + ")");

                // Option 2: Explorer chaque tuile disponible dans l'inventaire
                for (Map.Entry<TileType, Integer> entry : current.personalInventory.entrySet()) {
                    TileType tileType = entry.getKey();
                    Integer quantity = entry.getValue();
                    // Vérifier disponibilité et validité de la tuile
                    if (quantity > 0 && tileType != TileType.START && tileType != TileType.END) {
                        // Pour chaque direction possible de la tuile
                        for (Direction tileDirection : Direction.values()) {
                            TileDefinition tileDefinition = new TileDefinition(tileType, nextCase.getI(),
                                    nextCase.getJ(), tileDirection, null);
                            // Créer un nouveau nœud
                            Node successorWithTile = createMovement(current, nextCase, tileDefinition);
                            if (successorWithTile == null) {
                                continue;
                            }
                            successors.add(successorWithTile);
                        }
                    }
                }
            } else if (nextCase.isEnd()) {
                // Créer un successeur spécial pour la case finale
                Node endSuccessor = createMovement(current, nextCase, null);
                if (endSuccessor == null) {
                    continue;
                }
                successors.add(endSuccessor);
            } else {
                Node successor = createMovement(current, nextCase, null);
                // Si null, on ignore ce noeud
                if (successor == null) {
                    continue;
                }
                successors.add(successor);
            }
        }

        // Éviter les cycles courts type A→B→A
        Deque<String> recent = current.lastPositions;
        Set<String> recentSet = new HashSet<>(recent);
        if (recent.size() != recentSet.size()) {
            // Au moins une position repetée
            System.out.println("Cycle détecté, on ignore ce noeud.");
            return new ArrayList<>();
        }

        return successors;
    }

    /**
     * Méthode permettant de créer un noeud successeur.
     * 
     * @param parent         le noeud parent à partir duquel on crée le successeur
     * @param physicalCase   la case sur laquelle le cube va se déplacer
     * @param tileDefinition la tuile à placer, null si aucune
     * @return
     */
    private Node createMovement(Node parent, Case physicalCase, TileDefinition tileDefinition) {
        if (physicalCase.isBlocked()) {
            return null;
        }

        Node successor = new Node();
        // Copie des élements du noeud parent
        successor.parentNode = parent;
        successor.lastPositions = new ArrayDeque<>(parent.lastPositions);
        successor.personalInventory = new HashMap<>(parent.personalInventory);
        successor.solutionUsed = new ArrayList<>(parent.solutionUsed);

        // Taille max : 4 positions
        if (successor.lastPositions.size() > 4) {
            successor.lastPositions.removeFirst(); // Supprime l'ancienne
        }

        // Copie du cube parent
        successor.cube = new Cube(parent.cube, parent.cube.getPlate());
        // Création d'une case virtuelle
        Case virtualCase = getVirtualCase(parent, physicalCase.getI(), physicalCase.getJ());

        // Si une nouvelle tuile est fournie, l'appliquer à la case virtuelle si celle
        // ci ne contient pas de tuile
        if (tileDefinition != null) {
            // Vérifier si une tuile existe déjà à cette position
            TileDefinition existingTile = getTileAt(parent.solutionUsed, physicalCase.getI(), physicalCase.getJ());
            if (existingTile != null) {
                return null;
            }

            virtualCase.setTile(new Tile(tileDefinition.getType(), tileDefinition.getDirection()));
            int currentQuantity = successor.personalInventory.get(tileDefinition.getType());
            successor.personalInventory.put(tileDefinition.getType(), currentQuantity - 1);
            successor.solutionUsed.add(tileDefinition);
        }

        String currentPosition = successor.cube.getI() + "," + successor.cube.getJ();
        successor.lastPositions.addLast(currentPosition);

        // Cas où une slide peut renvoyer le cube sur une case Blocked
        boolean isValidSlide = simulateSlide(successor.cube, physicalCase);
        if (isValidSlide == false) {
            return null;
        }
        // Utiliser la case virtuelle pour le déplacement
        successor.cube.setCase(virtualCase);

        // Calcul des scores
        successor.gScore = calculateGScore(successor);
        if (successor.gScore > 100)
            return null;
        successor.hScore = calculateHeuristic(successor);
        successor.fScore = successor.gScore + successor.hScore;

        System.out.println("Création d'un successeur réussie pour la case (" +
                physicalCase.getI() + "," + physicalCase.getJ() + ") avec tuile: " +
                (tileDefinition != null ? tileDefinition.getType() : "aucune"));
        return successor;
    }

    /**
     * Methode permettant de simuler les tuiles de type slide
     * 
     * @param cube
     * @param startCase
     * @return
     */
    private boolean simulateSlide(Cube cube, Case startCase) {
        Case current = startCase;
        Direction dir = cube.getFacingDirection();
        while (true) {
            // Calcule les coord de la prochaine case selon la direction
            int nextI = current.getI() + dir.deltaI();
            int nextJ = current.getJ() + dir.deltaJ();
            // Vérifier que la prochaine pos est dans les limites du plateau
            if (!plate.checkI(nextI) || !plate.checkJ(nextJ))
                return false;
            Case nextCase = plate.getCase(nextI, nextJ);
            // Vérification que la prochaine case n'est pas de type Blocked
            if (nextCase.isBlocked())
                return false;
            // Sinon, poser le cube sur cette case
            cube.setCase(nextCase);
            current = nextCase;
            Tile tile = current.getTile();
            if (tile == null || tile.getTileType() != TileType.SLIDE)
                break;
            dir = tile.getDirection();
            cube.setFacingDirection(dir);
        }
        return true;
    }

    /**
     * Resout le plateau avec l'algorithme A*
     * 
     * @param plate
     */
    public List<TileDefinition> solveWithAStar(Plate plate) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();
        Case endCase = plate.getEndCase();

        // Initialisation avec le nœud de départ
        Node startNode = initialiseFirstNode();
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // Vérifier si la profondeur maximale est atteinte
            if (current.gScore > MAX_DEPTH) {
                System.out.println("Profondeur maximale atteinte pour ce noeud, on l'ignore");
                continue;
            }
            System.out.printf("Exploration: (%d,%d) Dir=%s G=%.1f H=%.1f%n",
                    current.cube.getI(), current.cube.getJ(),
                    current.cube.getFacingDirection(),
                    current.gScore, current.hScore);

            // Génération d'une clé d'état unique
            String stateKey = generateStateKey(current);
            System.out.println("Dernières positions : " + current.lastPositions);

            // Si déjà visité, passer au noeud suivant
            if (closedSet.contains(stateKey)) {
                continue;
            }

            // Marquer comme visité
            closedSet.add(stateKey);
            System.out.println("[STOCKED NODE]" + stateKey);

            // Vérifier si le cube est hors du plateau
            if (!plate.checkI(current.cube.getI()) || !plate.checkJ(current.cube.getJ())) {
                System.out.println("Cube est sorti du plateau - état rejeté !");
                // Ne pas générer de successeurs pour ce noeud, mais le marquer comme visité
                continue;
            }

            // Vérifier si l'objectif est atteint
            if (current.cube.getI() == endCase.getI() &&
                    current.cube.getJ() == endCase.getJ()) {
                System.out.println("Solution trouvée ");
                applyNodeSolution(current);
                return current.solutionUsed;
            } else {
                System.out.println("Position actuelle cube: " + current.cube.getI() + " j : " + current.cube.getJ());
            }

            // Générer et explorer les successeurs
            List<Node> successors = generateSuccessors(current);
            for (Node successor : successors) {
                String successorStateKey = generateStateKey(successor);
                if (!closedSet.contains(successorStateKey)) {
                    openSet.add(successor);
                }
            }
        }

        // Aucune solution trouvée
        return null;
    }

    /**
     * Applique la solution d'un noeud au plateau de jeu
     * 
     * @param current
     */
    private void applyNodeSolution(Node current) {
        // Placer chaque tuile de la solution sur le plateau
        for (TileDefinition tileDef : current.solutionUsed) {
            plate.place(tileDef.getType(), tileDef.getI(), tileDef.getJ(), tileDef.getDirection(), null);
        }
    }

    /**
     * Méthode permettant de tester l'algorithme.
     * Des logs concernant la sortie standard et erreur standard sont stockés dans
     * un fichier "log.txt"
     * pour un eventuel debuggage
     * 
     * @param levelNumber
     */
    public static void testAlgorithm(int levelNumber) {
        try {
            PrintStream fichierLog = new PrintStream("log.txt");
            System.setOut(fichierLog); // Redirige System.out
            System.setErr(fichierLog); // Redirige aussi System.err

            Level one = new Level(levelNumber);
            Plate plate = one.getPlate();
            AStarSolver solver = new AStarSolver(plate);
            plate.clearBoard();
            plate.printMap(DisplayMode.TERMINAL);
            System.out.println("-------------------");
            List<TileDefinition> sol = solver.solveWithAStar(plate);
            if (sol == null) {
                System.out.println("Aucune solution trouvé.");
            } else {
                for (TileDefinition tileDefinition : sol) {
                    System.out.println(tileDefinition);
                }
                System.out.println(sol.size());
                while (!plate.getCube().isArrived()) {
                    plate.printMap(DisplayMode.TERMINAL);
                    if (!plate.getCube().moveCube()) {
                        break;
                    }
                }
                plate.printMap(DisplayMode.TERMINAL);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}