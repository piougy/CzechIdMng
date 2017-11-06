import { Services } from 'czechidm-core';
import { Domain } from 'czechidm-core';
import AccountTypeEnum from '../domain/AccountTypeEnum';
import { Utils } from 'czechidm-core';

export default class AccountService extends Services.AbstractService {

  constructor() {
    super();
  }

  // dto
  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${AccountTypeEnum.getNiceLabel(entity.accountType)}:${entity._embedded.system.name}:${entity.uid}`;
  }

  getApiPath() {
    return '/accounts';
  }

  /**
  * Get connector object by given account. Call directly connector.
  */
  getConnectorObject(id) {
    return Services.RestApiService
      .get(this.getApiPath() + `/${encodeURIComponent(id)}/connector-object`)
      .then(response => {
        if (!response) {
          return null;
        }
        if (response.status === 204) {
          return null;
        }
        return response.json();
      })
      .then(jsonResponse => {
        if (Utils.Response.hasError(jsonResponse)) {
          throw Utils.Response.getFirstError(jsonResponse);
        }
        if (Utils.Response.hasInfo(jsonResponse)) {
          throw Utils.Response.getFirstInfo(jsonResponse);
        }
        return jsonResponse;
      });
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('systemEntity.uid');
  }
}
