package com.axelor.apps.mattermost.user.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.util.Objects;

public class UserController {

  protected MattermostService mattermostService;

  @Inject
  public UserController(MattermostService mattermostService) {
    this.mattermostService = mattermostService;
  }

  public void createUser(ActionRequest request, ActionResponse response) {
    try {
      User user = request.getContext().asType(User.class);
      user = Beans.get(UserRepository.class).find(user.getId());
      if (ObjectUtils.isEmpty(user.getMattermostUserId())) {
        Beans.get(MattermostService.class).createUser(user);
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateMattermost(ActionRequest request, ActionResponse response) {
    try {
      User user = request.getContext().asType(User.class);
      if (user.getId() == null || ObjectUtils.isEmpty(user.getMattermostUserId())) {
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

  @CallMethod
  public String countUnread() {
    try {
      User user = AuthUtils.getUser();
      if (user == null) {
        return "0";
      }
      String userId = user.getMattermostUserId();
      if (ObjectUtils.notEmpty(userId)) {
        String unread = mattermostService.getUnreadMessage(userId).toString();
        return unread;
      }
      return "0";
    } catch (Exception e) {
      TraceBackService.trace(e, "mattermost");
      return Objects.toString(0L);
    }
  }
}
