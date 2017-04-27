import FormableEntityManager from './FormableEntityManager';
import { IdentityContractService } from '../../services';

/**
 * Identity contracts
 *
 * @author Radek Tomiška
 */
export default class IdentityContractManager extends FormableEntityManager {

  constructor() {
    super();
    this.service = new IdentityContractService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'IdentityContract'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'identityContracts';
  }

  /**
   * Extended nice label
   *
   * @param  {entity} entity
   * @param  {boolean} showIdentity identity will be rendered.
   * @return {string}
   */
  getNiceLabel(entity, showIdentity = true) {
    return this.getService().getNiceLabel(entity, showIdentity);
  }
}
