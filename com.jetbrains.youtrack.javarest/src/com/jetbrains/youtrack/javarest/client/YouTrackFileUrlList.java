/**
@author: amarch
 */

package com.jetbrains.youtrack.javarest.client;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fileUrls")
public class YouTrackFileUrlList {

	private LinkedList<YouTrackFileUrl> urls = new LinkedList<YouTrackFileUrl>();

	@XmlElement(name = "fileUrl", type = YouTrackFileUrl.class)
	public LinkedList<YouTrackFileUrl> getFileUrls() {
		return urls;
	}

}
