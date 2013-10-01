/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.client;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name  = "param")
public class CustomFieldParam {
	
	private String name;
	
	private String value;

	@XmlAttribute(name  = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name  = "value")
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	

}
