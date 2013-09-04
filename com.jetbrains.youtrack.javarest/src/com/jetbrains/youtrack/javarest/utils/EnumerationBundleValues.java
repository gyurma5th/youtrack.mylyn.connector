/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;

@XmlRootElement(name = "enumeration")
public class EnumerationBundleValues {
	
	private LinkedList<String> values = new LinkedList<String>();

	@XmlElement(name = "value", type = String.class)
	public LinkedList<String> getValues() {
		return values;
	}

}
