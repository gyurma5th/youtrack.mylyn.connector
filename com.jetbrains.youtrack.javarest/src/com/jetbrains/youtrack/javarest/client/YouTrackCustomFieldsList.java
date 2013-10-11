/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.client;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "projectCustomFieldRefs")
public class YouTrackCustomFieldsList {
	
	private LinkedList<YouTrackCustomField> customFields = new LinkedList<>();
	
	@XmlElement(name = "projectCustomField", type = YouTrackCustomField.class)
	public LinkedList<YouTrackCustomField> getCustomFields(){
		return customFields;
	}

}
