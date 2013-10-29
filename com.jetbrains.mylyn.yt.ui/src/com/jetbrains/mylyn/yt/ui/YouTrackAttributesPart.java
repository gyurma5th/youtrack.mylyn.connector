package com.jetbrains.mylyn.yt.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.jetbrains.mylyn.yt.core.YouTrackTaskDataHandler;

public class YouTrackAttributesPart extends AbstractTaskEditorPart {

  private static final int COLUMN_MARGIN = 5;

  public YouTrackAttributesPart() {
    setPartName("Attributes");
  }

  private void addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute) {
    if (attribute != null || "".equals(attribute)) {
      AbstractAttributeEditor editor = createAttributeEditor(attribute);
      if (editor != null) {
        editor.createLabelControl(composite, toolkit);
        GridDataFactory.defaultsFor(editor.getLabelControl()).indent(COLUMN_MARGIN, 0)
            .applyTo(editor.getLabelControl());
        editor.createControl(composite, toolkit);
        getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
        if (editor instanceof YouTrackAttributeEditor) {
          GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).hint(130, 95)
              .applyTo(editor.getControl());
        } else {
          GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP)
              .applyTo(editor.getControl());
        }
      }
    }
  }

  @Override
  public void createControl(Composite parent, FormToolkit toolkit) {
    Section section = createSection(parent, toolkit, true);
    final Composite attributesComposite = toolkit.createComposite(section);
    GridLayout layout = new GridLayout(2, false);
    layout.marginWidth = 5;
    attributesComposite.setLayout(layout);

    addAttribute(attributesComposite, toolkit,
        getTaskData().getRoot().getMappedAttribute(TaskAttribute.PRODUCT));

    for (String key : getTaskData().getRoot().getAttributes().keySet()) {
      if (getTaskData().getRoot().getMappedAttribute(key).getMetaData().getKind() != null
          && getTaskData().getRoot().getMappedAttribute(key).getMetaData().getKind()
              .equals(YouTrackTaskDataHandler.CUSTOM_FIELD_KIND)) {
        addAttribute(attributesComposite, toolkit, getTaskData().getRoot().getMappedAttribute(key));
      }
    }

    addAttribute(attributesComposite, toolkit,
        getTaskData().getRoot().getMappedAttribute(TaskAttribute.COMPONENT));

    addAttribute(attributesComposite, toolkit,
        getTaskData().getRoot().getMappedAttribute(YouTrackTaskDataHandler.TAG_PREFIX));



    // ToolBar toolbar = new ToolBar(attributesComposite, SWT.FLAT);
    // ToolItem itemDrop = new ToolItem(toolbar, SWT.DROP_DOWN);
    // itemDrop.setText("drop menu");
    //
    // itemDrop.addSelectionListener(new SelectionAdapter() {
    //
    // Menu dropMenu = null;
    //
    // @Override
    // public void widgetSelected(SelectionEvent e) {
    // if (dropMenu == null) {
    // dropMenu = new Menu(attributesComposite.getShell(), SWT.POP_UP | SWT.SEARCH);
    // attributesComposite.getShell().setMenu(dropMenu);
    // MenuItem itemCheck = new MenuItem(dropMenu, SWT.CHECK);
    // itemCheck.setText("checkbox");
    // MenuItem itemRadio = new MenuItem(dropMenu, SWT.RADIO);
    // itemRadio.setText("radio1");
    // MenuItem itemRadio2 = new MenuItem(dropMenu, SWT.RADIO);
    // itemRadio2.setText("radio2");
    // }
    //
    // if (e.detail == SWT.ARROW) {
    // // Position the menu below and vertically aligned with the the drop down tool button.
    // final ToolItem toolItem = (ToolItem) e.widget;
    // final ToolBar toolBar = toolItem.getParent();
    //
    // Point point = toolBar.toDisplay(new Point(e.x, e.y));
    // dropMenu.setLocation(point.x, point.y);
    // dropMenu.setVisible(true);
    // }
    //
    // }
    //
    // });


    toolkit.paintBordersFor(attributesComposite);
    section.setClient(attributesComposite);
    setSection(toolkit, section);
  }
}
