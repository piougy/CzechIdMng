import { Services } from 'czechidm-core';
import { Domain, Utils} from 'czechidm-core';

export default class SynchronizationConfigService extends Services.AbstractService {

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
    let label = entity.name;
    // prepend system name
    if (entity._embedded
        && entity._embedded.systemMapping
        && entity._embedded.systemMapping._embedded
        && entity._embedded.systemMapping._embedded.objectClass
        && entity._embedded.systemMapping._embedded.objectClass._embedded
        && entity._embedded.systemMapping._embedded.objectClass._embedded.system) {
      label = `${ entity._embedded.systemMapping._embedded.objectClass._embedded.system.name } - ${ label }`;
    }
    return label;
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

  cancelSynchronization(id) {
    return Services.RestApiService.post(this.getApiPath() + `/${id}/cancel`, null).then(response => {
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

  isSynchronizationRunning(id) {
    return Services.RestApiService.post(this.getApiPath() + `/${id}/is-running`, null).then(response => {
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
