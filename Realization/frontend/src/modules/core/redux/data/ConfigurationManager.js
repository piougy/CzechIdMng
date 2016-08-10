import EntityManager from './EntityManager';
import { ConfigurationService } from 'core/services';

export default class ConfigurationManager extends EntityManager {

  constructor() {
    super();
    this.service = new ConfigurationService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Configuration';
  }

  getCollectionType() {
    return 'configurations';
  }
}
