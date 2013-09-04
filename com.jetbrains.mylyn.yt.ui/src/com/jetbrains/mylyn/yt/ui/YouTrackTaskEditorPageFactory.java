/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;

import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;

public class YouTrackTaskEditorPageFactory extends AbstractTaskEditorPageFactory {

		@Override
		public boolean canCreatePageFor(TaskEditorInput input) {
			return (input.getTask().getConnectorKind().equals(YouTrackCorePlugin.CONNECTOR_KIND) || TasksUiUtil.isOutgoingNewTask(
					input.getTask(), YouTrackCorePlugin.CONNECTOR_KIND));
		}

		@Override
		public IFormPage createPage(TaskEditor editor) {
			return new YouTrackTaskEditorPage(editor);
		}

		@Override
		public Image getPageImage() {
			return CommonImages.getImage(TasksUiImages.REPOSITORY_SMALL);
		}

		@Override
		public String getPageText() {
			return "YouTrack";
		}
		
		@Override
		public int getPriority() {
			return PRIORITY_TASK;
		}

}