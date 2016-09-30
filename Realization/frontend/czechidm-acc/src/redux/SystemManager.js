import { Managers } from 'czechidm-core';
import { SystemService } from '../services';

export default class SystemManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new SystemService();
  }

  getModule() {
    return 'acc';
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'System';
  }

  getCollectionType() {
    return 'systems';
  }
}
