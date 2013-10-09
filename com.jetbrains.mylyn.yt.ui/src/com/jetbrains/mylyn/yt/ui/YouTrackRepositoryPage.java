/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;
import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;

import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;

public class YouTrackRepositoryPage extends AbstractRepositorySettingsPage {

	public YouTrackRepositoryPage(TaskRepository taskRepository) {
		super("YouTrack Connector Settings", "Specify YouTrack host (e.g. 'me.myjetbrains.com/youtrack/')", taskRepository);
		setNeedsAnonymousLogin(true);
		setNeedsAdvanced(false);
		setNeedsEncoding(false);
		setNeedsHttpAuth(false);
		setNeedsProxy(false);
	}

	@Override
	public void applyTo(TaskRepository repository) {
		super.applyTo(repository);
		String location = getLocation(repository).getAbsolutePath();
		repository.setProperty(YouTrackCorePlugin.REPOSITORY_KEY_PATH, location);
	}

	private File getLocation(TaskRepository repository) {
		File root = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		return new File(root, repository.getRepositoryUrl());
	}

	@Override
	protected void createAdditionalControls(Composite parent) {
		//ignore
	}

	@Override
	public String getConnectorKind() {
		return YouTrackCorePlugin.CONNECTOR_KIND;
	}

	@Override
	protected Validator getValidator(final TaskRepository repository) {
		return new Validator() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				String location = repository.getRepositoryUrl();
				String user = repository.getUserName();
				String passwd = repository.getPassword();
				try{
					YouTrackClient client = YouTrackClient.createClient(location);
					if(!client.login(user, passwd)){
						throw new CoreException(new Status(IStatus.ERROR, YouTrackUiPlugin.ID_PLUGIN, "Credentials are not valid"));
					}
				}
				catch (Exception  e) {
					throw new CoreException(new Status(IStatus.ERROR, YouTrackUiPlugin.ID_PLUGIN, NLS.bind(
							"Host ''{0}'' or credentials are not valid for YT host", location)));
				}
			}
		};
	}

	@Override
	protected boolean isValidUrl(String url) {
		return super.isValidUrl(url);
	}

	@Override
	protected void repositoryTemplateSelected(RepositoryTemplate template) {
		repositoryLabelEditor.setStringValue(template.label);
		setUrl(template.repositoryUrl);
		setAnonymous(template.anonymous);
		getContainer().updateButtons();
	}

}
