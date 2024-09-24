package com.axelor.apps.mattermost.project.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.exception.MattermostExceptionMessage;
import com.axelor.apps.mattermost.project.fix.ProjectFix;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectFixController {

  public void initializeMattermost(ActionRequest request, ActionResponse response) {
    try {

      User user = AuthUtils.getUser();
      if (!user.getCode().equals("admin")) {
        response.setError(I18n.get(MattermostExceptionMessage.PROCESS_ONLY_FOR_SUPERUSER_ADMIN));
      }
      Beans.get(ProjectFix.class).initializeMattermost();
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
