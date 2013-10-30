package com.jetbrains.mylyn.yt.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.commons.workbench.EnhancedFilteredTree;
import org.eclipse.mylyn.commons.workbench.InPlaceCheckBoxTreeDialog;
import org.eclipse.mylyn.commons.workbench.SubstringPatternFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;


public class InPlaceAbleAdditionCheckBoxTreeDialog extends InPlaceCheckBoxTreeDialog {

  private Map<String, String> validValues;

  private CheckboxFilteredTree valueTree;

  private Set<String> selectedValues;

  private String dialogLabel;

  private class CheckboxFilteredTree extends EnhancedFilteredTree {

    public CheckboxFilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
      super(parent, treeStyle, filter);
    }

    @Override
    protected WorkbenchJob doCreateRefreshJob() {
      WorkbenchJob job = super.doCreateRefreshJob();
      job.addJobChangeListener(new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
          if (event.getResult() != null && event.getResult().isOK()
              && !getViewer().getTree().isDisposed()) {
            getViewer().setCheckedElements(selectedValues.toArray());
          }
        }
      });
      return job;
    }

    @Override
    protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
      return new CheckboxTreeViewer(parent, style);
    }

    @Override
    public CheckboxTreeViewer getViewer() {
      return (CheckboxTreeViewer) super.getViewer();
    }

  }

  public InPlaceAbleAdditionCheckBoxTreeDialog(Shell shell, Control openControl, List<String> values,
      Map<String, String> validValues, String dialogLabel) {
    super(shell, openControl, values, validValues, dialogLabel);
  }

  @Override
  protected Control createControl(Composite parent) {
    getShell().setText(dialogLabel);

    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = MARGIN_SIZE;
    layout.marginWidth = MARGIN_SIZE;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    composite.setLayout(layout);
    GridData gd =
        new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
    composite.setLayoutData(gd);

    valueTree =
        new CheckboxFilteredTree(composite, SWT.CHECK | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
            | SWT.BORDER, new SubstringPatternFilter());
    gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_BOTH);
    gd.heightHint = 175;
    gd.widthHint = 160;
    final CheckboxTreeViewer viewer = valueTree.getViewer();
    viewer.getControl().setLayoutData(gd);

    if (validValues != null) {

      viewer.setContentProvider(new ITreeContentProvider() {

        public Object[] getChildren(Object parentElement) {
          if (parentElement instanceof Map<?, ?>) {
            return ((Map<?, ?>) parentElement).keySet().toArray();
          }
          return null;
        }

        public Object getParent(Object element) {
          return null;
        }

        public boolean hasChildren(Object element) {
          return false;
        }

        public Object[] getElements(Object inputElement) {
          return getChildren(inputElement);
        }

        public void dispose() {}

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

      });


      // key difference is here
      valueTree.getTextSearchControl().addTraverseListener(new TraverseListener() {
        @Override
        public void keyTraversed(TraverseEvent e) {
          if (viewer.getTree().getItems().length == 0) {
            selectedValues.add(valueTree.getFilterControl().getText());
          }
        }
      });

      viewer.setLabelProvider(new LabelProvider() {
        @Override
        public String getText(Object element) {

          if (element instanceof String) {
            return validValues.get(element);
          }
          return super.getText(element);
        }
      });
      viewer.setInput(validValues);

      Set<String> invalidValues = new HashSet<String>();

      // Remove any currently entered invalid values
      for (String value : selectedValues) {
        if (!validValues.containsKey(value)) {
          invalidValues.add(value);
        }
      }

      // Remove any unselected values
      for (String value : validValues.keySet()) {
        if (!viewer.setChecked(value, true)) {
          invalidValues.add(value);
        }
      }

      selectedValues.removeAll(invalidValues);

      viewer.setCheckedElements(selectedValues.toArray());

    }

    viewer.addCheckStateListener(new ICheckStateListener() {

      public void checkStateChanged(CheckStateChangedEvent event) {
        if (event.getChecked()) {
          selectedValues.add((String) event.getElement());
        } else {
          selectedValues.remove(event.getElement());
        }
      }

    });


    return valueTree;
  }
}
