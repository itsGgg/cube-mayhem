# CubeMayhem

**Cube Mayhem** est un jeu de réflexion dans lequel le joueur doit guider un cube jusqu’à la sortie, en le déplaçant sur une grille.

Le joueur ne contrôle pas le cube directement. A la place, **il place des tuiles d'instruction sur la grille**
(comme _tourner à droite_, _avancer_, _se téléporter_, etc.) pour que le cube, une fois lancé, suive automatiquement le bon chemin tout seul.

Le défi du puzzle : **trouver la bonne combinaison de tuiles**, placées aux bons endroits, pour que le cube atteigne la sortie
sans se bloquer ou sortir de la grille.

C’est un jeu qui fait appel à la logique, à la planification et à la patience.

## Equipe

- Adem B. M.
- [@itsGgg](https://github.com/itsGgg) 
- Borine-Thibault H.
- Jakub K.
- Lilian L.

## Lancement du projet

### Prérequis

- Un JDK Java 17 ou version supérieure (`javac` et `java` doivent être accessibles dans le terminal)
- Git (ou télécharger le code source au format `.zip`)

### Etapes pour exécuter le projet

1. **Récupérer le projet** dans un dossier `projet-cubemayhem` :

   ```bash
   git clone https://github.com/itsGgg/cube-mayhem.git projet-cubemayhem
   cd projet-cubemayhem
   ```

   (ou décompresser l'archive `.zip`)

2. **Compiler et lancer le jeu**, depuis la racine du projet (`projet-cubemayhem`), avec l'une des méthodes suivantes :

#### Méthode 1 : scripts fournis (le plus simple)

Ces scripts compilent puis lancent le jeu automatiquement :

- Windows : double-cliquer sur [`runWindows.bat`](runWindows.bat)
- Linux/macOS : exécuter [`runLinuxMacOS.sh`](runLinuxMacOS.sh) (`./runLinuxMacOS.sh` dans un terminal)

#### Méthode 2 : compilation et exécution manuelles

```bash
# Compilation de tous les fichiers .java du projet (génère les .class dans out/)
find src -name "*.java" > sources.txt && javac -d out -cp src @sources.txt && rm sources.txt

# Exécution
java -cp out Main
```

#### Méthode 3 : depuis un IDE (IntelliJ IDEA, VSCode, etc.)

1. Ouvrir le dossier `projet-cubemayhem` dans l'IDE.
2. Vérifier que le SDK Java configuré est en version 17+.
3. Ouvrir `src/Main.java` et lancer `Main.main` (▶ ou `Shift + F10` sous IntelliJ).

> ⚠️ Le jeu charge ses ressources (sprites, niveaux dans [Levels](Levels), sons...) via des chemins relatifs à la racine du projet. Il faut donc toujours lancer la commande `java` (ou les scripts) **depuis la racine de `projet-cubemayhem`**, pas depuis `src/`.

## Comment jouer

(Mode terminal)

Le jeu peut aussi se jouer entièrement au clavier dans la console (sans interface graphique) : des touches permettent de poser (`d`) ou retirer (`r`) une tuile, afficher la grille (`a`), lancer l'exécution (`f`), afficher la solution (`s`), etc.

(Modes 2D et 3D)

- **Clic gauche** sur une case pour y poser la tuile sélectionnée dans l'inventaire (en bas de l'écran), ou pour faire pivoter une tuile directionnelle déjà posée.
- **Clic droit** sur une tuile posée pour la retirer et la récupérer dans l'inventaire.
- Une fois le chemin prêt, lancer l'exécution pour voir le cube suivre le chemin posé.
- Les boutons de contrôle en jeu permettent de retourner au menu, redémarrer le niveau, ou revoir la solution du niveau.

Un éditeur de niveaux est également disponible depuis le menu principal pour créer ses propres niveaux.
