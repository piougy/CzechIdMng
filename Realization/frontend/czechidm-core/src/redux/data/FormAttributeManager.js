import EntityManager from './EntityManager';
import { FormAttributeService } from '../../services';

export default class FormAttributeManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormAttributeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormAttribute';
  }

  getCollectionType() {
    return 'formAttributes';
  }
}
