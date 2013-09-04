/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;


import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;


public class YouTrackConnectorUi extends AbstractRepositoryConnectorUi {

	public YouTrackConnectorUi() {
	}

	@Override
	public String getConnectorKind() {
		return YouTrackCorePlugin.CONNECTOR_KIND;
	}

	@Override
	public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
		return new YouTrackRepositoryPage(taskRepository);
	}

	@Override
	public boolean hasSearchPage() {
		return false;
	}

	@Override
	public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping selection) {
		return new NewIssueWizard(repository, selection);
	}

	@Override
	public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
		RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
		wizard.addPage(new YouTrackFastQueryPage("youtrack.fast.query.page", repository, query));
		wizard.addPage(new YouTrackSearchQueryPage("youtrack.search.query.page", repository, query));
		return wizard;
	}

}

