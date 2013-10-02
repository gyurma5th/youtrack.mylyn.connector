package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "state")
public class StateValue extends BundleValue{
	
	@XmlAttribute(name  = "isResolved")
	private String isResolved;
	
	@XmlValue
	private String value;

	public boolean isResolved() {
		return Boolean.parseBoolean(isResolved);
	}

	public String getValue() {
		return value;
	}
}