package com.jetbrains.youtrack.javarest.client;

import java.util.Date;

public class IssueAttachment {

	private String name;

	private String url;

	private String description;

	private String id;

	private String login;

	private Date creationDate;

	public IssueAttachment(String name, String url, String description, String id, String author, Date created) {
		super();
		this.id = id;
		login = author;
		creationDate = created;
		this.setName(name);
		this.setUrl(url);
		this.setDescription(description);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}
	
	public String getAuthorLogin() {
		return login;
	}

	public Date getCreated() {
		return creationDate;
	}


}
