package org.jetbrains.emacs4ij.jelisp.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 7/11/11
 * Time: 6:28 PM
 * To change this template use File | Settings | File Templates.
 *
 * this is the base elisp exception
 */
public class LispException extends Exception {
    //TODO: store the position where the exception raised

    protected StringBuilder myStackTrace;
   // protected int myPosition;

    public LispException () {
        super ("Unknown exception");
    }

    public LispException (String message) { //}, int position) {
        super(message);
        //myPosition = position;
    }

    public LispException(String message, StringBuilder stackTrace) {
        super(message);
        myStackTrace = stackTrace;
    }

    public StringBuilder getMyStackTrace () {
        return myStackTrace;
    }

    @Override
    public String toString() {
        return "LispException{}";
    }
}