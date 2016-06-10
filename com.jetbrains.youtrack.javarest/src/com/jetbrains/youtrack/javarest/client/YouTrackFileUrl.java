package com.jetbrains.youtrack.javarest.client;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fileUrl")
public class YouTrackFileUrl {
	/*<fileUrl id="114-4" 
	 * url="http://evh01790ws.evosoft.com:8080/_persistent/203_104%20IO-Liste%20UCOMI.zip?file=114-4&amp;v=0&amp;c=false"
	 *  name="203_104 IO-Liste UCOMI.zip"
	 *   authorLogin="Varga_György"
	 *    group="All Users" 
	 *    created="1463562641842"/>
	 * */

    @XmlAttribute
    public String url;

	private String name;
	
	private String id;
	
	private String authorLogin;
	private String group;
	private String created;

//	@XmlElement(name = "url")
//	public String getUrl() {
//		return url;
//	}
//
//	public void setUrl(String url) {
//		this.url = url;
//	}

	@XmlAttribute(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute(name = "id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name = "authorLogin")
	public String getAuthorLogin() {
		return authorLogin;
	}

	public void setAuthorLogin(String authorLogin) {
		this.authorLogin = authorLogin;
	}

	@XmlAttribute(name = "group")
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@XmlAttribute(name = "created")
	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

}
