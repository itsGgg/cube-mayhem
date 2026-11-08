package controller.phases;

import controller.MenuController;

public class ExecutingTerminal {
    private MenuController menuController;

    public ExecutingTerminal(MenuController menuController) {
        this.menuController = menuController;
    }

    public void send() {
        System.out.println("--------DEBUT---EXECUTING--------");
        while(this.menuController.getGameState().isExecuting()) {
            System.out.println("[P] Mettre sur pause la musique");
            System.out.println("[O] Reprendre la musique");
            System.out.println("[a] Avancer pas à pas");
            System.out.println("[A] Avancer");
            System.out.println("[e] Etat du cube");
            System.out.println("[f] Finir la phase d'exécution");
            System.out.println("[q] Quitter le jeu");
            System.out.println("[M] Revenir au menu");
            System.out.println("[R] Redémarrer le niveau");
            System.out.println(("---------------------------------"));
            System.out.print("Votre action : ");
            menuController.getGameController().handleExecutingInput();
            System.out.println(("---------------------------------"));
            if(menuController.getCurrentLevel().isDone()) break;
        }
        System.out.println("--------FIN---EXECUTING--------");
    }
}
