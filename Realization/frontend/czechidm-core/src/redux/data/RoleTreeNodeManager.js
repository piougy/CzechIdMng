import EntityManager from './EntityManager';
import { RoleTreeNodeService } from '../../services';

/**
 * Automatic role administration
 *
 * @author Radek Tomiška
 */
export default class RoleTreeNodeManager extends EntityManager {

  constructor() {
    super();
    this.service = new RoleTreeNodeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'RoleTreeNode';
  }

  getCollectionType() {
    return 'roleTreeNodes';
  }

  supportsPatch() {
    return false;
  }
}
