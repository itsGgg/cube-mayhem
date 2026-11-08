package controller.phases;

import controller.MenuController;

import java.io.IOException;

public class DisposingTerminal {
    private MenuController menuController;

    public DisposingTerminal(MenuController menuController) {
        this.menuController = menuController;
    }

    public void send() throws IOException {
        menuController.setCurrentLevel(menuController.getLevelController().getCurrentLevel());

        if (menuController.getCurrentLevel() == null) {
            menuController.setCurrentLevel(menuController.getLevelController().loadLevel());
        }
        System.out.println("---------------------------------");
        System.out.println(
                "-*- Vous êtes au niveau n°" + menuController.getLevelController().getCurrentLevelNumber() + " -*-");
        System.out.println("--------DEBUT---DISPOSING--------");

        while (menuController.getGameState().isDisposing()) {
            System.out.println("[P] Mettre sur pause la musique");
            System.out.println("[O] Reprendre la musique");
            System.out.println("[a] Afficher le plateau");
            System.out.println("[d] Déposer une tuile");
            System.out.println("[r] Retirer une tuile");
            System.out.println("[R] Redémarrer le niveau");
            System.out.println("[i] S'informer sur une case");
            System.out.println("[s] Mettre la solution du puzzle");
            System.out.println("[f] Finir la phase de programmation");
            System.out.println("[M] Revenir au menu");
            System.out.println("[q] Quitter le jeu");
            System.out.println("---------------------------------");
            System.out.print("Votre action : ");
            menuController.getGameController().handleDisposingInput();
            System.out.println(("---------------------------------"));
            if (!menuController.getGameState().isDisposing())
                break;
        }
        System.out.println("--------FIN---DISPOSING--------");
    }
}
