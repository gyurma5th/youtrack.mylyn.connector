/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.internal.tasks.ui.editors.ToolBarButtonContribution;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class YouTrackTaskEditorPage extends AbstractTaskEditorPage {

  private final static String ID_NEW_COMMENTS_PART = "ID_NEW_COMMENTS_PART";

  private boolean refreshed = false;

  public YouTrackTaskEditorPage(TaskEditor editor) {
    super(editor, YouTrackCorePlugin.CONNECTOR_KIND);
    setNeedsPrivateSection(false);
    setNeedsSubmitButton(true);
  }

  @Override
  protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
    Set<TaskEditorPartDescriptor> descriptors = super.createPartDescriptors();

    for (Iterator<TaskEditorPartDescriptor> it = descriptors.iterator(); it.hasNext();) {
      TaskEditorPartDescriptor taskEditorPartDescriptor = it.next();
      if (taskEditorPartDescriptor.getId().equals(ID_PART_PEOPLE)
          || taskEditorPartDescriptor.getId().equals(ID_PART_ATTRIBUTES)
          || taskEditorPartDescriptor.getId().equals(ID_PART_COMMENTS)
          || taskEditorPartDescriptor.getId().equals(ID_PART_ACTIONS)
          || taskEditorPartDescriptor.getId().equals(ID_PART_SUMMARY)
          || taskEditorPartDescriptor.getId().equals(ID_PART_DESCRIPTION)) {
        it.remove();
      }
    }

    descriptors.add(new TaskEditorPartDescriptor(ID_PART_SUMMARY) {
      @Override
      public AbstractTaskEditorPart createPart() {
        return new YouTrackSummaryPart();
      }
    }.setPath(PATH_HEADER));

    descriptors.add(new TaskEditorPartDescriptor(ID_PART_ATTRIBUTES) {
      @Override
      public AbstractTaskEditorPart createPart() {
        YouTrackAttributesPart part = new YouTrackAttributesPart();
        part.setExpandVertically(true);
        return new YouTrackAttributesPart();
      }
    }.setPath(PATH_ACTIONS));

    descriptors.add(new TaskEditorPartDescriptor(ID_PART_DESCRIPTION) {
      @Override
      public AbstractTaskEditorPart createPart() {
        YouTrackDescriptionPart part = new YouTrackDescriptionPart();
        return part;
      }
    }.setPath(PATH_ACTIONS));

    descriptors.add(new TaskEditorPartDescriptor(ID_PART_COMMENTS) {
      @Override
      public AbstractTaskEditorPart createPart() {
        AbstractTaskEditorPart part = new YouTrackTaskEditorCommentsPart();
        return part;
      }
    }.setPath(PATH_HEADER));

    descriptors.add(new TaskEditorPartDescriptor(ID_NEW_COMMENTS_PART) {
      @Override
      public AbstractTaskEditorPart createPart() {
        AbstractTaskEditorPart part = new YouTrackTaskEditorNewCommentPart();
        return part;
      }
    }.setPath(PATH_HEADER));

    return descriptors;
  }

  @Override
  public void fillToolBar(IToolBarManager toolBarManager) {
    super.fillToolBar(toolBarManager);

    ToolBarButtonContribution editButtonContribution =
        new ToolBarButtonContribution("com.jetbrains.yt.mylyn.toolbars.update") {
          @Override
          protected Control createButton(Composite composite) {
            Button editButton = new Button(composite, SWT.FLAT);
            editButton.setText("Edit ");
            editButton.setImage(CommonImages.getImage(CommonImages.EDIT));
            editButton.setBackground(null);
            editButton.addListener(SWT.Selection, new Listener() {
              public void handleEvent(Event e) {
                doEdit();
              }
            });
            return editButton;
          }
        };
    editButtonContribution.marginLeft = 10;
    toolBarManager.add(editButtonContribution);
  }

  public void doEdit() {
    YouTrackTaskDataHandler.setEnableEditMode(true);
    getEditor().refreshPages();
    YouTrackTaskDataHandler.setEnableEditMode(false);
  }

  @Override
  protected AttributeEditorFactory createAttributeEditorFactory() {
    return new YouTrackAttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite());
  }

  @Override
  public void appendTextToNewComment(String text) {
    AbstractTaskEditorPart newCommentPart = getPart(ID_NEW_COMMENTS_PART);
    if (newCommentPart instanceof TaskEditorRichTextPart) {
      ((TaskEditorRichTextPart) newCommentPart).appendText(text);
      newCommentPart.setFocus();
    }
  }

  @Override
  protected void createFormContent(final IManagedForm managedForm) {
    super.createFormContent(managedForm);

    // TODO: fix here, need refresh because Layout of Description and
    // Attributes parts not resizes properly
    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
      public void run() {
        YouTrackTaskEditorPage.this.refresh();
      }
    });

  }
}
