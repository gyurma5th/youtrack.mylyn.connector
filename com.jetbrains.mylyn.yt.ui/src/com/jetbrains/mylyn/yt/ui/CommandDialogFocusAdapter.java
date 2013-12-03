package com.jetbrains.mylyn.yt.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.jetbrains.youtrack.javarest.client.YouTrackClient;
import com.jetbrains.youtrack.javarest.utils.IntellisenseItem;
import com.jetbrains.youtrack.javarest.utils.IntellisenseSearchValues;

public class CommandDialogFocusAdapter implements FocusListener {

  private Job autocomletionJob;

  private final String KEY_PRESS = "Ctrl+Space";

  private LinkedList<IntellisenseItem> items;

  private Map<String, IntellisenseItem> itemByNameMap = new HashMap<String, IntellisenseItem>();

  private IntellisenseSearchValues intellisense;

  private SimpleContentProposalProvider scp;

  private ContentProposalAdapterOpenable adapter;

  private Timer timer;

  private Text widgetText;

  public Text issuesCountText;

  public int queryIssuesAmount;

  public boolean isCountIssuses = false;

  public YouTrackClient client;

  // in milliseconds
  private int updateTime = 200;

  private int showDelay = 500;

  private String searchSequence;

  private long lastTryTime = 0;

  public CommandDialogFocusAdapter(YouTrackClient client, boolean isCountIssues,
      Text issuesCountText) {
    this.client = client;
    this.isCountIssuses = isCountIssues;
    this.issuesCountText = issuesCountText;
  }

  @Override
  public void focusLost(FocusEvent e) {
    if (autocomletionJob != null) {
      autocomletionJob.cancel();
      timer.cancel();
    }
  }

  private void syncWithUi(final boolean needOpen) {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        if (needOpen) {
          scp.setProposals(intellisense.getFullOptions());
          openPopupProposals();
        } else {
          adapter.closeProposalPopup();
          if (isCountIssuses && issuesCountText != null) {
            issuesCountText.setText("");
          }
        }

        if (isCountIssuses && issuesCountText != null) {
          if (queryIssuesAmount == -1) {
            issuesCountText.setText("Can't get number of issues. Please try another query.");
          } else if (queryIssuesAmount == 1) {
            issuesCountText.setText("1 issue");
          } else {
            issuesCountText.setText(queryIssuesAmount + " issues");
          }
        }
      }
    });
  }

  public class ContentProposalAdapterOpenable extends ContentProposalAdapter {

    public ContentProposalAdapterOpenable(Control control,
        IControlContentAdapter controlContentAdapter, IContentProposalProvider proposalProvider,
        KeyStroke keyStroke, char[] autoActivationCharacters) {
      super(control, controlContentAdapter, proposalProvider, keyStroke, autoActivationCharacters);
    }

    public void openProposalPopup() {
      super.openProposalPopup();
    }

    public void closeProposalPopup() {
      super.closeProposalPopup();
    }

  }

  class CheckModification extends TimerTask {

    public Text widgetText;

    public String searchSequence2;

    public int caret;

    public CheckModification(Text widgetText) {
      this.widgetText = widgetText;
      searchSequence2 = searchSequence;
    }

    public void run() {
      if (widgetText.isDisposed()) {
        autocomletionJob.cancel();
        timer.cancel();
        return;
      } else {

        Display.getDefault().syncExec(new Runnable() {
          public void run() {
            if (searchSequence == null || !searchSequence.equals(widgetText.getText())) {
              searchSequence2 = widgetText.getText();
              caret = widgetText.getCaretPosition();
            }
          }
        });

        if (searchSequence == null || !searchSequence.equals(searchSequence2)) {
          searchSequence = searchSequence2;
          lastTryTime = System.currentTimeMillis();
          intellisense = getClient().intellisenseSearchValues(searchSequence2, caret);

          if (isCountIssuses && issuesCountText != null) {
            queryIssuesAmount = getClient().getNumberOfIssues(searchSequence2);
          }

          items = intellisense.getIntellisenseItems();
          for (int ind = 0; ind < items.size(); ind++) {
            itemByNameMap.put(items.get(ind).getFullOption(), items.get(ind));
          }
          syncWithUi(false);
          return;
        } else {
          if (lastTryTime > 0 && lastTryTime + showDelay > System.currentTimeMillis()) {
            syncWithUi(true);
          }
        }
      }
    }
  }

  @Override
  public void focusGained(FocusEvent e) {

    if (e.getSource() instanceof Text) {
      widgetText = (Text) e.getSource();
    }

    try {
      intellisense = getClient().intellisenseSearchValues(widgetText.getText());
      scp = new SimpleContentProposalProvider(intellisense.getFullOptions());
      KeyStroke ks = KeyStroke.getInstance(KEY_PRESS);
      adapter =
          new ContentProposalAdapterOpenable(widgetText, new TextContentAdapter(), scp, ks, null);
      adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_IGNORE);
      adapter.addContentProposalListener(new IContentProposalListener() {

        @Override
        public void proposalAccepted(IContentProposal proposal) {
          insertAcceptedProposal(proposal);
        }
      });
    } catch (Exception e1) {
      throw new RuntimeException(e1);
    }

    autocomletionJob = new Job("command.autocompletion.proposals.job") {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        timer = new Timer();
        timer.schedule(new CheckModification(widgetText), 0, updateTime);
        return Status.OK_STATUS;
      }
    };
    autocomletionJob.setUser(true);
    autocomletionJob.schedule();
  }


  private void insertAcceptedProposal(IContentProposal proposal) {
    IntellisenseItem item = itemByNameMap.get((proposal.getContent()));
    String beforeInsertion = widgetText.getText();
    String afterInsertion =
        beforeInsertion.substring(0, item.getCompletionPositions().getStart())
            + proposal.getContent()
            + beforeInsertion.substring(item.getCompletionPositions().getEnd());
    widgetText.setText(afterInsertion);
    widgetText.setSelection(Integer.parseInt(item.getCaret()));
  }

  private void openPopupProposals() {
    adapter.openProposalPopup();
  }

  private YouTrackClient getClient() {
    return client;
  }
}
