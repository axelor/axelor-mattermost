package com.axelor.apps.mattermost.mattermost;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.helper.MattermostHelper;
import com.axelor.common.ObjectUtils;
import java.io.IOException;
import java.util.StringJoiner;
import org.apache.http.client.ClientProtocolException;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class MattermostRestUser extends MattermostRest {

  public MattermostRestUser(String url, String token) {
    super(url, token);
  }

  public String createUser(Long id, String email, String firstName, String name, String password)
      throws AxelorException {
    createHttpClient();

    try {
      String userId = doesUserExist(email);
      if (!ObjectUtils.isEmpty(userId)) {
        return userId;
      }
      return createNewUser(id, firstName, name, password, email);
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
      return null;
    }
  }

  protected String createNewUser(
      Long id, String firstName, String name, String password, String email)
      throws ClientProtocolException, AxelorException, IOException, JSONException {
    String url = getUrl(CREATE_USER_END_POINT);
    JSONObject jsonObject = createUserJsonObject(id, firstName, name, password, email);
    return getIdFromResponse(postCall(url, jsonObject.toString(2)));
  }

  public void updateUser(Long id, String email, String firstName, String name, String userId)
      throws ClientProtocolException, AxelorException, IOException, JSONException {
    createHttpClient();
    String url = getUrl(UPDATE_USER_END_POINT.replace("{user_id}", userId));
    JSONObject jsonObject = updateUserJsonObject(id, email, firstName, name);
    checkPutResponse(putCall(url, jsonObject.toString(2)));
  }

  protected String doesUserExist(String ssoMail)
      throws AxelorException, IOException, JSONException {
    String url = getUrl(GET_USER_END_POINT.replace("{email}", ssoMail));
    return getIdFromResponse(getCall(url));
  }

  protected JSONObject createUserJsonObject(
      Long id, String firstName, String name, String password, String email) throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("email", email);
    StringJoiner userName = new StringJoiner("_");
    userName.add(MattermostHelper.normalize(firstName));
    userName.add(MattermostHelper.normalize(name));
    userName.add(String.valueOf(id));
    StringJoiner nickName = new StringJoiner(" ");
    nickName.add(MattermostHelper.normalize(firstName));
    nickName.add(MattermostHelper.normalize(name));
    jsonObject.put("username", userName.toString());
    jsonObject.put("first_name", firstName);
    jsonObject.put("last_name", name);
    jsonObject.put("nickname", nickName.toString());
    jsonObject.put("password", password);
    return jsonObject;
  }

  protected JSONObject updateUserJsonObject(Long id, String email, String firstName, String name)
      throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("email", email);
    if (id != null && ObjectUtils.notEmpty(firstName) && name != null) {
      StringJoiner userName = new StringJoiner("_");
      userName.add(MattermostHelper.normalize(firstName));
      userName.add(MattermostHelper.normalize(name));
      userName.add(String.valueOf(id));
      StringJoiner nickName = new StringJoiner(" ");
      nickName.add(MattermostHelper.normalize(firstName));
      nickName.add(MattermostHelper.normalize(name));
      jsonObject.put("username", userName.toString());
      jsonObject.put("first_name", firstName);
      jsonObject.put("last_name", name);
      jsonObject.put("nickname", nickName.toString());
    }
    return jsonObject;
  }
}
