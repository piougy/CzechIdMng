import EntityManager from './EntityManager';
import { RuleService } from '../../services';

export default class TreeTypeManager extends EntityManager {

  constructor() {
    super();
    this.service = new RuleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Rule';
  }

  getCollectionType() {
    return 'rules';
  }
}
