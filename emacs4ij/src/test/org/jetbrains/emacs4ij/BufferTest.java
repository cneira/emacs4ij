package org.jetbrains.emacs4ij;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.Parser;
import org.jetbrains.emacs4ij.jelisp.elisp.*;
import org.jetbrains.emacs4ij.jelisp.exception.WrongNumberOfArgumentsException;
import org.jetbrains.emacs4ij.jelisp.exception.WrongTypeArgument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 9/22/11
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class BufferTest extends CodeInsightFixtureTestCase {
    Environment myEnvironment;
    Parser myParser = new Parser();
    String myTestsPath = "/home/kate/emacs4ij/emacs4ij/src/testSrc/";
    HashMap<String, IdeaEditor> myTests;
    String[]  myTestFiles;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        myTestFiles = (new File (myTestsPath)).list();
        myTests = new HashMap<String, IdeaEditor>();
        myEnvironment = new Environment(new Environment());
        for (String fileName: myTestFiles) {
            myFixture.configureByFile(myTestsPath + fileName);
            myTests.put(fileName, new IdeaEditor(fileName, getEditor()));
            myEnvironment.defineBuffer(new IdeaEditor(fileName, getEditor()));
        }
    }

    private LObject eval (String lispCode) {
        return myParser.parseLine(lispCode).evaluate(myEnvironment);
    }

    @Test
    public void testCurrentBuffer() {
        LObject lispObject = eval("(current-buffer)");
        Assert.assertEquals(myTests.get(myTestFiles[myTestFiles.length - 1]), lispObject);
    }

    @Test
    public void testBufferp() {
        LObject lispObject = eval("(bufferp (current-buffer))");
        Assert.assertEquals(LispSymbol.ourT, lispObject);
    }

    @Test
    public void testBufferpWrongNargs() {
        try {
            eval("(bufferp)");
        } catch (WrongNumberOfArgumentsException e) {
            //success
        }
    }

    @Test
    public void testGetBufferByName() {
        LObject lispObject = eval("(get-buffer \"1.txt\")");
        Assert.assertEquals(myTests.get("1.txt"), lispObject);
    }

    @Test
    public void testGetBufferByBuffer() {
        LObject lispObject = eval("(get-buffer (current-buffer))");
        Assert.assertEquals(myTests.get(myTestFiles[myTestFiles.length - 1]), lispObject);
    }

    @Test
    public void testGetBuffer_NonExistent() {
        LObject lispObject = eval("(get-buffer \"test.txt\")");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
    }


    @Test
    public void testBufferSize() {
        LObject currentBufferSizeAnonymous = eval("(buffer-size)");
        LObject currentBufferSize = eval("(buffer-size (current-buffer))");
        Assert.assertEquals(currentBufferSize, currentBufferSizeAnonymous);
    }

    @Test
    public void testBufferSizeNil() {
        LObject currentBufferSizeAnonymous = eval("(buffer-size nil)");
        LObject currentBufferSize = eval("(buffer-size (current-buffer))");
        Assert.assertEquals(currentBufferSize, currentBufferSizeAnonymous);
    }

    @Test
    public void testBufferName () {
        LObject lispObject = eval("(buffer-name (get-buffer \"1.txt\"))");
        Assert.assertEquals(new LispString("1.txt"), lispObject);
    }

    @Test
    public void testOtherBuffer_SingleBuffer () {
        while (myEnvironment.getBuffersSize() != 1)
            myEnvironment.closeCurrentBuffer();
        LObject lispObject = eval("(other-buffer)");
        Assert.assertEquals(myEnvironment.getCurrentBuffer(), lispObject);
    }

    @Test
    public void testOtherBuffer_NoBuffers () {
        myEnvironment.closeAllBuffers();
        try {
            eval("(other-buffer)");
        } catch (RuntimeException e) {
            Assert.assertEquals("no buffer is currently opened", e.getMessage());
            return;
        }
        Assert.assertEquals(1, 0);
    }

    @Test
    public void testOtherBuffer_NoParameters3buffers () {
        LObject lispObject = eval("(other-buffer)");
        Assert.assertEquals(myEnvironment.getBufferByIndex(myEnvironment.getBuffersSize()-2), lispObject);
    }

    @Test
    public void testOtherBuffer_NotBufferParameter () {
        LObject lispObject = eval("(other-buffer 1)");
        Assert.assertEquals(myEnvironment.getBufferByIndex(myEnvironment.getBuffersSize()-2), lispObject);
    }

    @Test
    public void testOtherBuffer_BufferParameter () {
        LObject lispObject = eval("(other-buffer (get-buffer \""+ myTestFiles[0] +"\" ))");
        Assert.assertEquals(myEnvironment.getCurrentBuffer(), lispObject);
    }

    @Test
    public void testEnvironment_GetBuffersNames() {
        String[] buffersNames = myEnvironment.getBuffersNames();
        Assert.assertArrayEquals(myTestFiles, buffersNames);
    }

    @Test
    public void testSwitchToBuffer_NoArgs() {
        try {
            eval("(switch-to-buffer)");
        } catch (WrongNumberOfArgumentsException e) {
            //success
            return;
        }
        Assert.assertEquals(0, 1);
    }

    @Test
    public void testSwitchToBuffer_Nil() {
        LObject lispObject = eval("(switch-to-buffer nil)");
        Assert.assertEquals(myEnvironment.getCurrentBuffer(), lispObject);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myFixture.getProject());
        Assert.assertEquals(myEnvironment.getCurrentBuffer().getEditor() , fileEditorManager.getSelectedTextEditor());
    }

    @Test
    public void testSwitchToBuffer_ExistentString() {
        LObject lispObject = eval("(switch-to-buffer \"" + myTestFiles[0] + "\")");
        Assert.assertEquals(myEnvironment.getCurrentBuffer(), lispObject);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myFixture.getProject());
        Assert.assertEquals(myEnvironment.getCurrentBuffer().getEditor() , fileEditorManager.getSelectedTextEditor());

    }

    @Test
    public void testSwitchToBuffer_NonExistentString() {
        LObject lispObject = eval("(switch-to-buffer \"test.txt\")");
        Assert.assertEquals(new LispString("It is not allowed to create files this way."), lispObject);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myFixture.getProject());
        Assert.assertEquals(myEnvironment.getCurrentBuffer().getEditor() , fileEditorManager.getSelectedTextEditor());
    }

    @Test
    public void testSwitchToBuffer_Buffer() {
        LObject lispObject = eval("(switch-to-buffer (get-buffer \"" + myTestFiles[0] + "\"))");
        Assert.assertEquals(myEnvironment.getCurrentBuffer(), lispObject);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myFixture.getProject());
        Assert.assertEquals(myEnvironment.getCurrentBuffer().getEditor() , fileEditorManager.getSelectedTextEditor());
    }

    @Test
    public void testSwitchToBuffer_ExistentString_NoRecord() {
        LObject lispObject = eval("(switch-to-buffer \"" + myTestFiles[0] + "\" 5)");
        Assert.assertEquals(myEnvironment.getBufferByIndex(0), lispObject);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myFixture.getProject());
        Assert.assertEquals(myEnvironment.getBufferByIndex(0).getEditor() , fileEditorManager.getSelectedTextEditor());
    }

    @Test
    public void testSetBuffer_String () {
        LObject lispObject = eval("(set-buffer \"" + myTestFiles[0] + "\")");
        Assert.assertEquals(myEnvironment.getBufferByIndex(0), lispObject);
    }

    @Test
    public void testSetBuffer_Buffer () {
        LObject lispObject = eval("(set-buffer (get-buffer \"" + myTestFiles[0] + "\"))");
        Assert.assertEquals(myEnvironment.getBufferByIndex(0), lispObject);
    }

    @Ignore
    @Test
    public void testSetBuffer_InProgn () {
        //todo: (progn (set-buffer "0.txt") (buffer-name)) => "1.txt"; but outside progn (buffer-name) => "3.txt"
        //throw new RuntimeException("not implemented!");
    }

    @Test
    public void testSetBuffer_WrongParameter () {
        try {
            eval("(set-buffer 5)");
        } catch (WrongTypeArgument e) {
            return;
        }
        Assert.assertEquals(0, 1);
    }

    @Test
    public void testPoint() {
        LObject lispObject = eval("(point)");
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myFixture.getProject());
        int point = fileEditorManager.getSelectedTextEditor().logicalPositionToOffset(fileEditorManager.getSelectedTextEditor().getCaretModel().getLogicalPosition()) + 1;
        Assert.assertEquals(new LispInteger(point), lispObject);
    }

    @Test
    public void testPointMin() {
        LObject lispObject = eval("(point-min)");
        Assert.assertEquals(new LispInteger(1), lispObject);
    }

    @Test
    public void testPointMax() {
        LObject lispObject = eval("(point-max)");
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myFixture.getProject());
        int pointMax = fileEditorManager.getSelectedTextEditor().getDocument().getTextLength()+1;
        Assert.assertEquals(new LispInteger(pointMax), lispObject);
    }

    @Test
    public void testGoToChar () {
        LObject lispObject = eval("(goto-char 5)");
        Assert.assertEquals(new LispInteger(5), lispObject);
        Assert.assertEquals(myEnvironment.getCurrentBuffer().point(), 5);
    }

    @Test
    public void testGoToChar_End () {
        LObject lispObject = eval("(goto-char 50)");
        Assert.assertEquals(new LispInteger(50), lispObject);
        Assert.assertEquals(myEnvironment.getCurrentBuffer().pointMax(), myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testGoToChar_Begin () {
        LObject lispObject = eval("(goto-char -50)");
        Assert.assertEquals(new LispInteger(-50), lispObject);
        Assert.assertEquals(myEnvironment.getCurrentBuffer().pointMin(), myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testForwardChar_NoParam () {
        int from = myEnvironment.getCurrentBuffer().point();
        LObject lispObject = eval("(forward-char)");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
        Assert.assertEquals(from+1, myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testForwardChar () {
        int from = myEnvironment.getCurrentBuffer().point();
        LObject lispObject = eval("(forward-char 5)");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
        Assert.assertEquals(from+5, myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testForwardChar_End () {
        LObject lispObject = eval("(forward-char 50)");
        Assert.assertEquals(new LispSymbol("End of buffer"), lispObject);
        Assert.assertEquals(myEnvironment.getCurrentBuffer().pointMax(), myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testForwardChar_Begin () {
        LObject lispObject = eval("(forward-char -50)");
        Assert.assertEquals(new LispSymbol("Beginning of buffer"), lispObject);
        Assert.assertEquals(myEnvironment.getCurrentBuffer().pointMin(), myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testBackwardChar_NoParam () {
        myEnvironment.getCurrentBuffer().forwardChar(2);
        int from = myEnvironment.getCurrentBuffer().point();
        LObject lispObject = eval("(backward-char)");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
        Assert.assertEquals(from-1, myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testBackwardChar () {
        myEnvironment.getCurrentBuffer().forwardChar(3);
        int from = myEnvironment.getCurrentBuffer().point();
        LObject lispObject = eval("(backward-char 2)");
        Assert.assertEquals(LispSymbol.ourNil, lispObject);
        Assert.assertEquals(from-2, myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testBackwardChar_End () {
        LObject lispObject = eval("(backward-char 50)");
        Assert.assertEquals(new LispSymbol("Beginning of buffer"), lispObject);
        Assert.assertEquals(myEnvironment.getCurrentBuffer().pointMin(), myEnvironment.getCurrentBuffer().point());
    }

    @Test
    public void testBackwardChar_Begin () {
        LObject lispObject = eval("(backward-char -50)");
        Assert.assertEquals(new LispSymbol("End of buffer"), lispObject);
        Assert.assertEquals(myEnvironment.getCurrentBuffer().pointMax(), myEnvironment.getCurrentBuffer().point());
    }

    //todo:: test mySymbols.put("buffer-end", new LispSymbol("buffer-end", LispSymbol.FunctionType.BuiltIn)); //note: it is compiled lisp function in emacs

    //==== markers ====

    @Test
    public void testPointMarkerOk() {
        LObject lispObject = eval("(point-marker)");
        LispBuffer currentBuffer = (LispBuffer) eval("(current-buffer)");
        Assert.assertEquals(new LispMarker(currentBuffer.point(), currentBuffer), lispObject);
    }

    @Test
    public void testPointMarkerInvalid () {
        try {
            eval("(point-marker)");
        } catch (WrongNumberOfArgumentsException e) {
            //sucess
        }
    }



}