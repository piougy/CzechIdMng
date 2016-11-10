import { Managers } from 'czechidm-core';
import { SchemaAttributeService } from '../services';

const service = new SchemaAttributeService();

export default class SchemaAttributeManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SchemaAttribute';
  }

  getCollectionType() {
    return 'schemaAttributes';
  }
}
