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

  /**
   * Returns connector form definition to given system
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 *
   * @param  {string} id
   * @return {promise}
   */
  getConnectorFormDefinition(id) {
    return Services.RestApiService
      .get(this.getApiPath() + `/${id}/connector-form-definition`)
      .then(response => {
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

  /**
   * Returns filled connector configuration
	 * or throws exception with code {@code CONNECTOR_CONFIGURATION_FOR_SYSTEM_NOT_FOUND}, when system is wrong configured
	 *
   * @param  {string} id
   * @return {promise}
   */
  getConnectorFormValues(id) {
    return Services.RestApiService
      .get(this.getApiPath() + `/${id}/connector-form-values`)
      .then(response => {
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

  /**
   * Saves connector configuration form values
   *
   * @param  {string} id
   * @param  {arrayOf(entity)} values filled form values
   * @return {promise}
   */
  saveConnectorFormValues(id, values) {
    return Services.RestApiService
      .post(this.getApiPath() + `/${id}/connector-form-values`, values)
      .then(response => {
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
