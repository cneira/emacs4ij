package org.jetbrains.emacs4ij.ide;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.emacs4ij.ide.tool.EchoArea;

public class EchoAreaToolWindowFactory implements ToolWindowFactory {
  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    EchoArea echoArea = project.getComponent(MyProjectComponent.class).getEchoArea();
    Content content = ContentFactory.SERVICE.getInstance().createContent(echoArea, "", false);
    toolWindow.getContentManager().addContent(content);
  }
}
