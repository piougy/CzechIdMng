import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';

class ConfidentialStorageValueService extends AbstractService {

  getApiPath() {
    return '/confidential-storage-values';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.key;
  }

  // dto
  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for scripts
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('key');
  }
}

export default ConfidentialStorageValueService;
