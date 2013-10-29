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
import org.eclipse.ui.services.IServiceLocator;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class YouTrackAttributeEditorFactory extends AttributeEditorFactory {

  private final TaskDataModel model;

  public YouTrackAttributeEditorFactory(TaskDataModel model, TaskRepository taskRepository,
      IServiceLocator serviceLocator) {
    super(model, taskRepository, serviceLocator);
    this.model = model;
  }

  @Override
  public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {

    if (TaskAttribute.TYPE_MULTI_SELECT.equals(type)) {
      if (taskAttribute.getId().equals(YouTrackTaskDataHandler.TAG_PREFIX)) {
        CheckboxMultiAttributeEditor attributeEditor =
            new CheckboxMultiAttributeEditor(model, taskAttribute);
        attributeEditor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
        return attributeEditor;
      } else {
        CheckboxMultiSelectAttributeEditor attributeEditor =
            new CheckboxMultiSelectAttributeEditor(model, taskAttribute);
        attributeEditor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
        return attributeEditor;
      }
    }

    return super.createEditor(type, taskAttribute);
  }
}
