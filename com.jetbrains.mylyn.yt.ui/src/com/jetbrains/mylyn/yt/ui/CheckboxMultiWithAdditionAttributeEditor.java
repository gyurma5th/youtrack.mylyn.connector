package com.jetbrains.mylyn.yt.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.mylyn.commons.ui.dialogs.AbstractInPlaceDialog;
import org.eclipse.mylyn.commons.ui.dialogs.IInPlaceDialogListener;
import org.eclipse.mylyn.commons.ui.dialogs.InPlaceDialogEvent;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.CheckboxMultiSelectAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class CheckboxMultiWithAdditionAttributeEditor extends CheckboxMultiSelectAttributeEditor {

  private Text valueText;

  private Composite parent;

  private Button button;

  private boolean suppressRefresh;

  private InPlaceAbleAdditionCheckBoxTreeDialog selectionDialog;

  private Map<String, String> validValues;

  public CheckboxMultiWithAdditionAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
    super(manager, taskAttribute);
  }

  @Override
  public void createControl(Composite parent, FormToolkit toolkit) {
    if (isReadOnly()) {
      valueText = new Text(parent, SWT.FLAT | SWT.READ_ONLY | SWT.WRAP);
      valueText.setFont(EditorUtil.TEXT_FONT);
      toolkit.adapt(valueText, false, false);
      valueText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
      valueText.setToolTipText(getDescription());
      refresh();
      setControl(valueText);
    } else {
      this.parent = parent;

      Composite composite = toolkit.createComposite(parent);
      composite.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
      GridLayout layout = new GridLayout(2, false);
      layout.marginWidth = 0;
      layout.marginBottom = 0;
      layout.marginLeft = 0;
      layout.marginRight = 0;
      layout.marginTop = 0;
      layout.marginHeight = 0;
      composite.setLayout(layout);

      valueText = toolkit.createText(composite, "", SWT.FLAT | SWT.WRAP); //$NON-NLS-1$
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false)
          .applyTo(valueText);
      valueText.setFont(EditorUtil.TEXT_FONT);
      valueText.setEditable(false);
      valueText.setToolTipText(getDescription());

      button = toolkit.createButton(composite, "", SWT.ARROW | SWT.DOWN); //$NON-NLS-1$
      GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.TOP).applyTo(button);
      button.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          List<String> values = getValues();
          validValues = getAttributeMapper().getOptions(getTaskAttribute());

          selectionDialog =
              new InPlaceAbleAdditionCheckBoxTreeDialog(WorkbenchUtil.getShell(), button, values, validValues,
                  NLS.bind("Select ", getLabel()));

          selectionDialog.addEventListener(new IInPlaceDialogListener() {

            public void buttonPressed(InPlaceDialogEvent event) {
              suppressRefresh = true;
              try {
                if (event.getReturnCode() == Window.OK
                    || event.getReturnCode() == AbstractInPlaceDialog.ID_CLEAR) {
                  Set<String> newValues = selectionDialog.getSelectedValues();
                  newValues.removeAll((Collection<?>) validValues.keySet());
                  for (String newValue : newValues) {
                    System.err.println(newValue);
                    getTaskAttribute().putOption(newValue, newValue);
                  }
                  validValues = getAttributeMapper().getOptions(getTaskAttribute());
                  setValues(new ArrayList<String>(selectionDialog.getSelectedValues()));
                  refresh();
                }
              } finally {
                suppressRefresh = false;
              }
            }
          });
          selectionDialog.open();
        }
      });

      toolkit.adapt(valueText, false, false);
      refresh();
      setControl(composite);
    }
  }
}
