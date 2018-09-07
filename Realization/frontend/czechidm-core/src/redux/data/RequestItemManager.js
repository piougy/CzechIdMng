import EntityManager from './EntityManager';
import { RequestItemService } from '../../services';
import { SearchParameters } from '../../domain';

export default class RequestItemManager extends EntityManager {

  constructor() {
    super();
    this.service = new RequestItemService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RequestItem';
  }

  getCollectionType() {
    return 'requestItems';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'asc');
  }
}
