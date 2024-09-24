package com.axelor.apps.mattermost.mattermost.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.apps.project.db.Project;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MattermostRestController {
  public void syncProject(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(MattermostService.class)
          .syncProject(JPA.find(Project.class, request.getContext().asType(Project.class).getId()));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
