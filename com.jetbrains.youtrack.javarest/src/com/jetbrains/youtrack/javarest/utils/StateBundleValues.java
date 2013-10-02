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
	private LinkedList<StateValue> stateValues;
	
	public LinkedList<String> getValues() {
		LinkedList<String> values = new LinkedList<>();
		for(StateValue value : stateValues){
			values.add(value.getValue());
		}
		return values;
	}

	public LinkedList<StateValue> getStateValues() {
		return stateValues;
	}
}
