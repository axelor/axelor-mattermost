package com.axelor.apps.mattermost.partner.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PartnerController {

  public void createUser(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      partner = Beans.get(PartnerRepository.class).find(partner.getId());
      if (ObjectUtils.isEmpty(partner.getMattermostUserId())
          && (partner.getIsCustomer() || partner.getIsContact())) {
        Beans.get(MattermostService.class).createUsers(partner);
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateMattermost(ActionRequest request, ActionResponse response) {
    try {
      Partner partner = request.getContext().asType(Partner.class);
      if ((!partner.getIsContact() && !partner.getIsCustomer())
          || partner.getId() == null
          || ObjectUtils.isEmpty(partner.getMattermostUserId())) {
        return;
      }
      Partner dbPartner = Beans.get(PartnerRepository.class).find(partner.getId());
      boolean canAccessChat = partner.getCanAccessChat();
      boolean dbCanAccessChat = dbPartner.getCanAccessChat();
      String name = partner.getName();
      String dbName = dbPartner.getName();
      if (canAccessChat != dbCanAccessChat) {
        Beans.get(MattermostService.class).updateChannelForPartner(partner, canAccessChat);
      }
      if (!name.equals(dbName)) {
        Beans.get(MattermostService.class).updatePartner(partner, name);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
