/**
@author: Alexander Marchuk
*/

package com.jetbrains.youtrack.javarest.client;

import java.util.LinkedList;

import com.jetbrains.youtrack.javarest.client.YouTrackCustomField.YouTrackCustomFieldType;
import com.jetbrains.youtrack.javarest.utils.BundleValues;


public class YouTrackCustomFieldBundle {
	
	private String name;
	
	private String cfType;
	
	private LinkedList<String> values;
	
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
		return values;
	}

	public void setValues(LinkedList<String> values) {
		this.values = values;
	}
	
	public LinkedList<String> getBundleValuesFromClient(YouTrackClient client){
		YouTrackCustomFieldType type = YouTrackCustomFieldType.getTypeByName(this.cfType);
		if(type != null){
			try{
				switch(type.getName()){
					case "enum[1]" :
					case "enum[*]" :
						return client.getEnumerationBundleValues(this.name);
					case "build[1]" :
					case "build[*]" :
						return client.getBuildBundleValues(this.name);
					case "ownedField[1]" :
					case "ownedField[*]" :
						return client.getOwnedFieldBundleValues(this.name);
					case "state[1]" : 
						return  ((BundleValues) client.stateBundleValues(this.name)).getValues();
					case "version[1]" :
					case "version[*]" :
						return client.getVersionBundleValues(this.name);
					case "user[1]" :
					case "user[*]" :
						return client.getUserBundleValues(this.name);
					//TODO: where is "group"? rest api
					default: 
						return new LinkedList<String>();
				}
			} catch(Exception e){
			//TODO:	
			}
		}
		return new LinkedList<String>();
	}

}
