import EntityManager from './EntityManager';
import { ContractPositionService } from '../../services';

/**
 * Identity's contract other position
 *
 * @author Radek Tomiška
 * @since 9.1.0
 */
export default class ContractPositionManager extends EntityManager {

  constructor() {
    super();
    this.service = new ContractPositionService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ContractPosition';
  }

  getCollectionType() {
    return 'contractPositions';
  }

  /**
   * Extended nice label
   *
   * @param  {entity} entity
   * @param  {boolean} showIdentity identity contract will be rendered.
   * @return {string}
   */
  getNiceLabel(entity, showIdentity = true) {
    return this.getService().getNiceLabel(entity, showIdentity);
  }
}
