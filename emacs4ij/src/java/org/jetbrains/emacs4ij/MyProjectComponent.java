package org.jetbrains.emacs4ij;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.EnvironmentException;
import org.jetbrains.emacs4ij.jelisp.GlobalEnvironment;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 8/5/11
 * Time: 12:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyProjectComponent implements ProjectComponent {
    private Environment myEnvironment;
    private Project myProject;

    public MyProjectComponent(Project project) {
        myProject = project;
        IdeaBuffer.setProject(project);
        if (!Checker.isReady()) {
            Checker.isGlobalEnvironmentInitialized = false;
            myEnvironment = null;
            // Messages.showInfoMessage("Until you set Emacs environment, no Emacs emulation will work.\nYou can set it by clicking on any of Emacs4ij icons.", "Emacs4ij");
        } else {
            initEnvironment();
        }
    }

    public boolean initEnvironment () {
        if (myEnvironment != null)
            return true;

        if (!MyApplicationComponent.initGlobalEnvironment())
            return false;

        myEnvironment = new Environment(GlobalEnvironment.getInstance());
        IdeaMiniBuffer miniBuffer = new IdeaMiniBuffer(0, null, myEnvironment);
        myEnvironment.defineServiceBuffer(miniBuffer);
        String scratchDir = myProject.getProjectFilePath().substring(0, myProject.getProjectFilePath().lastIndexOf("/")+1);
        IdeaBuffer scratchBuffer = new IdeaBuffer(myEnvironment, GlobalEnvironment.ourScratchBufferName, scratchDir, null);
        myEnvironment.defineServiceBuffer(scratchBuffer);

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
        for (VirtualFile virtualFile : fileEditorManager.getOpenFiles()) {
            IdeaBuffer newBuffer = new IdeaBuffer(myEnvironment, virtualFile.getName(), virtualFile.getParent().getPath()+'/', fileEditorManager.getSelectedTextEditor());
            myEnvironment.defineBuffer(newBuffer);
            //System.out.print("open: ");
            //setHeaders(newBuffer);
            //myEnvironment.printBuffers();
        }
        return true;
    }

    public Environment getEnvironment() {
        return myEnvironment;
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "org.jetbrains.emacs4ij.MyProjectComponent";
    }

    public void projectOpened() {
        myProject.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
                if (!Checker.isGlobalEnvironmentInitialized)
                    return;

                IdeaBuffer newBuffer = new IdeaBuffer(myEnvironment, virtualFile.getName(), virtualFile.getParent().getPath()+'/', fileEditorManager.getSelectedTextEditor());
                myEnvironment.defineBuffer(newBuffer);
                System.out.print("open: ");
                //setHeaders(newBuffer);
                myEnvironment.printBuffers();
            }

            @Override
            public void fileClosed(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
                if (!Checker.isGlobalEnvironmentInitialized)
                    return;

                if (!(myEnvironment.isSelectionManagedBySubroutine()))
                    myEnvironment.killBuffer(virtualFile.getName());
                else myEnvironment.setSelectionManagedBySubroutine(false);

                System.out.print("close: ");
                myEnvironment.printBuffers();
            }

            @Override
            public void selectionChanged(FileEditorManagerEvent fileEditorManagerEvent) {
                if (!Checker.isGlobalEnvironmentInitialized)
                    return;

                if (fileEditorManagerEvent.getNewFile() == null) {
                    if (myEnvironment.getBuffersSize() != 1)
                        throw new RuntimeException("the number of opened buffers doesn't correspond to number of opened files!");
                    return;
                }
                try {
                    if (!(myEnvironment.isSelectionManagedBySubroutine()))
                        myEnvironment.switchToBuffer(fileEditorManagerEvent.getNewFile().getName());
                    else myEnvironment.setSelectionManagedBySubroutine(false);
                    //setHeaders((IdeaBuffer)myEnvironment.getBufferCurrentForEditing());
                    System.out.print("select: ");
                    myEnvironment.printBuffers();
                } catch (EnvironmentException e) {
                    //ignore
                }
            }
        });
    }

    public void projectClosed() {
        // called when project is being closed
    }
}
