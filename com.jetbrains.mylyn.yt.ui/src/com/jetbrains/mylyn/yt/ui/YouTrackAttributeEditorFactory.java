/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.ui.services.IServiceLocator;

import com.jetbrains.mylyn.yt.core.YouTrackRepositoryConnector;
import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class YouTrackAttributeEditorFactory extends AttributeEditorFactory {

  private final TaskDataModel model;

  private final TaskRepository taskRepository;

  public YouTrackAttributeEditorFactory(TaskDataModel model, TaskRepository taskRepository,
      IServiceLocator serviceLocator) {
    super(model, taskRepository, serviceLocator);
    this.model = model;
    this.taskRepository = taskRepository;
  }

  @Override
  public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {

    if (TaskAttribute.TYPE_MULTI_SELECT.equals(type)) {
      CheckboxMultiSelectOpenableAttributeEditor attributeEditor =
          new CheckboxMultiSelectOpenableAttributeEditor(model, taskAttribute);
      attributeEditor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
      return attributeEditor;
    } else if (TaskAttribute.TYPE_DATE.equals(type)) {
      String emptyText;
      try {
        emptyText =
            YouTrackRepositoryConnector
                .getProject(taskRepository,
                    model.getTaskData().getRoot().getAttribute(TaskAttribute.PRODUCT).getValue())
                .getCustomFieldsMap().get(YouTrackTaskDataHandler.getNameFromLabel(taskAttribute))
                .getEmptyText();
      } catch (NullPointerException e) {
        emptyText = "";
      }
      return new CustomFieldDateAttributeEditor(model, taskAttribute, emptyText);
    } else if (YouTrackTaskDataHandler.TYPE_PERIOD.equals(type)) {
      String emptyText;
      try {
        emptyText =
            YouTrackRepositoryConnector
                .getProject(taskRepository,
                    model.getTaskData().getRoot().getAttribute(TaskAttribute.PRODUCT).getValue())
                .getCustomFieldsMap().get(YouTrackTaskDataHandler.getNameFromLabel(taskAttribute))
                .getEmptyText();
      } catch (NullPointerException e) {
        emptyText = "";
      }
      return new PeriodTextAttributeEditor(model, taskAttribute, emptyText);
    }

    return super.createEditor(type, taskAttribute);
  }
}
