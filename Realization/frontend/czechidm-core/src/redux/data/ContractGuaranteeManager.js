import EntityManager from './EntityManager';
import { ContractGuaranteeService } from '../../services';

/**
 * Identity's contract guarantee - manually defined  managers (if no tree structure is defined etc.)
 *
 * @author Radek Tomiška
 */
export default class ContractGuaranteeManager extends EntityManager {

  constructor() {
    super();
    this.service = new ContractGuaranteeService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'ContractGuarantee';
  }

  getCollectionType() {
    return 'contractGuarantees';
  }
}
