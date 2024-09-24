/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.mattermost.exception;

public final class MattermostExceptionMessage {

  private MattermostExceptionMessage() {}

  public static final String MATTERMOST_MISSING_MAIL_ON_USER = /*$$(*/
      "User %s has no email." /*)*/;
  public static final String MATTERMOST_MISSING_MAIL_ON_PARTNER = /*$$(*/
      "Partner %s has no email." /*)*/;
  public static final String MATTERMOST_MISSING_NAME_ON_PROJECT = /*$$(*/
      "Project %s has no name." /*)*/;
  public static final String MATTERMOST_API_MISSING_IDENTIFICATION_TOKEN = /*$$(*/
      "Identification token is missing on app mattermost configuration." /*)*/;
  public static final String MATTERMOST_API_MISSING_URL = /*$$(*/
      "Url is missing on app mattermost configuration." /*)*/;
  public static final String PROCESS_ONLY_FOR_SUPERUSER_ADMIN = /*$$(*/
      "This process is only for the superuser admin." /*)*/;
  public static final String MATTERMOST_API_MISSING_TENANT_NAME = /*$$(*/
      "Please select a tenant name in the mattermost configuration." /*)*/;
  public static final String MATTERMOST_API_MISSING_TEAM_ID = /*$$(*/
      "Missing team id for mattermost, please contact system administrator." /*)*/;
}
