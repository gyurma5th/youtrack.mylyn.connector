package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

public class BundleValues {
	
	private LinkedList<? extends BundleValue> bundleValues;
	
	/*public LinkedList<String> getValues() {
		LinkedList<String> values = new LinkedList<>();
		if(bundleValues != null){
			for(BundleValue value : bundleValues){
				values.add(value.getValue());
			}
		}
		return values;
	}
	
	public void setBundleValues(LinkedList<? extends BundleValue> bv) {
		this.bundleValues = bv;
	}*/
	public LinkedList<String> getValues(){
		return null;
	}
}
