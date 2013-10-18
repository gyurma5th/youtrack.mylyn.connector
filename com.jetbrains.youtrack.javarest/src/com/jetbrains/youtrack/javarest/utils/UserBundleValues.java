/**
@author: amarch
 */

package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "userBundle")
public class UserBundleValues extends BundleValues {

    @XmlElement(name = "user", type = UserValue.class)
    private LinkedList<UserValue> bundleValues;

    public LinkedList<UserValue> getStateValues() {
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
