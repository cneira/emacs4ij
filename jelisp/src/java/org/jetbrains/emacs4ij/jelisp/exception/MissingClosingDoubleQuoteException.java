package org.jetbrains.emacs4ij.jelisp.exception;

import org.jetbrains.emacs4ij.jelisp.JelispBundle;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 7/12/11
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class MissingClosingDoubleQuoteException extends LispException {
    public MissingClosingDoubleQuoteException () {
        super(JelispBundle.message("missing.double.quote"));
    }
}
