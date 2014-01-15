package com.jetbrains.youtrack.javarest.client;


import java.net.URL;
import java.util.ArrayList;

import javax.ws.rs.core.NewCookie;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class YouTrackClientFactory {

  private Client baseClient;

  private DefaultClientConfig defaultConfig;

  private static final String URL_PREFIX_HTTPS = "https://";

  private static final String URL_PREFIX_HTTP = "http://";

  public YouTrackClientFactory(Client baseClient) {
    this.baseClient = baseClient;
  }

  public YouTrackClientFactory() {
    defaultConfig = new DefaultClientConfig();
    this.baseClient = Client.create(defaultConfig);
    getClientFactory().addFilter(new LoggingFilter(System.out));
    handleCookies();
  }

  public void handleCookies() {

    // getConfig().getProperties().put(ApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);

    if (getClientFactory() != null) {
      getClientFactory().addFilter(new ClientFilter() {
        private ArrayList<Object> cookies;

        @Override
        public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
          if (cookies != null) {
            request.getHeaders().put("Cookie", cookies);
          }
          ClientResponse response = getNext().handle(request);
          if (response.getCookies() != null) {
            if (cookies == null) {
              cookies = new ArrayList<Object>();
            }

            for (int i = 0; i < response.getCookies().size(); i++) {
              NewCookie newCookie = response.getCookies().get(i);
              for (int j = 0; j < cookies.size(); j++) {
                Object oldCookie = cookies.get(j);
                if (oldCookie instanceof NewCookie) {
                  if (((NewCookie) oldCookie).getName().equals(newCookie.getName())) {
                    cookies.remove(j);
                    break;
                  }
                }
              }
            }
            cookies.addAll(response.getCookies());
          }
          return response;
        }
      });
    }
  }

  public YouTrackClient getClient(String baseUrlString) {
    URL baseUrl;
    if (baseUrlString.startsWith(URL_PREFIX_HTTPS) || baseUrlString.startsWith(URL_PREFIX_HTTP)) {
      try {
        baseUrl = new URL(baseUrlString);
        return new YouTrackClient(getClientFactory().resource(baseUrl.toURI()).path("/rest"));
      } catch (Exception e) {
        throw new RuntimeException("Repository URL is not valid.", e);
      }
    } else {
      try {
        baseUrl = new URL(URL_PREFIX_HTTPS + baseUrlString);
        return new YouTrackClient(getClientFactory().resource(baseUrl.toURI()).path("/rest"));
      } catch (Exception e) {
        throw new RuntimeException("Repository URL is not valid.", e);
      }
    }
  }

  public Client getClientFactory() {
    return baseClient;
  }

}
