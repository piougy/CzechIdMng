import { Services, Domain, Utils } from 'czechidm-core';

/**
 * Remote server with connectors.
 *
 * @author Radek Tomiška
 * @since 10.8.0
 */
class RemoteServerService extends Services.AbstractService {

  getApiPath() {
    return '/remote-servers';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${ entity.host }:${ entity.port }`;
  }

  supportsPatch() {
    return true;
  }

  supportsBulkAction() {
    return true;
  }

  getGroupPermission() {
    return 'REMOTESERVER';
  }

  supportsAuthorization() {
    return true;
  }

  /**
   * Returns default searchParameters for current entity type.
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super
      .getDefaultSearchParameters()
      .setName(Domain.SearchParameters.NAME_QUICK)
      .clearSort()
      .setSort('host')
      .setSort('port');
  }

  /**
   * Returns available frameworks with connectors on remote server.
   *
   * @param  {string} remoteServerId remote server identifier
   * @return {promise}
   */
  fetchAvailableFrameworks(remoteServerId) {
    return Services.RestApiService
      .get(Services.RestApiService.getUrl(`${ this.getApiPath() }/${ encodeURIComponent(remoteServerId) }/frameworks`))
      .then(response => {
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

export default RemoteServerService;
