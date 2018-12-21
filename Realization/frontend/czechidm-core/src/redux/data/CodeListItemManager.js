import EntityManager from './EntityManager';
import { CodeListItemService } from '../../services';
import DataManager from './DataManager';

/**
 * Code list itemss
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class CodeListItemManager extends EntityManager {

  constructor() {
    super();
    this.service = new CodeListItemService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'CodeListItem';
  }

  getCollectionType() {
    return 'codeListItems';
  }
}
