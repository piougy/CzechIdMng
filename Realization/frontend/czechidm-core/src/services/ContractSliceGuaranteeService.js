import AbstractService from './AbstractService';

/**
 * Identity's contract slice guarantee - manually defined  managers (if no tree structure is defined etc.)
 *
 * @author Vít Švanda
 */
export default class ContractSliceGuaranteeService extends AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/contract-slice-guarantees';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded) {
      return '';
    }
    return `${entity.id}`; // TODO: identityContract.niceLabel + guarantee.niceLabel
  }

  supportsPatch() {
    return false;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'CONTRACTSLICEGUARANTEE';
  }

}
