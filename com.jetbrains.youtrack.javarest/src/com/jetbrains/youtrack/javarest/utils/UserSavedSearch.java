/**
@author: amarch
*/

package com.jetbrains.youtrack.javarest.utils;

import javax.xml.bind.annotation.XmlAttribute;

public class UserSavedSearch {

	private String name;
	
	private String query;

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
	public SavedSearch convertIntoSavedSearch(){
		SavedSearch search = new SavedSearch();
		search.setName(this.name);
		search.setSearchText(this.query);
		return search;
	}
	
}
