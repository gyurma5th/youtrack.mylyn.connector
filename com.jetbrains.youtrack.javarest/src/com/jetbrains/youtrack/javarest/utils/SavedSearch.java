/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "search")
public class SavedSearch{
	
	private	String text;
		
	private String name;

	@XmlValue
	public String getSearchText() {
		return text;
	}

	public void setSearchText(String search) {
		this.text = search;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
