/**
@author: amarch
 */

package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ownedFieldBundle")
public class OwnedFieldBundleValues extends BundleValues {

    @XmlElement(name = "ownedField", type = OwnedFieldValue.class)
    public LinkedList<OwnedFieldValue> bundleValues;

    public LinkedList<OwnedFieldValue> getOwnedFields() {
	return bundleValues;
    }

    @Override
    public LinkedList<String> getValues() {
	LinkedList<String> values = new LinkedList<String>();
	if (bundleValues != null) {
	    for (BundleValue value : bundleValues) {
		values.add(value.getValue());
	    }
	}
	return values;
    }
}
