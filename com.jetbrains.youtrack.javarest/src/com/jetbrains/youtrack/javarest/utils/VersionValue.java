package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "version")
public class VersionValue extends BundleValue {
	
	@XmlValue
	private String value;

	public String getValue() {
		return value;
	}
}
