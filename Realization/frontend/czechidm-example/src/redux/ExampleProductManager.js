import { Managers } from 'czechidm-core';
import { ExampleProductService } from '../services';

export default class ExampleProductManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new ExampleProductService();
  }

  getModule() {
    return 'example';
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ExampleProduct';
  }

  getCollectionType() {
    return 'exampleProducts';
  }
}
