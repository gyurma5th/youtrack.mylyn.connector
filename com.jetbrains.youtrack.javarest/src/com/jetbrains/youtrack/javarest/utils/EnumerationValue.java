package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "value")
public class EnumerationValue extends BundleValue {
	
	@XmlValue
	private String value;
	
	public String getValue() {
		return value;
	}
}
