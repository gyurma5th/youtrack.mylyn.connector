/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.ui;

import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.jetbrains.mylyn.yt.core.YouTrackConnector;
import com.jetbrains.youtrack.javarest.client.YouTrackProject;


public class NewIssueWizard extends NewTaskWizard implements INewWizard {

	Combo projectCombo;
	
	public NewIssueWizard(TaskRepository taskRepository, ITaskMapping taskSelection) {
		super(taskRepository, taskSelection);
		setWindowTitle("New YouTrack Issue");
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public void addPages() {
		
		addPage(new WizardPage("Select project name from repository") {
			
			@Override
			public void createControl(Composite parent) {
				Composite composite = new Composite(parent, SWT.BORDER);
				composite.setLayout(new GridLayout(2, false));

				Label label = new Label(composite, SWT.NONE);
				label.setText("Select project:");
				projectCombo = new Combo(composite, SWT.NONE);
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(label);
				
				setControl(composite);
				
				LinkedList<YouTrackProject> projects;
				try {
					projects = (LinkedList<YouTrackProject>) YouTrackConnector.getClient(getTaskRepository()).getProjects();
					if(projectCombo != null){
						for(YouTrackProject project : projects){
							projectCombo.add(project.getProjectShortName());
						}
					}
				} catch (CoreException e) {
					//ignore
				}

				
			}
		});
	}

	@Override
	protected ITaskMapping getInitializationData() {
		
		final String s;
		
		if(projectCombo.getSelectionIndex() != -1){
			s = projectCombo.getText();
		} else{
			s = "";
		}
		
		return new TaskMapping() {
			@Override
			public String getProduct() {
				return s;
			}
		};
	}

}
