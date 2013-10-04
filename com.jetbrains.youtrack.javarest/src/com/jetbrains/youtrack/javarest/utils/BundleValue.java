package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class BundleValue {
	
	private String value;
	
	public String getValue() {
		return value;
	}
	
}
