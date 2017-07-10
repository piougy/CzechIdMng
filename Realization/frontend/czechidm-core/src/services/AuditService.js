import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';
import moment from 'moment';

class AuditService extends AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/audits';
  }

  /**
   * Get nice labal for audit entity. Audit entity hasn't name.
   * Nicelabel is id and revision date of entity.
   *
   * @param {[type]} entity [description]
   */
  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.id + ' (' + moment(entity.revisionDate).format('D. M. Y  H:mm:ss') + ')';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('timestamp', 'DESC');
  }

  getAuditedEntitiesSearchParameters() {
    return super.getDefaultSearchParameters().setName(AuditService.ENTITIES_SEARCH);
  }

  /**
   * Difference between two revision
   */
  getDiffBetweenVersion(firstRevId, secondRevId) {
    return RestApiService.get(this.getApiPath() + `/${firstRevId}/diff/${secondRevId}`)
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

  /**
   * Previous version of revision
   */
  getPreviousVersion(revId) {
    return RestApiService.get(this.getApiPath() + `/${revId}/diff/previous`)
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

/**
 * Search all audited entities
 */
AuditService.ENTITIES_SEARCH = 'entities';

/**
 * Search difference between two version
 */
AuditService.DIFF_SEARCH = 'diff';

export default AuditService;
