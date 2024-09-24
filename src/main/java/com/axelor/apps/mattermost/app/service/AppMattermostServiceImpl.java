package com.axelor.apps.mattermost.app.service;

import com.axelor.db.Query;
import com.axelor.studio.db.AppMattermost;

public class AppMattermostServiceImpl implements AppMattermostService {
  @Override
  public AppMattermost getAppMattermost() {
    return Query.of(AppMattermost.class).cacheable().fetchOne();
  }

  @Override
  public AppMattermost getAppMattermostNoFlush() {
    return Query.of(AppMattermost.class).cacheable().autoFlush(false).fetchOne();
  }
}
