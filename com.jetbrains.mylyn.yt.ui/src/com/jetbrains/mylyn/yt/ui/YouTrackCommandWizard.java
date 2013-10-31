package com.jetbrains.mylyn.yt.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.jetbrains.mylyn.yt.core.YouTrackConnector;
import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.utils.IntellisenseItem;
import com.jetbrains.youtrack.javarest.utils.IntellisenseSearchValues;

public class YouTrackCommandWizard extends Wizard {

  private YouTrackCommandDialogPage commandDialogPage;

  private TaskData taskData;

  private TaskRepository taskRepository;

  private TaskEditor editor;

  private static final String CANT_SUBMIT = "Can't submit incorrect command.";

  private class YouTrackCommandDialogPage extends WizardPage {

    private Text commandBoxText;

    private final String KEY_PRESS = "Ctrl+Space";

    private LinkedList<IntellisenseItem> items;

    private Map<String, IntellisenseItem> itemByNameMap = new HashMap<String, IntellisenseItem>();

    private IntellisenseSearchValues intellisense;

    private SimpleContentProposalProvider scp;


    protected YouTrackCommandDialogPage(String pageName) {
      super(pageName);
      setTitle(pageName);
      setPageComplete(false);
    }

    @Override
    public void createControl(Composite parent) {

      Composite composite = new Composite(parent, SWT.NONE);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
      GridLayout layout = new GridLayout(1, false);
      composite.setLayout(layout);

      GridData gd = new GridData(SWT.FILL);
      gd.grabExcessHorizontalSpace = true;
      gd.horizontalAlignment = SWT.FILL;
      composite.setLayoutData(gd);

      Label commandBoxLabel = new Label(composite, SWT.FILL);
      commandBoxLabel.setText("Command for "
          + YouTrackConnector.getRealIssueId(getTaskData().getTaskId(), getTaskRepository()) + ": "
          + getTaskData().getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue());

      commandBoxText = new Text(composite, SWT.SINGLE | SWT.FILL);
      commandBoxText.setLayoutData(gd);

      commandBoxText.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          setErrorMessage(null);
        }
      });

      try {
        intellisense = getClient().intellisenseSearchValues(commandBoxText.getText());
        scp = new SimpleContentProposalProvider(intellisense.getFullOptions());
        KeyStroke ks = KeyStroke.getInstance(KEY_PRESS);
        ContentProposalAdapter adapter =
            new ContentProposalAdapter(commandBoxText, new TextContentAdapter(), scp, ks, null);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
        adapter.addContentProposalListener(new IContentProposalListener() {

          @Override
          public void proposalAccepted(IContentProposal proposal) {
            insertAcceptedProposal(proposal);
          }
        });
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      commandBoxText.addKeyListener(new KeyAdapter() {
        public void keyReleased(KeyEvent ke) {
          intellisense =
              getClient().intellisenseSearchValues(commandBoxText.getText(),
                  commandBoxText.getCaretPosition());
          items = intellisense.getIntellisenseItems();
          for (int ind = 0; ind < items.size(); ind++) {
            itemByNameMap.put(items.get(ind).getFullOption(), items.get(ind));
          }
          scp.setProposals(intellisense.getFullOptions());
        }
      });

      setControl(composite);
    }

    private void insertAcceptedProposal(IContentProposal proposal) {
      IntellisenseItem item = itemByNameMap.get((proposal.getContent()));
      String beforeInsertion = commandBoxText.getText();
      String afterInsertion =
          beforeInsertion.substring(0, item.getCompletionPositions().getStart())
              + proposal.getContent()
              + beforeInsertion.substring(item.getCompletionPositions().getEnd());
      commandBoxText.setText(afterInsertion);
      commandBoxText.setSelection(Integer.parseInt(item.getCaret()));
    }


    @Override
    public boolean isPageComplete() {
      return true;
    }
  }

  public YouTrackCommandWizard(TaskData data, TaskRepository repository, TaskEditor editor) {
    super();
    setWindowTitle("Command Dialog");
    setTaskData(data);
    setTaskRepository(repository);
    setEditor(editor);
  }

  @Override
  public boolean performFinish() {
    return true;
  }

  public boolean finishPressed() {
    if (commandDialogPage.commandBoxText.getText().trim().length() == 0) {
      commandDialogPage.setErrorMessage(CANT_SUBMIT);
      return false;
    } else {
      try {
        getClient().applyCommand(
            YouTrackConnector.getRealIssueId(getTaskData().getTaskId(), taskRepository),
            commandDialogPage.commandBoxText.getText());
        return true;
      } catch (RuntimeException e) {
        commandDialogPage.setErrorMessage(CANT_SUBMIT);
        return false;
      }
    }
  }

  @Override
  public void addPages() {
    commandDialogPage = new YouTrackCommandDialogPage("Apply Command");
    addPage(commandDialogPage);
  }

  public TaskData getTaskData() {
    return taskData;
  }

  public void setTaskData(TaskData taskData) {
    this.taskData = taskData;
  }

  public TaskRepository getTaskRepository() {
    return taskRepository;
  }

  public void setTaskRepository(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  private YouTrackClient getClient() {
    return YouTrackConnector.getClient(getTaskRepository());
  }

  public TaskEditor getEditor() {
    return editor;
  }

  public void setEditor(TaskEditor editor) {
    this.editor = editor;
  }

}
