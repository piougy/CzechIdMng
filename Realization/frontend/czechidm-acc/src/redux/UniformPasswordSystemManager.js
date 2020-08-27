import { Managers } from 'czechidm-core';
import { UniformPasswordSystemService } from '../services';

const service = new UniformPasswordSystemService();

/**
 * Manager for uniform password and connection to system
 *
 * @author Ondrej Kopr
 */
export default class UniformPasswordSystemManager extends Managers.EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'UniformPasswordSystem';
  }

  getCollectionType() {
    return 'uniformPasswordSystems';
  }
}
