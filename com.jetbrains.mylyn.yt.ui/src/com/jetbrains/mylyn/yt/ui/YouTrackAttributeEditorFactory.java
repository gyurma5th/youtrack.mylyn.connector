/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.ui.services.IServiceLocator;

public class YouTrackAttributeEditorFactory extends AttributeEditorFactory {
	private final TaskDataModel model;

	public YouTrackAttributeEditorFactory(TaskDataModel model, TaskRepository taskRepository, IServiceLocator serviceLocator) {
		super(model, taskRepository, serviceLocator);
		this.model = model;
	}

	/*@Override
	public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {
		
		if (TaskAttribute.TYPE_MULTI_SELECT.equals(type)) {
			CheckboxMultiSelectAttributeEditor attributeEditor = new CheckboxMultiSelectAttributeEditor(model,
					taskAttribute);
			attributeEditor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
			return attributeEditor;
		}
		
		if (TaskAttribute.TYPE_PERSON.equals(type)) {
			return new PersonAttributeEditor(model, taskAttribute) {
				@Override
				public String getValue() {
					if (isReadOnly()) {
						IRepositoryPerson repositoryPerson = getAttributeMapper().getRepositoryPerson(
								getTaskAttribute());
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
					IRepositoryPerson repositoryPerson = getAttributeMapper().getRepositoryPerson(getTaskAttribute());
					if (repositoryPerson != null) {
						if (isReadOnly()) {
							if (repositoryPerson.getPersonId() != null  
									&& repositoryPerson.getPersonId() != "" 
									&& repositoryPerson.getPersonId() != " ") {
								getControl().setToolTipText(repositoryPerson.getPersonId());
							}
						} else {
							// add tooltip with user display name for editbox in which we just display user id
							if (repositoryPerson.getName() != null  
									&& repositoryPerson.getName() != "" 
									&& repositoryPerson.getName() != " ") {
								if (getText() != null) {
									getText().setToolTipText(repositoryPerson.getName());
								}
							}
						}
					}
				}
			};
		}
		return super.createEditor(type, taskAttribute);
	}*/
}
