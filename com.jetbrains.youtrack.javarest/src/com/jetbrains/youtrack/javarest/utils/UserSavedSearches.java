/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "queries")
public class UserSavedSearches{

	
	private LinkedList<UserSavedSearch> searches;

	@XmlElement(name = "query")
	public LinkedList<UserSavedSearch> getUserSearches() {
		return searches;
	}

	public void setUserSearches(LinkedList<UserSavedSearch> searches) {
		this.searches = searches;
	}
	
	public LinkedList<String> getUserSearchesNames() {
		LinkedList<String> names  = new LinkedList<>();
		for(UserSavedSearch search : searches){
			names.add(search.getName());
		}
		return names;
	}
	
}
