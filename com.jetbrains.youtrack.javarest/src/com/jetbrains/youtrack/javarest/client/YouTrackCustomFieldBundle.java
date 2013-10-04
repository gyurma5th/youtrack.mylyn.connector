/**
@author: Alexander Marchuk
*/

package com.jetbrains.youtrack.javarest.client;

import java.util.LinkedList;

import com.jetbrains.youtrack.javarest.client.YouTrackCustomField.YouTrackCustomFieldType;
import com.jetbrains.youtrack.javarest.utils.BundleValues;


public class YouTrackCustomFieldBundle<T extends BundleValues> {
	
	private String name;
	
	private String cfType;
	
	private LinkedList<String> values;
	
	private T bundleValues;
	
	public YouTrackCustomFieldBundle(String name){
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return cfType;
	}

	public void setType(String type) {
		this.cfType = type;
	}

	public LinkedList<String> getValues() {
		if(bundleValues != null){
			return ((BundleValues) bundleValues).getValues();
		}
		return null;
	}

	public T getBundleValuesFromClient(YouTrackClient client){
		YouTrackCustomFieldType type = YouTrackCustomFieldType.getTypeByName(cfType);
		if(type != null){
			try{
				switch(type.getName()){
					case "enum[1]" :
					case "enum[*]" :
						return (T) client.getEnumerationBundleValues(name);
					case "build[1]" :
					case "build[*]" :
						return (T) client.getBuildBundleValues(name);
					case "ownedField[1]" :
					case "ownedField[*]" :
						return (T) client.getOwnedFieldBundleValues(name);
					case "state[1]" : 
						return (T) client.getStateBundleValues(name);
					case "version[1]" :
					case "version[*]" :
						return (T) client.getVersionBundleValues(name);
					case "user[1]" :
					case "user[*]" :
						return (T) client.getUserBundleValues(name);
					//TODO: where is "group" rest api methods?
					default: 
						return null;
				}
			} catch(Exception e){
			//TODO:	
			}
		}
		return null;
	}

	public T getBundleValues() {
		return bundleValues;
	}

	public void setBundleValues(T bundleValues) {
		this.bundleValues = bundleValues;
	}

}
