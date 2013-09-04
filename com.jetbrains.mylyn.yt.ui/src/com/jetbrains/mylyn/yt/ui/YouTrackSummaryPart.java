/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorSummaryPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.forms.widgets.FormToolkit;



public class YouTrackSummaryPart extends TaskEditorSummaryPart{
	
	private void addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute, boolean shouldInitializeGridData) {
		AbstractAttributeEditor editor = createAttributeEditor(attribute);
		if (editor != null) {
			editor.setReadOnly(true);
			editor.setDecorationEnabled(false);

			editor.createLabelControl(composite, toolkit);
			if (shouldInitializeGridData) {
				GridDataFactory.defaultsFor(editor.getLabelControl())
						.indent(EditorUtil.HEADER_COLUMN_MARGIN, 0)
						.applyTo(editor.getLabelControl());
			}

			editor.createControl(composite, toolkit);
			getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
		}
	}
	
	
	@Override
	protected Composite createHeaderLayout(Composite parent, FormToolkit toolkit) {
		Composite composite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		
		// Reporter name
		TaskAttribute attribute = getTaskData().getRoot().getMappedAttribute(TaskAttribute.USER_REPORTER);
		if (attribute != null) {
			addAttribute(composite, toolkit, attribute, true);
		}
		
		// Date of submission
		attribute = getTaskData().getRoot().getMappedAttribute(TaskAttribute.DATE_CREATION);
		if(attribute != null){
			addAttribute(composite, toolkit, attribute, true);
		}
		
		// Date of update
		attribute = getTaskData().getRoot().getMappedAttribute(TaskAttribute.DATE_MODIFICATION);
		if(attribute != null){
			addAttribute(composite, toolkit, attribute, true);
		}
		
		// Issue URL
		attribute = getTaskData().getRoot().getMappedAttribute("ISSUE_URL");
		if(attribute != null){
			//TODO: remove hack with attribute text write normal usage
			String url = attribute.getValue();
			attribute.setValue(" ");
		    addAttribute(composite, toolkit, attribute, true);
		    attribute.setValue(url);
			Link link = new Link(composite, SWT.NONE);
		    link.setText(url);
		    link.setToolTipText("Open link in internal Eclipse browser.");
		    link.addSelectionListener(new SelectionAdapter(){
		        @Override
		        public void widgetSelected(SelectionEvent e) {
		               try {
		            	   final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
		            			   getTaskData().getRoot().getId());
		            	   browser.openURL(new URL(e.text));
		              } 
		             catch (PartInitException ex) {
		                // TODO Auto-generated catch block
		                 ex.printStackTrace();
		            } 
		            catch (MalformedURLException ex) {
		                // TODO Auto-generated catch block
		                ex.printStackTrace();
		            }
		        }
		    });
		}

//		Layout layout = composite.getLayout();
		if (layout instanceof GridLayout) {
			GridLayout gl = (GridLayout) layout;
			gl.numColumns = composite.getChildren().length;

			if (gl.numColumns == 0) {
				gl.numColumns = 4;
			}
		}
		
		return composite;
	}

}