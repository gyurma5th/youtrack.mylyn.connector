/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorOutlineNode;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.forms.IManagedForm;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class YouTrackTaskEditorPage extends AbstractTaskEditorPage {

  public final static String ID_NEW_COMMENTS_PART = "ID_NEW_COMMENTS_PART";

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
    }.setPath(PATH_COMMENTS));

    descriptors.add(new TaskEditorPartDescriptor(ID_NEW_COMMENTS_PART) {
      @Override
      public AbstractTaskEditorPart createPart() {
        AbstractTaskEditorPart part = new YouTrackTaskEditorNewCommentPart();
        return part;
      }
    }.setPath(PATH_COMMENTS));

    return descriptors;
  }

  @Override
  public void fillToolBar(final IToolBarManager toolBarManager) {

    Action webViewAction = new Action() {
      @Override
      public void run() {
        IWebBrowser browser;
        try {
          browser =
              PlatformUI.getWorkbench().getBrowserSupport()
                  .createBrowser(getModel().getTaskData().getRoot().getId());
          URL issueURL =
              YouTrackTaskDataHandler.getIssueURL(getModel().getTaskData(), getTaskRepository());
          if (issueURL != null) {
            browser.openURL(issueURL);
          }
        } catch (PartInitException e) {
          throw new RuntimeException(e);
        }
      }
    };
    webViewAction.setToolTipText("Open issue in internal Eclipse browser.");
    webViewAction.setImageDescriptor(CommonImages.WEB);
    toolBarManager.add(webViewAction);

    Action submitAction = new Action() {
      @Override
      public void run() {
        doSubmit();
      }
    };
    submitAction.setToolTipText("Submit");
    submitAction.setImageDescriptor(TasksUiImages.REPOSITORY_SUBMIT);
    toolBarManager.add(submitAction);


    Action editAction = new Action() {
      @Override
      public void run() {
        if (!YouTrackTaskDataHandler.isEnableEditMode()) {
          doEdit();
        }
      }
    };
    editAction.setToolTipText("Edit");
    editAction.setImageDescriptor(CommonImages.EDIT);
    toolBarManager.add(editAction);

    Action cancelAction = new Action() {
      @Override
      public void run() {
        if (YouTrackTaskDataHandler.isEnableEditMode()) {
          doCancel();
        }
      }
    };
    cancelAction.setToolTipText("Cancel");
    cancelAction.setImageDescriptor(CommonImages.REMOVE);
    toolBarManager.add(cancelAction);
  }

  public void doEdit() {
    YouTrackTaskDataHandler.setEnableEditMode(true);
    getEditor().refreshPages();
  }

  public void doCancel() {
    YouTrackTaskDataHandler.setEnableEditMode(false);
    getModel().revert();
    getEditor().refreshPages();
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

  @Override
  protected void createParts() {
    List<TaskEditorPartDescriptor> descriptors =
        new LinkedList<TaskEditorPartDescriptor>(createPartDescriptors());
    // single column
    createParts(PATH_HEADER, getEditorComposite(), descriptors);
    // two column
    Composite bottomComposite = getManagedForm().getToolkit().createComposite(getEditorComposite());
    bottomComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
    GridDataFactory.fillDefaults().grab(true, false).applyTo(bottomComposite);
    createParts(PATH_ACTIONS, bottomComposite, descriptors);
    createParts(PATH_PEOPLE, bottomComposite, descriptors);
    bottomComposite.pack(true);
    createParts(PATH_COMMENTS, getEditorComposite(), descriptors);
  }

  private void createParts(String path, final Composite parent,
      final Collection<TaskEditorPartDescriptor> descriptors) {
    for (Iterator<TaskEditorPartDescriptor> it = descriptors.iterator(); it.hasNext();) {
      final TaskEditorPartDescriptor descriptor = it.next();
      if (path == null || path.equals(descriptor.getPath())) {
        SafeRunner.run(new ISafeRunnable() {
          public void handleException(Throwable e) {}

          public void run() throws Exception {
            AbstractTaskEditorPart part = descriptor.createPart();
            if (part instanceof YouTrackDescriptionPart) {
              ((YouTrackDescriptionPart) part).setPartId(descriptor.getId());
            } else if (part instanceof YouTrackAttributesPart) {
              ((YouTrackAttributesPart) part).setPartId(descriptor.getId());
            } else if (part instanceof YouTrackTaskEditorNewCommentPart) {
              ((YouTrackTaskEditorNewCommentPart) part).setPartId(descriptor.getId());
            }
            initializePart(parent, part, descriptors);
          }
        });
        it.remove();
      }
    }
  }

  private void initializePart(Composite parent, AbstractTaskEditorPart part,
      Collection<TaskEditorPartDescriptor> descriptors) {
    getManagedForm().addPart(part);
    part.initialize(this);
    if (parent != null) {
      part.createControl(parent, getManagedForm().getToolkit());
      if (part.getControl() != null) {
        if (ID_PART_ACTIONS.equals(part.getPartId())) {
          // do not expand horizontally
          GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(false, false)
              .applyTo(part.getControl());
        } else {
          if (part.getExpandVertically()) {
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
                .applyTo(part.getControl());
          } else {
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false)
                .applyTo(part.getControl());
          }
        }
        // for outline
        if (ID_PART_COMMENTS.equals(part.getPartId())) {
          EditorUtil.setMarker(part.getControl(), TaskEditorOutlineNode.LABEL_COMMENTS);
        } else if (ID_PART_ATTACHMENTS.equals(part.getPartId())) {
          EditorUtil.setMarker(part.getControl(), TaskEditorOutlineNode.LABEL_ATTACHMENTS);
        }
      }
    }
  }

  @Override
  public void doSubmit() {
    super.doSubmit();
    YouTrackTaskDataHandler.setEnableEditMode(false);
  }

}
