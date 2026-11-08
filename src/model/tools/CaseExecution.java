package model.tools;

import model.level.plate.Case;

@FunctionalInterface
public interface CaseExecution {
    /**
     *  Executer une action sur la case ciblée.
     * @param targetCase
     * @return Retourne true si l'exécution s'est bien déroulée.
     */
    boolean execute(Case targetCase);
}