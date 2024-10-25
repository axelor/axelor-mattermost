package com.axelor.apps.mattermost.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.io.IOException;
import javax.annotation.PreDestroy;
import javax.persistence.PreRemove;
import wslite.json.JSONException;

public class UserListener {

  @PreRemove
  @PreDestroy
  protected void onPreRemove(User user) throws AxelorException, IOException, JSONException {
    String mattermostUserId = user.getMattermostUserId();
    if (mattermostUserId != null) {
      Beans.get(MattermostService.class).deleteUser(mattermostUserId);
    }
  }
}
