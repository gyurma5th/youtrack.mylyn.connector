/**
@author: amarch
*/

package com.jetbrains.mylyn.yt.core;

import java.util.List;

import com.jetbrains.youtrack.javarest.client.YouTrackProject;

public class YouTrackConfiguration {
	
	private List<YouTrackProject> projects;

	long updated = -1;

	public List<YouTrackProject> getProjects() {
		return projects;
	}

	void setProjects(List<YouTrackProject> projects) {
		this.projects = projects;
	}

}