/**
@author: Alexander Marchuk
*/

package com.jetbrains.youtrack.javarest.client;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Field{
	
	@XmlElement(name = "value")
	private LinkedList<String> value;
	
	private String name;
	
	private String type;

	public LinkedList<String> getValues() {
		return value;
	}

	public void setValue(LinkedList<String> value) {
		this.value = value;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@XmlAttribute(name = "type", namespace="http://www.w3.org/2001/XMLSchema-instance")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
		
}
