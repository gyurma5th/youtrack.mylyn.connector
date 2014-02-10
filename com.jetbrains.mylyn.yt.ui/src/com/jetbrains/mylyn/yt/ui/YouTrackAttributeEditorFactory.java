/**
 * @author: amarch
 */

package com.jetbrains.mylyn.yt.ui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.mylyn.internal.tasks.ui.editors.PersonAttributeEditor;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
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
      return new PeriodTextAttributeEditor(model, taskAttribute, emptyText, taskRepository);
    } else if (TaskAttribute.TYPE_PERSON.equals(type)) {
      if (TaskAttribute.TYPE_PERSON.equals(type)) {
        return new PersonAttributeEditor(model, taskAttribute) {
          @Override
          public String getValue() {
            if (isReadOnly()) {
              IRepositoryPerson repositoryPerson =
                  getAttributeMapper().getRepositoryPerson(getTaskAttribute());
              if (repositoryPerson != null) {
                final String name = repositoryPerson.getName();
                if (name != null) {
                  return name;
                } else {
                  return repositoryPerson.getPersonId();
                }
              }
            }

            return super.getValue();
          }

          @Override
          public void createControl(Composite parent, FormToolkit toolkit) {
            super.createControl(parent, toolkit);
            IRepositoryPerson repositoryPerson =
                getAttributeMapper().getRepositoryPerson(getTaskAttribute());
            if (repositoryPerson != null) {
              if (isReadOnly()) {
                if (!StringUtils.isBlank(repositoryPerson.getPersonId())) {
                  getControl().setToolTipText(repositoryPerson.getPersonId());
                }
              } else {
                // add tooltip with user display name for editbox in which we just display user id
                if (!StringUtils.isBlank(repositoryPerson.getName())) {
                  if (getText() != null) {
                    getText().setToolTipText(repositoryPerson.getName());
                  }
                }
              }
            }
          }
        };
      }
    }

    return super.createEditor(type, taskAttribute);
  }
}
