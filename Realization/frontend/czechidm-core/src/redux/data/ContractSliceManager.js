import FormableEntityManager from './FormableEntityManager';
import { ContractSliceService } from '../../services';

/**
 * Contract slices
 *
 * @author Vít Švanda
 */
export default class ContractSliceManager extends FormableEntityManager {

  constructor() {
    super();
    this.service = new ContractSliceService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ContractSlice'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'contractSlices';
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
