import { Services } from 'czechidm-core';
import { Domain, Utils} from 'czechidm-core';

export default class SynchronizationConfigService extends Services.AbstractService {

  constructor() {
    super();
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  getApiPath() {
    return '/system-synchronization-configs';
  }

  startSynchronization(id) {
    return Services.RestApiService.post(this.getApiPath() + `/${id}/start`, null).then(response => {
      if (response.status === 403) {
        throw new Error(403);
      }
      if (response.status === 404) {
        throw new Error(404);
      }
      return response.json();
    })
    .then(json => {
      if (Utils.Response.hasError(json)) {
        throw Utils.Response.getFirstError(json);
      }
      return json;
    });
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }
}
