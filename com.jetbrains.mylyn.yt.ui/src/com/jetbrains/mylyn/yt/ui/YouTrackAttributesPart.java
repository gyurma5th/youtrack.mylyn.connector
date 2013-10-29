package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class YouTrackAttributesPart extends AbstractTaskEditorPart {

  private static final int COLUMN_MARGIN = 5;

  public YouTrackAttributesPart() {
    setPartName("Attributes");
  }

  private void addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute) {
    if (attribute != null || "".equals(attribute)) {
      AbstractAttributeEditor editor = createAttributeEditor(attribute);
      if (editor != null) {
        editor.createLabelControl(composite, toolkit);
        GridDataFactory.defaultsFor(editor.getLabelControl()).indent(COLUMN_MARGIN, 0)
            .applyTo(editor.getLabelControl());
        editor.createControl(composite, toolkit);
        getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
        if (editor instanceof YouTrackAttributeEditor) {
          GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).hint(130, 95)
              .applyTo(editor.getControl());
        } else {
          GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP)
              .applyTo(editor.getControl());
        }
      }
    }
  }

  @Override
  public void createControl(Composite parent, FormToolkit toolkit) {
    Section section = createSection(parent, toolkit, true);
    Composite attributesComposite = toolkit.createComposite(section);
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 5;
    attributesComposite.setLayout(layout);

    addAttribute(attributesComposite, toolkit,
        getTaskData().getRoot().getMappedAttribute(TaskAttribute.PRODUCT));

    for (String key : getTaskData().getRoot().getAttributes().keySet()) {
      if (getTaskData().getRoot().getMappedAttribute(key).getMetaData().getKind() != null
          && getTaskData().getRoot().getMappedAttribute(key).getMetaData().getKind()
              .equals(YouTrackTaskDataHandler.CUSTOM_FIELD_KIND)) {
        addAttribute(attributesComposite, toolkit, getTaskData().getRoot().getMappedAttribute(key));
      }
    }

    addAttribute(attributesComposite, toolkit,
        getTaskData().getRoot().getMappedAttribute(TaskAttribute.COMPONENT));

    addAttribute(attributesComposite, toolkit,
        getTaskData().getRoot().getMappedAttribute(YouTrackTaskDataHandler.TAG_PREFIX));

    toolkit.paintBordersFor(attributesComposite);
    section.setClient(attributesComposite);
    setSection(toolkit, section);
  }

}
