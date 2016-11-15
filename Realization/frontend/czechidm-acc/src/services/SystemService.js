import { Services } from 'czechidm-core';
import { Domain, Utils} from 'czechidm-core';

class SystemService extends Services.AbstractService {

  getApiPath() {
    return '/systems';
  }

  getNiceLabel(system) {
    if (!system) {
      return '';
    }
    return system.name;
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  generateSchema(id) {
    return Services.RestApiService.post(this.getApiPath() + `/${id}/generate-schema`, null).then(response => {
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
}

export default SystemService;
