package org.jetbrains.emacs4ij.jelisp.exception;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 3/30/12
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class EndOfFileException extends LispException {
    public EndOfFileException () {
        super("End of file during parsing");
    }
}