/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;

import org.eclipse.mylyn.internal.tasks.ui.editors.CheckboxMultiSelectAttributeEditor;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.services.IServiceLocator;

public class YouTrackAttributeEditorFactory extends AttributeEditorFactory {

  public class YouTrackTaskEditor extends AbstractAttributeEditor {

    public YouTrackTaskEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
      super(manager, taskAttribute);
    }

    @Override
    public void createControl(Composite parent, FormToolkit toolkit) {

      Composite composite = toolkit.createComposite(parent);
      composite.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
      GridLayout layout = new GridLayout(2, false);
      layout.marginWidth = 0;
      layout.marginBottom = 0;
      layout.marginLeft = 0;
      layout.marginRight = 0;
      layout.marginTop = 0;
      layout.marginHeight = 0;
      composite.setLayout(layout);
    }

  }


  private final TaskDataModel model;

  public YouTrackAttributeEditorFactory(TaskDataModel model, TaskRepository taskRepository,
      IServiceLocator serviceLocator) {
    super(model, taskRepository, serviceLocator);
    this.model = model;
  }

  @Override
  public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {

    if (TaskAttribute.TYPE_MULTI_SELECT.equals(type)) {
      CheckboxMultiSelectAttributeEditor attributeEditor =
          new CheckboxMultiSelectAttributeEditor(model, taskAttribute);
      attributeEditor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
      return attributeEditor;
    }

    return super.createEditor(type, taskAttribute);
  }
}
