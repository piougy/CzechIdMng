import { Managers } from 'czechidm-core';
import { VsRequestService } from '../services';

/**
 * Manager controlls request for virtual systems
 *
 * @author Vít Švanda
 */
export default class VsRequestManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new VsRequestService();
  }

  getModule() {
    return 'vs';
  }

  getService() {
    return this.service;
  }

  /**
   * Controlled entity
   */
  getEntityType() {
    return 'VsRequest';
  }

  /**
   * Collection name in search / find response
   */
  getCollectionType() {
    return 'requests';
  }
}
