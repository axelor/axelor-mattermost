package com.axelor.apps.mattermost.user.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class UserController {

  public void updateMattermost(ActionRequest request, ActionResponse response) {
    try {
      User user = request.getContext().asType(User.class);
      if (user.getId() == null) {
        return;
      }
      User dbUser = Beans.get(UserRepository.class).find(user.getId());
      boolean canAccessChat = user.getCanAccessChat();
      boolean dbCanAccessChat = dbUser.getCanAccessChat();
      String name = user.getName();
      String dbName = dbUser.getName();
      if (canAccessChat != dbCanAccessChat) {
        Beans.get(MattermostService.class).updateChannelForUser(user, canAccessChat);
      }
      if (!name.equals(dbName)) {
        Beans.get(MattermostService.class).updateUser(user, name);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
