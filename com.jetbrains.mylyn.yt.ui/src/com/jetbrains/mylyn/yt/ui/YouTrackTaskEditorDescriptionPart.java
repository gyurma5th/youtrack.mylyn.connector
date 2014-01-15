package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorDescriptionPart;

public class YouTrackTaskEditorDescriptionPart extends TaskEditorDescriptionPart {

	public YouTrackTaskEditorDescriptionPart() {
		setPartName(Messages.TaskEditorDescriptionPart_Description);
	}
	
	@Override
	protected void fillToolBar(ToolBarManager toolBar) {
//		if (!getTaskData().isNew()) {
//			AbstractReplyToCommentAction replyAction = new AbstractReplyToCommentAction(getTaskEditorPage(), null) {
//				@Override
//				protected String getReplyText() {
//					return getEditor().getValue();
//				}
//			};
//			replyAction.setImageDescriptor(TasksUiImages.COMMENT_REPLY_SMALL);
//			toolBar.add(replyAction);
//		}
//		super.fillToolBar(toolBar);
	}

}
