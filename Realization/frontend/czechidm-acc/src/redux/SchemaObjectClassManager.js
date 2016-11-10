import { Managers } from 'czechidm-core';
import { SchemaObjectClassService } from '../services';

const service = new SchemaObjectClassService();

export default class SchemaObjectClassManager
 extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'SchemaObjectClass';
  }

  getCollectionType() {
    return 'schemaObjectClasses';
  }
}
