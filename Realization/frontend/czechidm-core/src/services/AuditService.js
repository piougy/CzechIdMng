import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class AuditService extends AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/audits';
  }

  /**
   * Get nice labal for audit entity. Audit entity hasn't name. Nicelabel is type of audited entity.
   *
   * @param {[type]} entity [description]
   */
  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return `${entity.type}`;
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
}

/**
 * Search all audited entities
 */
AuditService.ENTITIES_SEARCH = 'entities';

export default AuditService;
