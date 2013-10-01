/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;

import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.jetbrains.mylyn.yt.core.YouTrackConnector;
import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.client.YouTrackProject;

public class YouTrackQueryPage extends AbstractRepositoryQueryPage2 {

	private Combo projectCombo;

	public YouTrackQueryPage(TaskRepository repository, IRepositoryQuery query) {
		super("youtrack", repository, query);
		setTitle("YouTrack Search");
		setDescription("Specify search parameters.");
	}

	@Override
	protected void createPageContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Project:");
		
		projectCombo = new Combo(composite, SWT.NONE);
		
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(label);
	}

	@Override
	protected void doRefresh() {
		try {
			LinkedList<YouTrackProject> projects = (LinkedList<YouTrackProject>) getClient().getProjects();
			projectCombo.removeAll();
			for (YouTrackProject project : projects) {
				projectCombo.add(project.getProjectShortName());
			}
		} catch (CoreException e) {
			System.err.println("Exception while refreshing" + e.toString());
		}
		
	}

	private YouTrackClient getClient() throws CoreException {
		return ((YouTrackConnector) getConnector()).getClient(getTaskRepository());
	}

	@Override
	protected boolean hasRepositoryConfiguration() {
		return true;
	}

	@Override
	protected boolean restoreState(IRepositoryQuery query) {
		String project = query.getAttribute(YouTrackCorePlugin.QUERY_KEY_PROJECT);
		if (project != null) {
			projectCombo.setText(project);
		}
		return true;
	}

	@Override
	public void applyTo(IRepositoryQuery query) {
		if (getQueryTitle() != null) {
			query.setSummary(getQueryTitle());
		}
		query.setAttribute(YouTrackCorePlugin.QUERY_KEY_PROJECT, projectCombo.getText());
	}

}
