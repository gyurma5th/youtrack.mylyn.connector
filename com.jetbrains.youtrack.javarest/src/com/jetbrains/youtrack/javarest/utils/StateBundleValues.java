/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "stateBundle")
public class StateBundleValues extends BundleValues{
	
	@XmlElement(name = "state", type = StateValue.class)
	private LinkedList<StateValue> bundleValues;

	public LinkedList<StateValue> getStateValues() {
		return bundleValues;
	}
	
	public LinkedList<String> getValues() {
		LinkedList<String> values = new LinkedList<>();
		if(bundleValues != null){
			for(BundleValue value : bundleValues){
				values.add(value.getValue());
			}
		}
		return values;
	}
}
