package model.exceptions.mismatch;

import model.exceptions.MismatchCaseException;

public class StartCaseException extends MismatchCaseException {

    public StartCaseException() {
        super("Il doit y avoir exactement 1 case de début !");
    }
}
