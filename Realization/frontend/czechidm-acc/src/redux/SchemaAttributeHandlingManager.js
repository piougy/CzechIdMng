import { Managers } from 'czechidm-core';
import { SchemaAttributeHandlingService } from '../services';

const service = new SchemaAttributeHandlingService();

export default class SchemaAttributeHandlingManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SchemaAttributeHandling';
  }

  getCollectionType() {
    return 'schemaAttributesHandling';
  }
}
