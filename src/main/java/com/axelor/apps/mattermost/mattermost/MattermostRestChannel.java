package com.axelor.apps.mattermost.mattermost;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.helper.MattermostHelper;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import org.apache.http.client.ClientProtocolException;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class MattermostRestChannel extends MattermostRest {

  @Inject
  public MattermostRestChannel(String url, String token) {
    super(url, token);
  }

  public String createChannel(String teamId, String channelName) {
    createHttpClient();

    try {
      String channelNameComputed = MattermostHelper.computeName(channelName);

      return createChannel(channelName, channelNameComputed, teamId);
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
      return null;
    }
  }

  public void deleteChannel(String channelId) {
    try {
      createHttpClient();

      if (channelId == null) {
        return;
      }
      String url = getUrl(DELETE_CHANNEL_END_POINT.replace("{channel_id}", channelId));
      checkDeleteResponse(deleteCall(url));
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  protected boolean doesChannelExistForThisTeamAndProject(String channelName, String teamId)
      throws AxelorException, IOException {
    String url =
        getUrl(
            GET_CHANNEL_END_POINT_TEAM
                .replace("{team_id}", teamId)
                .replace("{channel_name}", channelName));
    return checkObjectExists(getCall(url));
  }

  protected String createChannel(String channelDisplayName, String channelName, String teamId)
      throws UnsupportedCharsetException, ClientProtocolException, IOException, AxelorException,
          JSONException {

    JSONObject jsonObject = createChannelJsonObject(channelDisplayName, channelName, teamId);
    String url = getUrl(CREATE_CHANNEL_END_POINT);
    return getIdFromResponse(postCall(url, jsonObject.toString(2)));
  }

  public void updateChannel(String channelName, String channelId)
      throws ClientProtocolException, AxelorException, IOException, JSONException {

    createHttpClient();
    String channelNameComputed = MattermostHelper.computeName(channelName);
    JSONObject jsonObject = updateChannelJsonObject(channelName, channelNameComputed);
    String url = getUrl(UPDATE_CHANNEL_END_POINT.replace("{channel_id}", channelId));
    checkPutResponse(putCall(url, jsonObject.toString(2)));
  }

  public List<String> getMemberList(String channelId)
      throws AxelorException, ClientProtocolException, IOException, JSONException {

    createHttpClient();
    String url = getUrl(GET_CHANNEL_MEMBERS_END_POINT.replace("{channel_id}", channelId));
    return getIdListFromResponse(getCall(url));
  }

  protected JSONObject createChannelJsonObject(
      String channelDisplayName, String channelName, String teamId) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("team_id", teamId);
    System.out.println("channelName: " + channelName);
    jsonObject.put("name", channelName);
    jsonObject.put("display_name", channelDisplayName);
    jsonObject.put("type", "P");
    return jsonObject;
  }

  protected JSONObject updateChannelJsonObject(String channelDisplayName, String channelName)
      throws JSONException {
    JSONObject jsonObject = new JSONObject();
    System.out.println("channelName: " + channelName);
    jsonObject.put("name", channelName);
    jsonObject.put("display_name", channelDisplayName);
    return jsonObject;
  }
}
