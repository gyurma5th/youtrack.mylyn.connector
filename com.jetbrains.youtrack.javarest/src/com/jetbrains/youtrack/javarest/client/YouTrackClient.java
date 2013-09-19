package com.jetbrains.youtrack.javarest.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.jetbrains.youtrack.javarest.utils.BuildBundleValues;
import com.jetbrains.youtrack.javarest.utils.EnumerationBundleValues;
import com.jetbrains.youtrack.javarest.utils.ICallPost;
import com.jetbrains.youtrack.javarest.utils.IntellisenseItem;
import com.jetbrains.youtrack.javarest.utils.IntellisenseSearchValues;
import com.jetbrains.youtrack.javarest.utils.OwnedField;
import com.jetbrains.youtrack.javarest.utils.OwnedFieldBundleValues;
import com.jetbrains.youtrack.javarest.utils.SavedSearch;
import com.jetbrains.youtrack.javarest.utils.SavedSearches;
import com.jetbrains.youtrack.javarest.utils.StateBundleValues;
import com.jetbrains.youtrack.javarest.utils.UserBundleValues;
import com.jetbrains.youtrack.javarest.utils.UserSavedSearch;
import com.jetbrains.youtrack.javarest.utils.UserSavedSearches;
import com.jetbrains.youtrack.javarest.utils.VersionBundleValues;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.ApacheHttpClientConfig;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class YouTrackClient {

	private String username = null;
	
	private String password = null;

	private String HOST;

	private int PORT;

	private final String baseServerURL;

	private final static String DEFAULT_SCHEME = "http";

	private final static String DEFAULT_HOST = "localhost";

	private final static int DEFAULT_PORT = 80;

	private ClientConfig config;

	private ApacheHttpClient jerseyClient;

	private WebResource service;
	
	public YouTrackClient() {
		this.HOST = DEFAULT_HOST;
		this.PORT = DEFAULT_PORT;
		this.baseServerURL = DEFAULT_SCHEME + "://" + DEFAULT_HOST + ":"
				+ Integer.toString(DEFAULT_PORT) + "/rest";
		setClientConfigs();
	}

	public YouTrackClient(String hostname, int port, String scheme) {
		try{
			URI validator = new URI(buildBaseURL(hostname, port, scheme));
				
			this.HOST = hostname;
			this.PORT = port;
			baseServerURL = validator.toString();
				
		} catch (URISyntaxException e) {
			throw new  RuntimeException("Incorrect host or port, cant create new client");
		}

		setClientConfigs();
	}
	
	private String buildBaseURL(String hostname, int port, String scheme) throws URISyntaxException{
		
		StringBuilder uri = new StringBuilder(scheme);
		uri.append("://");
		
		if(new URI(uri.toString() + hostname).getHost().toString().equals(hostname)){
			uri.append(hostname).
				append(":").append(port);
		} else {
			if(hostname.contains("/")){
				String realHost = hostname.substring(0, hostname.indexOf("/"));
				String afterHostPart = hostname.substring(hostname.indexOf("/"));
				uri.append(realHost).
				append(":").append(port).
				append(afterHostPart);
			} else {
				throw new  RuntimeException("Incorrect host or port, cant create new client");
			}
		}
		
		if(!uri.toString().endsWith("/")){
			uri.append("/");
		} 
		uri.append("rest");
		return uri.toString();
	}
	
	private void setClientConfigs(){
		setConfig(new DefaultApacheHttpClientConfig());
		getConfig().getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);
		this.jerseyClient = ApacheHttpClient.create(getConfig());
		jerseyClient.addFilter(new LoggingFilter(System.out));
		service = jerseyClient.resource(this.getBaseServerURL());
	}
	
	public static YouTrackClient createClient(String url) {
		URI uri;
		try {
			if(url.contains("://")){
				uri = new URI(url);
			} else {
				uri = new URI(DEFAULT_SCHEME + "://" +url);
			}
			
			String host = "";
			if(uri.getHost() != null){
				host = uri.getHost();
			}
			if(uri.getPath() != null){
				host += uri.getPath();
			}
			
			String scheme = uri.getScheme();
			if(scheme == null){
				scheme = DEFAULT_SCHEME;
			}
			
			int port = uri.getPort();
			if(port == -1){
				port = DEFAULT_PORT;
			}
			
			return new YouTrackClient(host, port, scheme);
		} catch (URISyntaxException e) {
			throw new  RuntimeException("Incorrect host or port, can't create new client");
		}
	}

	public boolean login(final String username, final String password) {
		callPost(new ICallPost() {
			@Override
			public ClientResponse call() {
				if (username == null || password == null || "".equals(username)  || "".equals(password) ){
					throw new RuntimeException("Failed : NULL username or password ");
				} else {
					return service.path("/user/login").
							queryParam("login", username).
							queryParam("password", password).
							post(ClientResponse.class);
				}
			}
		}, 200, "Failed to login");
		
		this.setPassword(password);
		this.setUsername(username);
		return true;
	}
	
	public boolean loginWithCredentials() {
		callPost(new ICallPost() {
			@Override
			public ClientResponse call() {
				if(username == null || password == null){
					throw new RuntimeException("Failed to login with credentials : saved username or password is NULL ");
				} else {
					return service.path("/user/login")
							.queryParam("login", getUsername())
							.queryParam("password", getPassword())
							.post(ClientResponse.class);
				}
			}
		}, 200, "Failed to login with credentials");
		
		return true;
	}
	
	public boolean issueExist(String issueId){
		return service.path("/issue/").path(issueId).path("/exists").get(ClientResponse.class).getStatus() == 200;
	}

	public YouTrackIssue getIssue(String id) {
		if(id == null){
			throw new RuntimeException("Null issue id");
		} else {
			if (!issueExist(id)) {
				throw new RuntimeException("Issue with such id dont exist in tracker.");
			} else {
				return service.path("/issue/").path(id).accept("application/xml").get(YouTrackIssue.class);
			}
		}
			
	}

	public List<YouTrackIssue> getIssuesInProject(String projectname, String filter, int after, int max, long updatedAfter) {
		try {
			return service.path("/issue/byproject/").path(projectname).
					queryParam("filter", filter).
					queryParam("after", Integer.toString(after)).
					queryParam("max", Integer.toString(max)).
					queryParam("updatedAfter", Long.toString(updatedAfter)).
					accept("application/xml").
					get(YouTrackIssuesList.class).getIssues();
		} catch (RuntimeException e) {
			throw new RuntimeException("Exception while get list of issues in project :\n" + e.getMessage(), e);
		}
	}
	
	public List<YouTrackIssue> getIssuesInProject(String projectname, int max) {
			return getIssuesInProject(projectname, "", 0, max, 0);
	}
	
	/*
	 * returns 10 or less issues
	 */
	public List<YouTrackIssue> getIssuesInProject(String projectname){
		return getIssuesInProject(projectname, "", 0, 10, 0);
	}
	
	public String getBaseServerURL() {
		return baseServerURL;
	}

	public ClientConfig getConfig() {
		return config;
	}

	public void setConfig(ClientConfig config) {
		this.config = config;
	}
	
	public String getPassword() {
		if(password == null){
			throw new RuntimeException("Attempt to get null password.");
		} else {
			return password;
		}
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		if(username == null){
			throw new RuntimeException("Attemp to get null username");
		} else {
			return username;
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<YouTrackProject> getProjects(){
		try{
			return service.path("/project/all").
					accept("application/xml").
					get(YouTrackProjectsList.class).getProjects();
		} catch(Exception e){
			throw new RuntimeException("Exception while get list of projects\n" + e.getMessage());
		}
	}
	
	/**
	 * @param issue
	 * @return new issue id from tracker, if successfully uploaded
	 */
	public String putNewIssue(final YouTrackIssue issue){
		ClientResponse response = callPost(new ICallPost() {
			@Override
			public ClientResponse call() {
				if(issue != null && issue.getProjectName() != null && issue.getSummary() != null) {
					WebResource resource = service.path("/issue").
							queryParam("project", issue.getProjectName()).
							queryParam("summary", issue.getSummary());
					if(issue.getDescription() != null){
						resource = resource.queryParam("description", issue.getDescription());
					}
					return resource.put(ClientResponse.class);
				} else {
					throw new RuntimeException("Issue's project and summary can't be null.");
				}
			}
		}, 201, "Failed put new issue");
		
		return YouTrackIssue.getIdFromResponse(response);
	}

	public void deleteIssue(final String issueId){
		callPost(new ICallPost() {
			@Override
			public ClientResponse call() {
				if(issueId != null){
					return  service.path("/issue/").path(issueId).delete(ClientResponse.class);
				} else {
					throw new RuntimeException("Null issue id");
				}
			}
		}, 200, "Failed delete issue " + issueId);
	}
	
	public void applyCommand(final String issueId, final String command) {
		callPost(new ICallPost() {
			@Override
			public ClientResponse call() {
				if(issueId != null && command != null){
					return service.path("/issue/").path(issueId).path("/execute").
							queryParam("command", command).post(ClientResponse.class);
				} else {
					throw new RuntimeException("Null issue id or command while apply command.");
				}
			}
		}, 200, "Failed apply command " + command + " to issue " + issueId);
	}

	/**
	 * @param filterQuery
	 * @return number of relevant issues or all issues, if filter string is null
	 * return -1 if reach max number of attempts
	 */
	public int getNumberOfIssues(String filterQuery){
		WebResource resource = service.path("/issue/count");
		if(filterQuery != null){
			resource = resource.queryParam("filter", filterQuery);
		} else {
			resource = resource.queryParam("filter", "");
		}
		
		int number;
		int attemptCount = 0;
		while((number = resource.accept("application/xml").get(XmlNumberOfIssuesParser.class).getNumber()) == -1 && attemptCount++ < 20){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				break;
			}
		}
		return number;
	}
	
	@XmlRootElement(name="int") 
	private static class XmlNumberOfIssuesParser{
		
		/*TODO: fix, too many strings of code
		 *  for a simple action: get number from <int>1</int>
		 */
		
		@XmlValue
		private int number;
		
		private int getNumber(){
			return this.number;
		}
		
	}
	
	
	public List<YouTrackIssue> getIssuesByFilter(String filterQuery, int max) {
		WebResource resource;
		if(filterQuery != null){
			resource = service.path("/issue").queryParam("filter", filterQuery);
		} else {
			resource = service.path("/issue").queryParam("filter", "");
		}
		
		if(max != -1){
			resource = resource.queryParam("max", Integer.toString(max));
		}

		return resource.accept("application/xml").get(IssueCompactsList.class).getIssues();
	}
	
	
	public List<YouTrackIssue> getIssuesByFilter(String filterQuery) {
		return getIssuesByFilter(filterQuery, -1);
	}
	
	
	public LinkedList<YouTrackCustomField> getProjectCustomFields(String projectname){
		if(projectname != null){
			try{
				return service.path("/admin/project/").path(projectname).path("/customfield")
						.accept("application/xml").get(YouTrackCustomFieldsList.class).getCustomFields();
			} catch(Exception e){
				throw new RuntimeException("Exception while get project custom fields:\n" + e.getMessage());
			}
		} else{
			throw new RuntimeException("Null projectname while get project custom fields.");
		}
	}
	
	public YouTrackCustomField getProjectCustomField(String projectname, String fieldname){
		if(projectname != null && fieldname != null){
			try{
				return service.path("/admin/project/").path(projectname).
						path("/customfield/").path(fieldname).accept("application/xml").get(YouTrackCustomField.class);
			} catch(Exception e){
				throw new RuntimeException("Exception while get project custom field:\n" + e.getMessage());
			}
		} else{
			throw new RuntimeException("Null projectname or fieldname while get project custom field.");
		}
	}
	
	public Set<String> getProjectCustomFieldNames(String projectname){
		LinkedList<YouTrackCustomField> cfs =  getProjectCustomFields(projectname);
		Set<String> cfNames = new HashSet<>();
		
		for(YouTrackCustomField cf: cfs){
			cfNames.add(cf.getName());
		}
		return cfNames;
	}
	
	public LinkedList<String> getEnumerationBundleValues(String bundlename){
		return service.path("/admin/customfield/bundle/").path(bundlename).accept("application/xml").
				get(EnumerationBundleValues.class).getValues();
	}
	
	public LinkedList<String> getOwnedFieldBundleValues(String bundlename){
		return service.path("/admin/customfield/ownedFieldBundle/").path(bundlename).accept("application/xml").
				get(OwnedFieldBundleValues.class).getValues();
	}
	
	public LinkedList<OwnedField> getOwnedFields(String bundlename){
		return service.path("/admin/customfield/ownedFieldBundle/").path(bundlename).accept("application/xml").
				get(OwnedFieldBundleValues.class).getOwnedFields();
	}
	
	public LinkedList<String> getBuildBundleValues(String bundlename){
		return service.path("/admin/customfield/buildBundle/").path(bundlename).accept("application/xml").
				get(BuildBundleValues.class).getValues();
	}
	
	public LinkedList<String> getStateBundleValues(String bundlename){
		return service.path("/admin/customfield/stateBundle/").path(bundlename).accept("application/xml").
				get(StateBundleValues.class).getValues();
	}
	
	public LinkedList<String> getVersionBundleValues(String bundlename){
		return service.path("/admin/customfield/versionBundle/").path(bundlename).accept("application/xml").
				get(VersionBundleValues.class).getValues();
	}
	
	public LinkedList<String> getUserBundleValues(String bundlename){
		return service.path("/admin/customfield/userBundle/").path(bundlename).accept("application/xml").
				get(UserBundleValues.class).getValues();
	}
	
	public void addComment(final String issueId, final String comment){
		callPost(new ICallPost() {
			@Override
			public ClientResponse call() {
				if(issueId != null && comment != null){
					return service.path("/issue/").path(issueId).path("/execute").
							queryParam("comment", comment).
							accept("application/xml").
							post(ClientResponse.class);
				} else {
					throw new RuntimeException("Null issue id or comment body.");
				}
			}
		}, 200, "Failed add comment to issue " + issueId);
	}
	
	public String[] intellisenseOptions(String filter){
		return intellisenseOptions(filter, filter.length());
	}
	
	public LinkedList<IntellisenseItem> intellisenseItems(String filter){
		return intellisenseItems(filter, filter.length());
	}
	
	public String[] intellisenseOptions(String filter, int caret){
		return service.path("/issue/intellisense").
				queryParam("filter", filter).
				queryParam("caret", String.valueOf(caret)).
				accept("application/xml").
				get(IntellisenseSearchValues.class).getOptions();
	}
	
	public LinkedList<IntellisenseItem> intellisenseItems(String filter, int caret){
		return service.path("/issue/intellisense").
				queryParam("filter", filter).
				queryParam("caret", String.valueOf(caret)).
				accept("application/xml").
				get(IntellisenseSearchValues.class).getIntellisenseItems();
	}
	
	public IntellisenseSearchValues intellisenseSearchValues(String filter, int caret){
		return service.path("/issue/intellisense").
				queryParam("filter", filter).
				queryParam("caret", String.valueOf(caret)).
				accept("application/xml").
				get(IntellisenseSearchValues.class).getIntellisenseSearchValues();
	}
	
	public IntellisenseSearchValues intellisenseSearchValues(String filter){
		return intellisenseSearchValues(filter, filter.length());
	}
	
	public LinkedList<String> getSavedSearchesNames(){
		return service.path("/user/search").accept("application/xml").get(SavedSearches.class).getSearchNames();
	}
	
	private ClientResponse callPost(ICallPost call, int okStatus, String exceptionMessage) {
		ClientResponse response = call.call();
		if (response.getStatus() != okStatus) {
			throw new RuntimeException(exceptionMessage + "\nRESPONSE CODE: " + response.getStatus());
		}
		return response;
	}
	
	public LinkedList<SavedSearch> getSavedSearches(){
		return service.path("/user/search").accept("application/xml").get(SavedSearches.class).getSearches();
	}
	
	public SavedSearch getSavedSearch(String searchname){
		if(searchname != null){
			return service.path("/user/search/").path(searchname).accept("application/xml").get(SavedSearch.class);
		} else {
			throw new RuntimeException("Null saved search name.");
		}
	}
	
	public LinkedList<UserSavedSearch> getSavedSearchesForUser(String username){
		if(username != null){
			return service.path("/user/").path(username).path("/filter").
					accept("application/xml").get(UserSavedSearches.class).getUserSearches();
		} else {
			throw new RuntimeException("Can't get saved searches for null username.");
		}
	}
	
	public LinkedList<String> getSavedSearchesNamesForUser(String username){
		if(username != null){
			return service.path("/user/").path(username).path("/filter").
					accept("application/xml").get(UserSavedSearches.class).getUserSearchesNames();
		} else {
			throw new RuntimeException("Can't get saved searches for null username.");
		}
	}
	
	/**
	 * summary can't be empty by rest restriction
	 * @param issueId
	 * @param newSummary
	 * @param newDescription new description
	 * 						 if null, not changed
	 */
	public void updateIssueSummaryAndDescription(final String issueId, final String newSummary, final String newDescription){
		callPost(new ICallPost() {
			@Override
			public ClientResponse call() {
				WebResource resource = service.path("/issue/").path(issueId);
				if(newSummary != null && newSummary.length() > 0){
					resource = resource.queryParam("summary", newSummary);
					if(newDescription != null){
						resource = resource.queryParam("description", newDescription);
					}
				} else {
					throw new RuntimeException("Failed to update issue: summary cant be empty");
				}
				return resource.post(ClientResponse.class);
			}
		}, 200, "Failed to update issue description and summary ");
	}
	
	/**
	 * If issue not update fully, make incomplete update
	 * @param oldIssueId
	 * @param newIssue
	 */
	public void updateIssue(String oldIssueId, YouTrackIssue newIssue){
		if(oldIssueId != null){
			
			YouTrackIssue oldIssue = this.getIssue(oldIssueId);
			
			if(newIssue.getSummary() != null && newIssue.getSummary().length() > 0 &&
					(!oldIssue.getSummary().equals(newIssue.getSummary()) || (newIssue.getDescription() != null && 
							(oldIssue.getDescription() == null || !oldIssue.getDescription().equals(
									newIssue.getDescription()))))){
				updateIssueSummaryAndDescription(oldIssueId, newIssue.getSummary(), newIssue.getDescription());
			}
			
			String project = newIssue.getProjectName();
			Set<String> customFieldsNames = this.getProjectCustomFieldNames(project);
			
			StringBuilder command = new StringBuilder();
			for(String customFieldName : customFieldsNames){
				if(newIssue.getProperties().get(customFieldName) instanceof String){
					String newValue = newIssue.getProperties().get(customFieldName).toString();
					if(!oldIssue.getProperties().containsKey(customFieldName) || 
							!newValue.equals(oldIssue.getProperties().get(customFieldName).toString())){
						command.append(customFieldName + ": " + newValue + " ");
						
					}
				}
			}
			if(command.toString() != null){
				this.applyCommand(oldIssueId, command.toString());
			}
		} else {
			throw new RuntimeException("Null target issue id while update issue.");
		}
	}
	
}
	 
