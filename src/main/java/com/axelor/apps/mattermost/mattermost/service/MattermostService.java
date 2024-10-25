package com.axelor.apps.mattermost.mattermost.service;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;

import wslite.json.JSONException;

public interface MattermostService {

  void syncProject(Project project);

  void updateUser(User user, String name)
      throws AxelorException, ClientProtocolException, IOException, JSONException;

  void createTeam() throws AxelorException;

  void updateChannelForUser(User user, boolean canAccessChat) throws AxelorException;

  void createUser(User user);

  Long getUnreadMessage(String userId) throws AxelorException, IOException, JSONException;

  void deleteUser(String mattermostUserId) throws AxelorException, IOException, JSONException;
}
