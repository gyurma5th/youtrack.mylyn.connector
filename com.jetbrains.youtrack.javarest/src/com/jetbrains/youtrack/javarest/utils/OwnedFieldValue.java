/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "ownedField")
public class OwnedFieldValue extends BundleValue{
	
	@XmlValue
	private String value;
	
	@XmlAttribute(name = "owner")
	private String owner;
	
	@XmlAttribute(name = "description")
	private String description;
	
	public String getValue() {
		return value;
	}

	public String getOwner() {
		return owner;
	}

	public String getDescription() {
		return description;
	}
}
