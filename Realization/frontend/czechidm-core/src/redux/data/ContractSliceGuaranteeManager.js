import EntityManager from './EntityManager';
import { ContractSliceGuaranteeService } from '../../services';

/**
 * Identity's contract slice guarantee - manually defined  managers (if no tree structure is defined etc.)
 *
 * @author Vít Švanda
 */
export default class ContractSliceGuaranteeManager extends EntityManager {

  constructor() {
    super();
    this.service = new ContractSliceGuaranteeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ContractSliceGuarantee';
  }

  getCollectionType() {
    return 'contractSliceGuarantees';
  }
}
