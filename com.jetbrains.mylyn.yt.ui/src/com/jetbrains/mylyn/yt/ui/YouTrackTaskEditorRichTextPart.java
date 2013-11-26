/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.State;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedEvent;
import org.eclipse.mylyn.internal.tasks.ui.editors.RichTextEditor.StateChangedListener;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

/**
 * @author initial API: Steffen Pingel
 * @author Alexander Marchuk
 */
public class YouTrackTaskEditorRichTextPart extends AbstractTaskEditorPart {

  private RichTextAttributeEditor editor;

  private TaskAttribute attribute;

  private Composite composite;

  private int sectionStyle;

  private Action toggleEditAction;

  private Action toggleBrowserAction;

  private boolean ignoreToggleEvents;

  public YouTrackTaskEditorRichTextPart() {
    setSectionStyle(ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
        | ExpandableComposite.EXPANDED);
  }

  public void appendText(String text) {
    if (editor == null) {
      return;
    }

    editor.showEditor();
    if (toggleEditAction != null) {
      toggleEditAction.setChecked(false);
    }

    StringBuilder strBuilder = new StringBuilder();
    String oldText = editor.getViewer().getDocument().get();
    if (strBuilder.length() != 0) {
      strBuilder.append("\n"); //$NON-NLS-1$
    }
    strBuilder.append(oldText);
    strBuilder.append(text);
    editor.getViewer().getDocument().set(strBuilder.toString());
    TaskAttribute attribute = getTaskData().getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
    if (attribute != null) {
      attribute.setValue(strBuilder.toString());
      getTaskEditorPage().getModel().attributeChanged(attribute);
    }
    editor.getViewer().getTextWidget().setCaretOffset(strBuilder.length());
    editor.getViewer().getTextWidget().showSelection();
  }

  public int getSectionStyle() {
    return sectionStyle;
  }

  public void setSectionStyle(int sectionStyle) {
    this.sectionStyle = sectionStyle;
  }

  @Override
  public void createControl(Composite parent, FormToolkit toolkit) {

    if (attribute == null) {
      return;
    }
    AbstractAttributeEditor attributeEditor = createAttributeEditor(attribute);
    if (!(attributeEditor instanceof RichTextAttributeEditor)) {
      return;
    }

    Section section = createSection(parent, toolkit, sectionStyle);

    composite = toolkit.createComposite(section);
    composite.setLayout(EditorUtil.createSectionClientLayout());

    if (attribute.getMetaData().isReadOnly()) {

      Browser browser = new Browser(composite, SWT.NONE);
      GridData gd = EditorUtil.getTextControlLayoutData(getTaskEditorPage(), browser, true);

      if (getTaskEditorPage() != null
          && getTaskEditorPage().getPart(AbstractTaskEditorPage.ID_PART_ATTRIBUTES) != null
          && getTaskEditorPage().getPart(AbstractTaskEditorPage.ID_PART_ATTRIBUTES).getControl() != null) {
        gd.heightHint =
            getTaskEditorPage().getPart(AbstractTaskEditorPage.ID_PART_ATTRIBUTES).getControl()
                .computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
        // TODO: check the margins width, not hardcode constants
        gd.widthHint =
            getTaskEditorPage().getEditorComposite().getSize().x
                - getTaskEditorPage().getPart(AbstractTaskEditorPage.ID_PART_ATTRIBUTES)
                    .getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).x - 40;
      }

      browser.setLayoutData(gd);
      String wikifyDescription =
          getTaskData().getRoot().getMappedAttribute(YouTrackTaskDataHandler.WIKIFY_DESCRIPTION)
              .getValue();
      browser.setText(wikifyDescription);
    } else {
      editor = (RichTextAttributeEditor) attributeEditor;
      editor.createControl(composite, toolkit);
      StyledText textWidget = editor.getViewer().getTextWidget();
      editor.getControl().setLayoutData(
          EditorUtil.getTextControlLayoutData(getTaskEditorPage(), textWidget,
              getExpandVertically()));
      editor.getControl().setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
    }

    toolkit.paintBordersFor(composite);
    section.setClient(composite);
    setSection(toolkit, section);
  }

  public TaskAttribute getAttribute() {
    return attribute;
  }

  protected Composite getComposite() {
    return composite;
  }

  protected RichTextAttributeEditor getEditor() {
    return editor;
  }

  public void setAttribute(TaskAttribute attribute) {
    this.attribute = attribute;
  }

  @Override
  public void setFocus() {
    if (editor != null) {
      editor.getControl().setFocus();
    }
  }

  @Override
  protected void fillToolBar(ToolBarManager manager) {
    if (getEditor().hasPreview()) {
      toggleEditAction = new Action("", SWT.TOGGLE) { //$NON-NLS-1$
            @Override
            public void run() {
              if (isChecked()) {
                editor.showEditor();
              } else {
                editor.showPreview();
              }

              if (toggleBrowserAction != null) {
                toggleBrowserAction.setChecked(false);
              }
            }
          };
      toggleEditAction.setImageDescriptor(CommonImages.EDIT_SMALL);
      toggleEditAction.setToolTipText(Messages.TaskEditorRichTextPart_Edit_Tooltip);
      toggleEditAction.setChecked(true);
      getEditor().getEditor().addStateChangedListener(new StateChangedListener() {
        public void stateChanged(StateChangedEvent event) {
          try {
            ignoreToggleEvents = true;
            toggleEditAction
                .setChecked(event.state == State.EDITOR || event.state == State.DEFAULT);
          } finally {
            ignoreToggleEvents = false;
          }
        }
      });
      manager.add(toggleEditAction);
    }
    if (toggleEditAction == null && getEditor().hasBrowser()) {
      toggleBrowserAction = new Action("", SWT.TOGGLE) { //$NON-NLS-1$
            @Override
            public void run() {
              if (ignoreToggleEvents) {
                return;
              }
              if (isChecked()) {
                editor.showBrowser();
              } else {
                editor.showEditor();
              }

              if (toggleEditAction != null) {
                toggleEditAction.setChecked(false);
              }
            }
          };
      toggleBrowserAction.setImageDescriptor(CommonImages.PREVIEW_WEB);
      toggleBrowserAction.setToolTipText(Messages.TaskEditorRichTextPart_Browser_Preview);
      toggleBrowserAction.setChecked(false);
      getEditor().getEditor().addStateChangedListener(new StateChangedListener() {
        public void stateChanged(StateChangedEvent event) {
          try {
            ignoreToggleEvents = true;
            toggleBrowserAction.setChecked(event.state == State.BROWSER);
          } finally {
            ignoreToggleEvents = false;
          }
        }
      });
      manager.add(toggleBrowserAction);
    }
    if (!getEditor().isReadOnly()) {
      manager.add(getMaximizePartAction());
    }
    super.fillToolBar(manager);
  }

  @Override
  protected Control getLayoutControl() {
    return (getEditor() != null) ? getEditor().getControl() : null;
  }

  @Override
  public boolean setFormInput(Object input) {
    if (input instanceof String && getAttribute() != null) {
      if (input.equals(getAttribute().getId())) {
        EditorUtil.focusOn(getTaskEditorPage().getManagedForm().getForm(), getControl());
        return true;
      }
    }
    return false;
  }

}
