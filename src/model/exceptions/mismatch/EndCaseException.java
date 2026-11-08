package model.exceptions.mismatch;

import model.exceptions.MismatchCaseException;

public class EndCaseException extends MismatchCaseException {

    public EndCaseException() {
        super("Il doit y avoir exactement 1 case de fin !");
    }
}
