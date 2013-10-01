package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractReplyToCommentAction;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorRichTextPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Steffen Pingel
 */
public class YouTrackDescriptionPart extends TaskEditorRichTextPart{

	public YouTrackDescriptionPart() {
		setPartName("Description");
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		if (getAttribute() == null) {
			return;
		}
		super.createControl(parent, toolkit);
	}

	@Override
	protected void fillToolBar(ToolBarManager toolBar) {
		if (!getTaskData().isNew()) {
			AbstractReplyToCommentAction replyAction = new AbstractReplyToCommentAction(getTaskEditorPage(), null) {
				@Override
				protected String getReplyText() {
					return getEditor().getValue();
				}
			};
			replyAction.setImageDescriptor(TasksUiImages.COMMENT_REPLY_SMALL);
			toolBar.add(replyAction);
		}
	}

	@Override
	public void initialize(AbstractTaskEditorPage taskEditorPage) {
		super.initialize(taskEditorPage);
		setAttribute(getModel().getTaskData().getRoot().getMappedAttribute(TaskAttribute.DESCRIPTION));
	}

}
