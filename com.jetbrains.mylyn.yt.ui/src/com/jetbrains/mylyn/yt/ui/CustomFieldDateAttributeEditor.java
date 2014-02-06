/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.jetbrains.mylyn.yt.ui;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.commons.workbench.forms.DatePicker;
import org.eclipse.mylyn.internal.tasks.core.TaskActivityUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.DateAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Steffen Pingel
 * @author Robert Elves
 * @author Alexander Marchuk
 */
public class CustomFieldDateAttributeEditor extends DateAttributeEditor {

  private DatePicker datePicker;

  private boolean showTime;

  private boolean showDateRelative;

  private Text text;

  private String emptyValue;

  public CustomFieldDateAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute,
      String emptyValue) {
    super(manager, taskAttribute);
    this.emptyValue = emptyValue;
  }

  @Override
  public void createControl(Composite composite, FormToolkit toolkit) {
    if (isReadOnly()) {
      text = new Text(composite, SWT.FLAT | SWT.READ_ONLY);
      text.setFont(EditorUtil.TEXT_FONT);
      toolkit.adapt(text, false, false);
      text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
      text.setText(getTextValue());
      text.setToolTipText(getDescription());
      setControl(text);
    } else {
      datePicker = new DatePicker(composite, SWT.FLAT, getTextValue(), showTime, 0);
      datePicker.setFont(EditorUtil.TEXT_FONT);
      datePicker.setToolTipText(getDescription());
      datePicker.setDateFormat(DateFormat.getDateInstance(DateFormat.MEDIUM));
      updateDatePicker();

      Text text = null;
      Button button = null;
      for (Control control : datePicker.getChildren()) {
        if (control instanceof Text) {
          text = (Text) control;
        } else if (control instanceof Button) {
          button = (Button) control;
          break;
        }
      }

      final Button pickerButton = button;

      if (text != null) {
        text.addMouseListener(new MouseListener() {

          @Override
          public void mouseUp(MouseEvent e) {
            // TODO Auto-generated method stub
          }

          @Override
          public void mouseDown(MouseEvent e) {
            pickerButton.notifyListeners(SWT.Selection, null);
          }

          @Override
          public void mouseDoubleClick(MouseEvent e) {
            // TODO Auto-generated method stub
          }
        });

        text.addModifyListener(new ModifyListener() {

          @Override
          public void modifyText(ModifyEvent e) {
            Text widget = (Text) e.widget;
            if (widget.getText().equals("Choose Date")) {
              widget.setText(emptyValue == null ? "" : emptyValue);
            }
          }
        });
      }

      datePicker.addPickerSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          Calendar cal = datePicker.getDate();
          if (cal != null) {
            if (!showTime) {
              TaskActivityUtil.snapStartOfDay(cal);
            }
            Date value = cal.getTime();
            if (!value.equals(getValue())) {
              setValue(value);
            }
          } else {
            if (getValue() != null) {
              setValue(null);
            }
            datePicker.setDate(null);
          }
        }

      });

      GridDataFactory.fillDefaults().hint(120, SWT.DEFAULT).grab(false, false).applyTo(datePicker);
      datePicker.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
      toolkit.adapt(datePicker, false, false);

      setControl(datePicker);
    }
  }

  private void updateDatePicker() {
    if (getValue() != null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(getValue());
      datePicker.setDate(cal);
    }
  }

  @Override
  protected void decorateIncoming(Color color) {
    if (datePicker != null) {
      datePicker.setBackground(color);
    }
  }

  public boolean getShowTime() {
    return showTime;
  }

  private String getTextValue() {
    Date date = getValue();
    if (date != null) {
      if (getShowTime()) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
      } else {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
      }
    } else {
      return getTaskAttribute().getValue(); //$NON-NLS-1$
    }
  }

  public Date getValue() {
    return getAttributeMapper().getDateValue(getTaskAttribute());
  }

  public void setShowTime(boolean showTime) {
    this.showTime = showTime;
  }

  public void setValue(Date date) {
    getAttributeMapper().setDateValue(getTaskAttribute(), date);
    attributeChanged();
  }

  @Override
  public void refresh() {
    if (text != null && !text.isDisposed()) {
      text.setText(getTextValue());
    }
    if (datePicker != null && !datePicker.isDisposed()) {
      updateDatePicker();
    }
  }

  @Override
  public boolean shouldAutoRefresh() {
    return true;
  }

  public boolean getShowDateRelative() {
    return showDateRelative;
  }

  public void setShowDateRelative(boolean showDateRelative) {
    this.showDateRelative = showDateRelative;
  }

}
