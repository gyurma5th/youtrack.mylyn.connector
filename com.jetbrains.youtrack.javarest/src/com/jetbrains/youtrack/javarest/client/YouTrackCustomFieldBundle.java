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

    public YouTrackCustomFieldBundle() {
	this.setName(null);
    }

    public YouTrackCustomFieldBundle(String name) {
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
	if (bundleValues != null) {
	    return ((BundleValues) bundleValues).getValues();
	}
	return null;
    }

    public T getBundleValuesFromClient(YouTrackClient client) {
	YouTrackCustomFieldType type = YouTrackCustomFieldType
		.getTypeByName(cfType);
	if (type != null) {
	    try {
		switch (type) {
		case ENUM_SINGLE:
		case ENUM_MULTI:
		    return (T) client.getEnumerationBundleValues(name);
		case BUILD_SINGLE:
		case BUILD_MULTI:
		    return (T) client.getBuildBundleValues(name);
		case OWNED_SINGLE:
		case OWNED_MULTI:
		    return (T) client.getOwnedFieldBundleValues(name);
		case STATE:
		    return (T) client.getStateBundleValues(name);
		case VERSION_SINGLE:
		case VERSION_MULTI:
		    return (T) client.getVersionBundleValues(name);
		case USER_SINGLE:
		case USER_MULTI:
		    return (T) client.getUserBundleValues(name);
		    // TODO: where is "group" rest api methods?
		default:
		    return null;
		}
	    } catch (Exception e) {
		// TODO:
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
