package com.axelor.apps.mattermost.app.service;

import com.axelor.studio.db.AppMattermost;

public interface AppMattermostService {

  public AppMattermost getAppMattermost();

  public AppMattermost getAppMattermostNoFlush();
}