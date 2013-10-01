/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "userBundle")
public class UserBundleValues {
	
	private LinkedList<String> values = new LinkedList<String>();

	@XmlElement(name = "user", type = String.class)
	public LinkedList<String> getValues() {
		return values;
	}

}

