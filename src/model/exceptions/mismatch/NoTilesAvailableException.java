package model.exceptions.mismatch;

import model.exceptions.MismatchCaseException;

public class NoTilesAvailableException extends MismatchCaseException {

    public NoTilesAvailableException() {
        super("Il n'y a plus de tuiles disponibles !");
    }
}
