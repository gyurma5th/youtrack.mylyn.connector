
/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;

import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.internal.tasks.ui.wizards.Messages;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.jetbrains.mylyn.yt.core.YouTrackConnector;
import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.utils.SavedSearch;
import com.jetbrains.youtrack.javarest.utils.UserSavedSearch;

public class YouTrackFastQueryPage extends AbstractRepositoryQueryPage2{
	
	private TaskRepository repository;
	
	private LinkedList<SavedSearch> searches;
		
	private Combo savedSearchesCombo;
	
	private Text numberOfIssues;
	
	public YouTrackFastQueryPage(String pageName, TaskRepository repository, IRepositoryQuery query) {
		super("youtrack.fast.query.page", repository, query);
		this.repository = repository;
		setTitle("YouTrack Fast Query");
		setDescription("Choose saved search or customize query on the next page.");
	}

	@Override
	protected void doRefreshControls(){
		doRefresh();
	}
	

	@Override
	protected boolean hasRepositoryConfiguration() {
		return true;
	}

	@Override
	protected boolean restoreState(IRepositoryQuery query) {
		String filter = query.getAttribute(YouTrackCorePlugin.QUERY_KEY_FILTER);
		if (filter != null) {
			for(SavedSearch savedSearch : searches){
				if(savedSearch.getSearchText().equals(filter)){
					savedSearchesCombo.setText(savedSearch.getName());
					break;
				}
			}
		}
		return true;
	}

	@Override
	public void applyTo(IRepositoryQuery query) {
		if (getQueryTitle() != null) {
			query.setSummary(getQueryTitle());
		}
		if(savedSearchesCombo.getText() != null){
			query.setAttribute(YouTrackCorePlugin.QUERY_KEY_FILTER, 
					searches.get(savedSearchesCombo.getSelectionIndex()).getSearchText());
		}
	}

	@Override
	protected void createPageContent(SectionComposite parent) {
		
		Composite composite = new Composite(parent.getContent(), SWT.BORDER);
		composite.setLayout(new GridLayout(2, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Saved Search:");
		savedSearchesCombo = new Combo(composite, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(label);
		
		fillSearches();
		
		Composite numberOfIssuesComposite = new Composite(parent.getContent(), SWT.BORDER);
		numberOfIssuesComposite.setLayout(new GridLayout(2, false));
		
		Label labelIssues = new Label(numberOfIssuesComposite, SWT.NONE);
		labelIssues.setText("Issues in query: ");
		
		numberOfIssues = new Text(numberOfIssuesComposite, SWT.SINGLE);
		numberOfIssues.setEnabled(false);
		
		savedSearchesCombo.addListener(SWT.Selection, new Listener() {
	        @Override
	        public void handleEvent(Event arg0) {
	        	
	        	if(getQueryTitle() == null || getQueryTitle().length() == 0){
	        		setQueryTitle(savedSearchesCombo.getText());
	        	}
	        	
	        	int queryIssuesAmount;
				try {
					queryIssuesAmount = getClient().getNumberOfIssues(searches.get(savedSearchesCombo.getSelectionIndex()).getSearchText());
					numberOfIssues.setText(new Integer(queryIssuesAmount).toString());
				} catch (CoreException e) {
					numberOfIssues.setText("");
					throw new RuntimeException(e.getMessage());
				}
	        }
	    });
		
	}
	
	private YouTrackClient getClient() throws CoreException {
		return YouTrackConnector.getClient(getTaskRepository());
	}
	
	protected void doRefresh() {
		savedSearchesCombo.removeAll();
		numberOfIssues.setText("");
		fillSearches();
		for(SavedSearch search : this.searches){
			savedSearchesCombo.add(search.getName());
		}
	}
	
	protected void fillSearches(){
		try{
			if(repository.getUserName() != null){
					searches = new LinkedList<>();
					LinkedList<UserSavedSearch> userSearches = getClient().getSavedSearchesForUser(repository.getUserName());
					for(UserSavedSearch userSearch : userSearches){
						searches.add(userSearch.convertIntoSavedSearch());
					}
			} else {
				searches = getClient().getSavedSearches();
			}
		} catch (CoreException e) {
			throw new RuntimeException("Exception while refreshing" + e.toString());
		} 
	}
	
	
	@Override
	public boolean isPageComplete() {
		if(this.getQueryTitle() != null && this.getQueryTitle().length() > 0 && savedSearchesCombo.getSelectionIndex() != -1){
			return true;
		}
        return false;
    }
	
	@Override
	public boolean canFlipToNextPage(){
		return true;
	}
	
}
