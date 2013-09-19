/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.client;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.jetbrains.youtrack.javarest.client.YouTrackCustomField.YouTrackCustomFieldType;

@XmlRootElement(name="project")
public class YouTrackProject {
	
	private String projectFullName;
	
	private String projectShortName;

	@XmlAttribute(name="name")
	public String getProjectFullName() {
		return projectFullName;
	}

	public void setProjectFullName(String projectFullName) {
		this.projectFullName = projectFullName;
	}
 
	@XmlAttribute(name="shortName")
	public String getProjectShortName() {
		return projectShortName;
	}

	public void setProjectShortName(String projectShortName) {
		this.projectShortName = projectShortName;
	}
	
	private HashMap<String, YouTrackCustomField> customFieldsMap = new HashMap<>();
	
	private Date customFieldsUpdatedDate;
	
	private boolean customFieldsUpdated = false;
	
	public HashMap<String, YouTrackCustomField> getCustomFieldsMap() {
		return customFieldsMap;
	}
	
	public Collection<YouTrackCustomField> getCustomFields() {
		return customFieldsMap.values();
	}

	public void setCustomFieldsMap(HashMap<String, YouTrackCustomField> customFields) {
		customFieldsMap = customFields;
	}
	
	public void addCustomField(YouTrackCustomField field){
		if(customFieldsMap != null){
			customFieldsMap.put(field.getName(), field);
		} else {
			customFieldsMap = new HashMap<>();
			customFieldsMap.put(field.getName(), field);
		}
	}

	public boolean isCustomFieldsUpdated() {
		return customFieldsUpdated;
	}

	public void setCustomFieldsUpdated(boolean customFieldsUpdated) {
		this.customFieldsUpdated = customFieldsUpdated;
	}

	public Date getCustomFieldsUpdatedDate() {
		return customFieldsUpdatedDate;
	}

	public void setCustomFieldsUpdatedDate(
			Date customFieldsUpdatedDate) {
		this.customFieldsUpdatedDate = customFieldsUpdatedDate;
	}
	
	public void updateCustomFields(YouTrackClient client){
		
		if(client != null){
			
			setCustomFieldsMap(new HashMap<String, YouTrackCustomField>());
			
			if(projectShortName != null && !projectShortName.equals("")){
				for(YouTrackCustomField field : client.getProjectCustomFields(projectShortName)){
					YouTrackCustomField fullField;
						fullField = client.getProjectCustomField(projectShortName, field.getName());
						if(!YouTrackCustomFieldType.getTypeByName(fullField.getType()).isSimple()){
							LinkedList<String> values = fullField.findBundle().getBundleValuesFromClient(client);
							fullField.getBundle().setValues(values);
						}
						addCustomField(fullField);
				}
			} else {
				updateCustomFields(client);
			}
			
			setCustomFieldsUpdated(true);
			setCustomFieldsUpdatedDate(new Date());
		}
	}
	
	public String getBothNames(){
		if(getProjectFullName() != null){
			if(getProjectShortName() != null){
				return getProjectFullName() + " [" + getProjectShortName() + "]";
			} else {
				return getProjectFullName();
			}
		} else {
			return "";
		}
	}
	
	public static String getShortNameFromBoth(String both){
		if(both.contains("[") && both.contains("]")){
			return both.substring(both.indexOf("[") + 1, both.indexOf("]"));
		} else {
			return both;
		}
	}
}
