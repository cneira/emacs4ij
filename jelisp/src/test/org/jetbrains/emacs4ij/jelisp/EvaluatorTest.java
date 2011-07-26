package org.jetbrains.emacs4ij.jelisp;

import junit.framework.Assert;
import org.jetbrains.emacs4ij.jelisp.elisp.*;
import org.jetbrains.emacs4ij.jelisp.exception.InvalidFunctionException;
import org.jetbrains.emacs4ij.jelisp.exception.LispException;
import org.jetbrains.emacs4ij.jelisp.exception.VoidVariableException;
import org.jetbrains.emacs4ij.jelisp.exception.WrongNumberOfArgumentsException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 7/13/11
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class EvaluatorTest {

    Environment environment = new Environment(Environment.ourGlobal);

    private LispObject evaluateString (String lispCode) throws LispException {
        Parser parser = new Parser();
        return Evaluator.evaluate(parser.parseLine(lispCode), environment);
    }

    @Test
    public void testEvaluateInteger () {
        LispObject lispObject = evaluateString("5");
        Assert.assertEquals(new LispInteger(5), lispObject);
    }

    @Test
    public void testEvaluateString () {
        LispObject lispObject = evaluateString("\"test\"");
        Assert.assertEquals(new LispString("test"), lispObject);
    }

    @Test (expected = VoidVariableException.class)
    public void testEvalSymbol() {
        evaluateString("test");
    }

    @Test
    public void testPlus() throws LispException {
        LispObject lispObject = evaluateString("(+ 2 2)");
        Assert.assertEquals(new LispInteger(4), lispObject);
    }

    @Test
    public void testInnerLists() throws LispException {
        LispObject lispObject = evaluateString("(+ 2 (+ 2 3))");
        Assert.assertEquals(new LispInteger(7), lispObject);
    }

    @Test
    public void testQuote () {
        LispObject lispObject = evaluateString("'5");
        Assert.assertEquals(new LispInteger(5), lispObject);
    }

    @Test
    public void testQuotedQuotedList () {
        LispObject lispObject = evaluateString("'(quote 5)");
        Assert.assertEquals(new LispList(new LispSymbol("quote"), new LispInteger(5)), lispObject);
    }

    @Test
    public void testSetVar() throws LispException {
        LispObject value = evaluateString("(set 'var (+ 2 3))");
        Assert.assertEquals("set return value assertion", new LispInteger(5), value);
        LispObject lispObject = evaluateString("var");
        Assert.assertEquals(new LispInteger(5), lispObject);
    }

    @Test
    public void testDefun3args() {
        LispObject fun = evaluateString("(defun mult7 (arg) (* 7 arg))");
        Assert.assertEquals("defun return value assertion", new LispSymbol("mult7"), fun);
        LispObject value = evaluateString("(mult7 5)");
        Assert.assertEquals("mult7 return value assertion", new LispInteger(35), value);
    }

    @Test
    public void testDefun4args() {
        LispObject fun = evaluateString("(defun mult7 (arg) \"multiplies arg*7\" (* 7 arg))");
        Assert.assertEquals("defun return value assertion", new LispSymbol("mult7"), fun);
        LispObject value = evaluateString("(mult7 5)");
        Assert.assertEquals("mult7 return value assertion", new LispInteger(35), value);
    }

    @Test (expected = WrongNumberOfArgumentsException.class)
    public void testDefunWrongNumberOfArgs() {
        LispObject fun = evaluateString("(defun mult7 () ())");
        Assert.assertEquals("defun return value assertion", new LispSymbol("mult7"), fun);
        evaluateString("(mult7 5)");
    }

    @Test
    public void testDefunEmptyBody() {
        LispObject fun = evaluateString("(defun nilFun () ())");
        Assert.assertEquals("defun return value assertion", new LispSymbol("nilFun"), fun);
        LispObject value = evaluateString("(nilFun)");
        Assert.assertEquals("nilFun return value assertion", LispSymbol.ourNil, value);
    }

    @Test
    public void testDefunEmptyBody2 () {
        LispObject fun = evaluateString("(defun nilFun ())");
        Assert.assertEquals("defun return value assertion", new LispSymbol("nilFun"), fun);
        LispObject value = evaluateString("(nilFun)");
        Assert.assertEquals("nilFun return value assertion", LispSymbol.ourNil, value);
    }


    @Test
    public void testNil () {
        LispObject n = evaluateString("nil");
        Assert.assertEquals(LispSymbol.ourNil, n);
    }

    @Test
    public void testT () {
        LispObject n = evaluateString("t");
        Assert.assertEquals(LispSymbol.ourT, n);
    }

    @Test
    public void testDefunIntBody () {
        LispObject fun = evaluateString("(defun testFun () 5)");
        Assert.assertEquals("defun return value assertion", new LispSymbol("testFun"), fun);
        LispObject value = evaluateString("(testFun)");
        Assert.assertEquals("testFun return value assertion", new LispInteger(5), value);
    }

    @Test (expected = InvalidFunctionException.class)
    public void testDefunWrongBody () {
        LispObject fun = evaluateString("(defun testFun () (5))");
        Assert.assertEquals("defun return value assertion", new LispSymbol("testFun"), fun);
        evaluateString("(testFun)");
    }

    @Test
    public void testDefunComplexBody () {
        LispObject fun = evaluateString("(defun testFun () 5 6 7 8 'ann)");
        Assert.assertEquals("defun return value assertion", new LispSymbol("testFun"), fun);
        LispObject value = evaluateString("(testFun)");
        Assert.assertEquals("testFun return value assertion", new LispSymbol("ann"), value);
    }

    @Test
    public void testFunctionSymbolArgumentsSubstitution() {
        evaluateString("(defun test (a) a)");
        LispObject lispObject = evaluateString("(test 5)");
        Assert.assertEquals(new LispInteger(5), lispObject);
    }

    @Test
    public void testFunctionArgumentsSubstitution() {
        evaluateString("(defun test (a) (+ a (+ a 1)))");
        LispObject lispObject = evaluateString("(test 5)");
        Assert.assertEquals(new LispInteger(11), lispObject);
    }

    @Test
    public void testLetEmpty() {
        LispObject lispObject = evaluateString("(let ())");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
    }

    @Test
    public void testLetEmptyVar() {
        LispObject lispObject = evaluateString("(let () 5)");
        Assert.assertEquals(new LispInteger(5), lispObject);
    }

    @Test
    public void testLetNilVar() {
        LispObject lispObject = evaluateString("(let (a) a)");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
    }

    @Test
    public void testLetEmptyBody() {
        LispObject lispObject = evaluateString("(let ((a 5)) )");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
    }

    @Test
    public void testLetAtomVar() {
        LispObject lispObject = evaluateString("(let ((a 5)) a)");
        Assert.assertEquals(new LispInteger(5), lispObject);
    }

    @Test
    public void testLetStar() {
        LispObject lispObject = evaluateString("(let* ((a 5) (b (+ 2 a))) (+ a b))");
        Assert.assertEquals(new LispInteger(12), lispObject);
    }

    @Test
    public void testEq() {
        LispObject lispObject = evaluateString("(eq 5 5)");
        Assert.assertEquals(LispSymbol.ourT, lispObject);
    }

    @Test
    public void testCarSafe() {
        evaluateString("(set 'p 'defun)");
        LispObject lispObject = evaluateString("(car-safe p)");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
    }

    @Test
    public void testQuotedFunctionArg() {
        LispObject kit = evaluateString("(defun kit (a) (car-safe a))");
        Assert.assertEquals("kit ", new LispSymbol("kit"), kit);
        LispObject lispObject = evaluateString("(kit 'test)");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
    }

    @Test
    public void testOrEmpty() {
        LispObject or = evaluateString("(or)");
        Assert.assertEquals(LispSymbol.ourNil, or);
    }

    @Test
    public void testOrMulti() {
        LispObject or = evaluateString("(or nil nil nil 5)");
        Assert.assertEquals(new LispInteger(5), or);
    }

    @Test
    public void testAndEmpty() {
        LispObject and = evaluateString("(and)");
        Assert.assertEquals(LispSymbol.ourT, and);
    }

    @Test
    public void testAndNil() {
        LispObject and = evaluateString("(and 1 2 3 nil)");
        Assert.assertEquals(LispSymbol.ourNil, and);
    }

    @Test
    public void testAndVal() {
        LispObject and = evaluateString("(and 1 2 3 4 5)");
        Assert.assertEquals(new LispInteger(5), and);
    }

    @Test
    public void testIfT () {
        LispObject lispObject = evaluateString("(if t)");
        Assert.assertEquals(LispSymbol.ourT, lispObject);
    }

    @Test
    public void testIfNil () {
        LispObject lispObject = evaluateString("(if nil)");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
    }

    @Test
    public void testIfTrue () {
        LispObject lispObject = evaluateString("(if 5 'true 'false)");
        Assert.assertEquals(new LispSymbol("true"), lispObject);
    }

    @Test
    public void testIfFalse () {
        LispObject lispObject = evaluateString("(if () 'true 'one 'two 'false)");
        Assert.assertEquals(new LispSymbol("false"), lispObject);
    }

    @Test
    public void testMemq () {
        evaluateString("(set 'a '(1 2 3))");
        LispObject lispObject = evaluateString("(memq 4 a)");
        Assert.assertEquals("not exist", LispSymbol.ourNil, lispObject);
        lispObject = evaluateString("(memq 3 a)");
        Assert.assertEquals(new LispList(new LispInteger(3)), lispObject);
    }

    @Test
    public void testList() {
        LispObject lispObject = evaluateString("(list)");
        Assert.assertEquals("no args", LispSymbol.ourNil, lispObject);
        lispObject = evaluateString("(list 5 \"test\")");
        Assert.assertEquals("2 args", new LispList(new LispInteger(5), new LispString("test")), lispObject);
        lispObject = evaluateString("(list nil)");
        Assert.assertEquals("list of nil -1", new LispList(LispSymbol.ourNil), lispObject);
        lispObject = evaluateString("(list (list))");
        Assert.assertEquals("list of nil -2", new LispList(LispSymbol.ourNil), lispObject);
    }

    //@Ignore
    @Test
    public void testFinder () {
        try {
        LispObject path = evaluateString("(" + Environment.ourFinder.getName() + " 'fill-column 'defvar)");
        Assert.assertEquals(new LispString("src/buffer.c"), path);
        } catch (LispException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }



}
