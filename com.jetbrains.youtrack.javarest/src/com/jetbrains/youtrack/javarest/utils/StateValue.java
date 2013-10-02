package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "state")
public class StateValue {
	
	@XmlAttribute(name  = "isResolved")
	private String isResolved;

	public boolean isResolved() {
		return Boolean.parseBoolean(isResolved);
	}
}