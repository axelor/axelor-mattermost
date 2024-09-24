package com.axelor.apps.mattermost.mattermost;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.helper.MattermostHelper;
import java.io.IOException;
import org.apache.http.client.ClientProtocolException;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class MattermostRestTeam extends MattermostRest {

  public MattermostRestTeam(String url, String token) {
    super(url, token);
  }

  public String createTeam(String name) {
    createHttpClient();
    try {

      String url = getUrl(CREATE_TEAM_END_POINT);
      JSONObject jsonObject = createTeamJsonObject(name);
      return getIdFromResponse(postCall(url, jsonObject.toString(2)));
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
      return null;
    }
  }

  public void deleteTeam(String name) {
    createHttpClient();
    try {

      if (doesTeamExist(name)) {
        String teamId =
            getObjectId(
                getUrl(
                    GET_TEAM_END_POINT.replace("{team_name}", MattermostHelper.computeName(name))));
        if (teamId == null) {
          return;
        }
        String url = getUrl(DELETE_TEAM_END_POINT.replace("{team_id}", teamId));
        checkDeleteResponse(deleteCall(url));
      }
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
    }
  }

  protected boolean doesTeamExist(String name)
      throws ClientProtocolException, IOException, AxelorException {
    String url =
        getUrl(GET_TEAM_END_POINT.replace("{team_name}", MattermostHelper.computeName(name)));
    return checkObjectExists(getCall(url));
  }

  protected JSONObject createTeamJsonObject(String name) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", MattermostHelper.computeName(name));
    jsonObject.put("display_name", name);
    jsonObject.put("type", "I");
    return jsonObject;
  }
}
