import { Managers } from 'czechidm-core';
import { ExampleProductService } from '../services';

/**
 * Example product manager
 *
 * @author Radek Tomiška
 */
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

  /**
   * Controlled entity
   */
  getEntityType() {
    return 'ExampleProduct';
  }

  /**
   * Collection name in search / find response
   */
  getCollectionType() {
    return 'exampleProducts';
  }
}
