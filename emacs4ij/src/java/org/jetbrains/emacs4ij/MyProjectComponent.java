package org.jetbrains.emacs4ij;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.emacs4ij.jelisp.CustomEnvironment;
import org.jetbrains.emacs4ij.jelisp.GlobalEnvironment;
import org.jetbrains.emacs4ij.jelisp.exception.DoubleBufferException;
import org.jetbrains.emacs4ij.jelisp.exception.EnvironmentException;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 8/5/11
 * Time: 12:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyProjectComponent implements ProjectComponent {
    private CustomEnvironment myEnvironment = null;
    private Project myProject;
    private final String myDisplayGroupId = "Emacs4ij";

    public MyProjectComponent(Project project) {
        myProject = project;
        IdeaBuffer.setProject(project);
    }

    @NotNull
    public String getComponentName() {
        return "org.jetbrains.emacs4ij.MyProjectComponent";
    }

    public void projectOpened() {
        myProject.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
                if (myEnvironment == null)
                    return;
                try {
                    new IdeaBuffer(myEnvironment, virtualFile.getName(), virtualFile.getParent().getPath() + '/', fileEditorManager.getSelectedTextEditor());
                } catch (DoubleBufferException e) {
                    System.err.println(e.getMessage());
                }
//                System.out.print("open: ");
//                myEnvironment.printBuffers();
            }

            @Override
            public void fileClosed(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
                if (myEnvironment == null)
                    return;

                if (!(myEnvironment.isSelectionManagedBySubroutine()))
                    myEnvironment.killBuffer(virtualFile.getName());
                else myEnvironment.setSelectionManagedBySubroutine(false);

//                System.out.print("close: ");
//                myEnvironment.printBuffers();
            }

            @Override
            public void selectionChanged(FileEditorManagerEvent fileEditorManagerEvent) {
                if (myEnvironment == null)
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
//                    System.out.print("select: ");
//                    myEnvironment.printBuffers();
                } catch (EnvironmentException e) {
                    //ignore
                }
            }
        });

        new Task.Backgroundable(myProject, "Initializing Emacs environment", false) {
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Loading Emacs functions");
                indicator.setFraction(0.0);
                if (EnvironmentInitializer.silentInitGlobal()) {
                    myEnvironment = new CustomEnvironment(GlobalEnvironment.INSTANCE);
                    EnvironmentInitializer.initProjectEnv(myProject, myEnvironment);
                } else {
                    final String htmlText = "Error occurred while initializing Emacs4ij environment.\nYou can fix Emacs Settings on the Tools Panel or <a href=\"xxx\">now</a>.";
                    JBPopupFactory.getInstance()
                            .createHtmlTextBalloonBuilder(htmlText, MessageType.WARNING, new HyperlinkListener() {
                                @Override
                                public void hyperlinkUpdate(HyperlinkEvent e) {
                                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                                        new OpenSettings().actionPerformed(myProject);
                                }
                            })
//                            .setHideOnAction(true)
//                            .setFadeoutTime(7500)
                            .createBalloon()
                            .show(RelativePoint.getNorthEastOf(WindowManager.getInstance().getIdeFrame(myProject).getComponent()), Balloon.Position.atRight);


//                    new Notification(myDisplayGroupId,
//                            "Emacs4ij",
//                            "Error occurred while initializing Emacs4ij environment. You can fix Emacs Settings on the Tools Panel or <a href=\"xxx\">now</a>.",
//                            NotificationType.WARNING,
//                            new NotificationListener() {
//                                @Override
//                                public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
//                                    new OpenSettings().actionPerformed(myProject);
//                                    notification.expire();
//                                }
//                            }).notify(myProject);
                }
                indicator.setFraction(1.0);
            }
        }.queue();
    }

    public void initEnv () {
        myEnvironment = new CustomEnvironment(GlobalEnvironment.INSTANCE);
        EnvironmentInitializer.initProjectEnv(myProject, myEnvironment);
    }

    public CustomEnvironment getEnvironment() {
        return myEnvironment;
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    public void projectClosed() {
        // called when project is being closed
    }
}
