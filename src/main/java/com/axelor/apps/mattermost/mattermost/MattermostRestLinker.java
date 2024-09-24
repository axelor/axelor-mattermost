package com.axelor.apps.mattermost.mattermost;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class MattermostRestLinker extends MattermostRest {

  public MattermostRestLinker(String url, String token) {
    super(url, token);
  }

  public void linkUsersToTeamAndChannel(String userId, String teamId, String channelId) {
    try {
      linkUser(userId, teamId, channelId);
    } catch (AxelorException e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  public void linkUser(String userId, String teamId, String channelId) throws AxelorException {
    createHttpClient();
    if (ObjectUtils.isEmpty(teamId)
        || ObjectUtils.isEmpty(userId)
        || ObjectUtils.isEmpty(channelId)) {
      return;
    }
    try {
      if (!ObjectUtils.isEmpty(userId) && !ObjectUtils.isEmpty(teamId)) {
        linkUserToTeam(userId, teamId);
      }
      linkUserToChannel(userId, channelId);
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  public void unlinkUser(String userId, String channelId) throws AxelorException {
    createHttpClient();
    if (ObjectUtils.isEmpty(userId) || ObjectUtils.isEmpty(channelId)) {
      return;
    }
    try {

      String url =
          getUrl(
              UNLINK_USER_TO_CHANNEL_END_POINT
                  .replace("{channel_id}", channelId)
                  .replace("{user_id}", userId));
      checkDeleteResponse(deleteCall(url));
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  protected void linkUserToTeam(String userId, String teamId)
      throws AxelorException, ClientProtocolException, IOException, JSONException {
    JSONObject jsonObject = createLinkUserToTeamJson(userId, teamId);
    String url = getUrl(LINK_USER_TO_TEAM_END_POINT.replace("{team_id}", teamId));
    checkPostResponse(postCall(url, jsonObject.toString(2)));
  }

  protected void linkUserToChannel(String userId, String channelId)
      throws JSONException, ClientProtocolException, AxelorException, IOException {
    JSONObject jsonObject = createLinkUserToChannelJson(userId, channelId);
    String url = getUrl(LINK_USER_TO_CHANNEL_END_POINT.replace("{channel_id}", channelId));
    checkPostResponse(postCall(url, jsonObject.toString(2)));
  }

  protected JSONObject createLinkUserToTeamJson(String userId, String teamId) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("team_id", teamId);
    jsonObject.put("user_id", userId);
    return jsonObject;
  }

  protected JSONObject createLinkUserToChannelJson(String userId, String channelId)
      throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("user_id", userId);
    return jsonObject;
  }
}
