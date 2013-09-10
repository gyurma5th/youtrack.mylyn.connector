/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;

import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.commons.workbench.forms.SectionComposite;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.jetbrains.mylyn.yt.core.YouTrackConnector;
import com.jetbrains.mylyn.yt.core.YouTrackCorePlugin;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.client.YouTrackProject;
import com.jetbrains.youtrack.javarest.utils.IntellisenseItem;
import com.jetbrains.youtrack.javarest.utils.IntellisenseSearchValues;
import com.jetbrains.youtrack.javarest.utils.SavedSearch;
import com.jetbrains.youtrack.javarest.utils.UserSavedSearch;

public class YouTrackRepositoryQueryPage extends AbstractRepositoryQueryPage2{
	
	private TaskRepository repository;
	
	private LinkedList<SavedSearch> searches;
		
	private Combo savedSearchesCombo;
	
	private Text numberOfIssues;
	
	private Button customizeQueryCheckbox;
	
	private Combo projectCombo;
	
	private Text searchBoxText;
	
	private static String KEY_PRESS = "Ctrl+Space";

	private static String[] options;
	
	private static LinkedList<IntellisenseItem> items;
	
	private static IntellisenseSearchValues intellisense;
	
	private static SimpleContentProposalProvider scp;

	
	public YouTrackRepositoryQueryPage(String pageName, TaskRepository repository, IRepositoryQuery query) {
		super("youtrack.repository.query.page", repository, query);
		this.repository = repository;
		setTitle("YouTrack Repository Query");
		setDescription("Choose saved search or select customize query.");
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
		filter = query.getAttribute(YouTrackCorePlugin.QUERY_KEY_FILTER);
		if(filter != null){
			searchBoxText.setText(filter);
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
		
		setMessage("Choose saved search or select customize query.");
		
		if (getQueryTitle() != null) {
			query.setSummary(getQueryTitle());
		}
		if(searchBoxText.getText() != null){
			query.setAttribute(YouTrackCorePlugin.QUERY_KEY_FILTER, searchBoxText.getText());
		}
	}

	@Override
	protected void createPageContent(SectionComposite parent) {
		
		customizeQueryCheckbox = new Button(parent.getContent(), SWT.CHECK);
		customizeQueryCheckbox.setText("Checkbox 1");
		
		createFastQueryCompositeContent(parent);
		createCustomizeQueryContent(parent);
		
	}
	
	protected void createFastQueryCompositeContent(SectionComposite parent){
//		Composite composite = new Composite(parent.getContent(), SWT.BORDER);
//		composite.setLayout(new GridLayout(2, false));
		
		Group composite = new Group(parent.getContent(), SWT.NONE);
		composite.setText("Saved search");
		GridData gd = new GridData(SWT.FILL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(gd);
		

//		Label label = new Label(composite, SWT.NONE);
//		label.setText("Saved Search:");
		savedSearchesCombo = new Combo(composite, SWT.FILL);
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(label);
		savedSearchesCombo.setLayoutData(gd);
		
		fillSearches();
		
		Composite numberOfIssuesComposite = new Composite(composite, SWT.BORDER);
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
	
	protected void createCustomizeQueryContent(SectionComposite parent){
	
		Group composite = new Group(parent.getContent(), SWT.NONE);
		composite.setText("Enter query into search box (press Ctrl+Space for query completion).");
		GridData gd = new GridData(SWT.FILL);
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(gd);

		Listener countSuitableIssuesListener = new Listener() {
	        @Override
	        public void handleEvent(Event arg0) {
	        	countIssuesInListeners();
	        }
	    };
	    
	    SelectionListener countSuitableIssuesSelectionListener = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				countIssuesInListeners();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				countIssuesInListeners();
			}
		};
		
//		Label searchLabel = new Label(parent.getContent(), SWT.NONE);
//		searchLabel.setText("Search box:");
		
		searchBoxText = new Text(composite, SWT.SINGLE | SWT.FILL);
		searchBoxText.setLayoutData(gd);
		
		searchBoxText.addListener(SWT.CHANGED, countSuitableIssuesListener);
		
		try {
			intellisense = getClient().intellisenseSearchValues(searchBoxText.getText());
			options = intellisense.getOptions();
			scp = new SimpleContentProposalProvider(options);
            scp.setProposals(options);
            KeyStroke ks = KeyStroke.getInstance(KEY_PRESS);
            ContentProposalAdapter adapter = new ContentProposalAdapter(searchBoxText, new TextContentAdapter(), scp, ks, null);
            adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
            adapter.addContentProposalListener(new IContentProposalListener() {
				
				@Override
				public void proposalAccepted(IContentProposal proposal) {
					insertAcceptedProposal(proposal);
				}
			});
        }
        catch( Exception e )
        {
           //TODO: fix
        }
		
		searchBoxText.addKeyListener( new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke)
            {	
            	try {
            		intellisense = getClient().intellisenseSearchValues(searchBoxText.getText(), searchBoxText.getCaretPosition());
        			options = intellisense.getOptions();
        			items  = intellisense.getIntellisenseItems();
					scp.setProposals(options); 
				} catch (CoreException e) {
					// TODO Auto-generated catch block
				}
            }
        }
        );
		
		Composite numberOfIssuesComposite = new Composite(composite, SWT.BORDER);
		numberOfIssuesComposite.setLayout(new GridLayout(2, false));
		
		Label labelIssues = new Label(numberOfIssuesComposite, SWT.NONE);
		labelIssues.setText("Issues in query: ");
		numberOfIssues = new Text(numberOfIssuesComposite, SWT.SINGLE);
		numberOfIssues.setEnabled(false);

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
		
		searchBoxText.setText("");
		numberOfIssues.setText("");
		
		if(this.getPreviousPage() != null){
			IWizardPage previousPage = this.getPreviousPage();
			if(previousPage instanceof YouTrackFastQueryPage){
				this.setQueryTitle(((YouTrackFastQueryPage) previousPage).getQueryTitle());
			}
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
		return false;
	}
	
	public void recursiveSetEnabled(Control ctrl, boolean enabled) {
	   if (ctrl instanceof Composite) {
	      Composite comp = (Composite) ctrl;
	      for (Control c : comp.getChildren())
	         recursiveSetEnabled(c, enabled);
	   } else {
	      ctrl.setEnabled(enabled);
	   }
	}
	
	private void insertAcceptedProposal(IContentProposal proposal){
		IntellisenseItem item = items.get(Arrays.asList(options).indexOf(proposal.getContent()));
		String beforeInsertion = searchBoxText.getText();
		String afterInsertion = beforeInsertion.substring(0, item.getCompletionPositions().getStart()) + 
				proposal.getContent() + beforeInsertion.substring(item.getCompletionPositions().getEnd());
		searchBoxText.setText(afterInsertion);
		searchBoxText.setSelection(Integer.parseInt(item.getCaret()));
	}
	
	private void countIssuesInListeners(){
		try {
			int queryIssuesAmount = 
					((YouTrackConnector) getConnector()).queryIssuesAmount(null, searchBoxText.getText(), getTaskRepository());
			numberOfIssues.setText(new Integer(queryIssuesAmount).toString());
		} catch (CoreException e) {
			numberOfIssues.setText("");
			throw new RuntimeException(e.getMessage());
		}
	}
}
