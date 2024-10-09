package com.axelor.apps.mattermost.mattermost;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public abstract class MattermostRest {

  protected CloseableHttpClient httpClient;
  protected static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected static final String GET_USER_END_POINT = "/api/v4/users/email/{email}";
  protected static final String GET_TEAM_END_POINT = "/api/v4/teams/name/{team_name}";
  protected static final String GET_CHANNEL_END_POINT = "/api/v4/channels/{channel_id}";
  protected static final String GET_CHANNEL_END_POINT_TEAM =
      "/api/v4/teams/{team_id}/channels/name/{channel_name}";
  protected static final String CREATE_TEAM_END_POINT = "/api/v4/teams";
  protected static final String CREATE_CHANNEL_END_POINT = "/api/v4/channels";
  protected static final String CREATE_USER_END_POINT = "/api/v4/users";
  protected static final String LINK_USER_TO_TEAM_END_POINT = "/api/v4/teams/{team_id}/members";
  protected static final String LINK_USER_TO_CHANNEL_END_POINT =
      "/api/v4/channels/{channel_id}/members";
  protected static final String DELETE_TEAM_END_POINT = "/api/v4/teams/{team_id}?permanent=true";
  protected static final String DELETE_CHANNEL_END_POINT =
      "/api/v4/channels/{channel_id}?permanent=true";
  protected static final String UNLINK_USER_TO_CHANNEL_END_POINT =
      "/api/v4/channels/{channel_id}/members/{user_id}";
  protected static final String GET_CHANNEL_MEMBERS_END_POINT =
      "/api/v4/channels/{channel_id}/members";
  protected static final String UPDATE_TEAM_END_POINT = "/api/v4/teams/{team_id}/patch";
  protected static final String UPDATE_CHANNEL_END_POINT = "/api/v4/channels/{channel_id}/patch";
  protected static final String UPDATE_USER_END_POINT = "/api/v4/users/{user_id}/patch";
  protected static final String DELETE_USER_END_POINT = "/api/v4/users/{user_id}?permanent=true";
  protected static final String UNREAD_MSG_BY_TEAM_END_POINT =
      "/api/v4/users/{user_id}/teams/unread";
  protected static final String LOGIN_END_POINT = "/api/v4/users/login";

  protected String url;
  protected String token;

  public MattermostRest(String url, String token) {
    this.url = url;
    this.token = token;
  }

  protected String getIdFromResponse(CloseableHttpResponse response)
      throws AxelorException, IOException, JSONException {
    if (response.getStatusLine() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "Error on id retrieval : no response body.");
    }
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
      String body = EntityUtils.toString(response.getEntity());
      JSONObject json = new JSONObject(body);
      String id = (String) json.get("id");
      response.close();
      LOG.debug("Found id : " + id);
      return id;
    } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
      LOG.debug("Object not found");
      response.close();
      return null;
    } else {
      response.close();
      LOG.debug("Something went wrong");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format("Error on get: %s", response.getStatusLine().toString()));
    }
  }

  protected List<String> getIdListFromResponse(CloseableHttpResponse response)
      throws AxelorException, IOException, JSONException {
    if (response.getStatusLine() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          "Error on id retrieval : no response body.");
    }
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
      List<String> idList = new ArrayList<String>();
      String body = EntityUtils.toString(response.getEntity());
      JSONArray jsonArray = new JSONArray(body);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
        idList.add((String) jsonObject.get("user_id"));
      }
      response.close();
      return idList;
    } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
      LOG.debug("Object not found");
      response.close();
      return null;
    } else {
      response.close();
      LOG.debug("Something went wrong");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format("Error on get: %s", response.getStatusLine().toString()));
    }
  }

  protected CloseableHttpResponse getCall(String url)
      throws AxelorException, ClientProtocolException, IOException {
    // Configuration HttpGet
    HttpGet httpGet = new HttpGet(url);
    httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    httpGet.setConfig(RequestConfig.copy(RequestConfig.DEFAULT).build());
    LOG.debug("Executing http get request {}", httpGet);
    return httpClient.execute(httpGet);
  }

  protected CloseableHttpResponse putCall(String url, String json)
      throws AxelorException, ClientProtocolException, IOException {
    // Configuration HttpPut
    HttpPut httpPut = new HttpPut(url);
    httpPut.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    httpPut.setConfig(RequestConfig.copy(RequestConfig.DEFAULT).build());
    LOG.debug("Executing http put request {}", httpPut);
    httpPut.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    return httpClient.execute(httpPut);
  }

  protected CloseableHttpResponse postCall(String url, String json)
      throws AxelorException, ClientProtocolException, IOException {
    // Configuration HttpPost
    HttpPost httpPost = new HttpPost(url);
    httpPost.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    httpPost.setConfig(RequestConfig.copy(RequestConfig.DEFAULT).build());
    LOG.debug("Executing http post request {}", httpPost);
    httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    return httpClient.execute(httpPost);
  }

  protected CloseableHttpResponse deleteCall(String url)
      throws AxelorException, ClientProtocolException, IOException {
    // Configuration HttpDelete
    HttpDelete httpDelete = new HttpDelete(url);
    httpDelete.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    httpDelete.setConfig(RequestConfig.copy(RequestConfig.DEFAULT).build());
    LOG.debug("Executing http delete request {}", httpDelete);
    return httpClient.execute(httpDelete);
  }

  protected void checkPostResponse(CloseableHttpResponse response)
      throws AxelorException, IOException {
    if (response.getStatusLine() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error on creation: no response body.");
    }
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_CREATED) {
      LOG.debug("Object created");
      response.close();
    } else {
      response.close();
      LOG.debug("Something went wrong");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format("Error on post: %s", response.getStatusLine().toString()));
    }
  }

  protected boolean checkPutResponse(CloseableHttpResponse response)
      throws AxelorException, IOException {
    if (response.getStatusLine() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error on api: no response body.");
    }
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_OK) {
      LOG.debug("Object updated");
      response.close();
      return true;
    } else {
      response.close();
      LOG.debug("Something went wrong");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format("Error on get: %s", response.getStatusLine().toString()));
    }
  }

  protected boolean checkObjectExists(CloseableHttpResponse response)
      throws AxelorException, IOException {
    if (response.getStatusLine() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error on api: no response body.");
    }
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_OK) {
      LOG.debug("Object found");
      response.close();
      return true;
    } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
      LOG.debug("Object not found");
      response.close();
      return false;
    } else {
      response.close();
      LOG.debug("Something went wrong");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format("Error on get: %s", response.getStatusLine().toString()));
    }
  }

  protected boolean checkDeleteResponse(CloseableHttpResponse response)
      throws AxelorException, IOException {
    if (response.getStatusLine() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Error on api: no response body.");
    }
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NOT_FOUND) {
      LOG.debug("Object deleted");
      response.close();
      return true;
    } else {
      response.close();
      LOG.debug("Something went wrong");
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format("Error on get: %s", response.getStatusLine().toString()));
    }
  }

  protected void createHttpClient() {
    if (httpClient == null) {
      httpClient = HttpClients.createDefault();
    }
  }

  protected String getUrl(String endPoint) throws AxelorException {
    return url + endPoint;
  }

  protected String getObjectId(String url)
      throws AxelorException, ClientProtocolException, IOException, JSONException {
    return getIdFromResponse(getCall(url));
  }
}
