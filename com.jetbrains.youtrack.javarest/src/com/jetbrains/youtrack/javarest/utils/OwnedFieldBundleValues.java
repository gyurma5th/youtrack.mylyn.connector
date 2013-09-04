/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ownedFieldBundle")
public class OwnedFieldBundleValues {
	
	@XmlElement(name = "ownedField", type = OwnedField.class)
	public LinkedList<OwnedField> ownedFields;
	
	public LinkedList<OwnedField> getOwnedFields() {
		return ownedFields;
	}
	
	public LinkedList<String> getValues() {
		LinkedList<String> values = new LinkedList<>();
		for(OwnedField ownedField : ownedFields){
			values.add(ownedField.getValue());
		}
		return values;
	}

}
