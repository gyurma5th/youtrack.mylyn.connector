package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class BundleValue {
	
	private String value;
	
	public String getValue() {
		return value;
	}
	
//	public abstract boolean isResolved();
	
}
