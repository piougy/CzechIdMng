import { Services, Domain } from 'czechidm-core';

/**
 * Provisioning break notification recxipient.
 *
 * @author Ondřej Kopr
 */
export default class ProvisioningBreakRecipientService extends Services.AbstractService {

  // dto
  supportsPatch() {
    return false;
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    if (entity._embedded) {
      if (entity._embedded.identity) {
        return `${ entity._embedded.identity.username }`;
      }
      if (entity._embedded.role) {
        return `${ entity._embedded.role.name }`;
      }
    }
    return 'recipient'; // #2038 empty recipient can be saved (old data)
  }

  getApiPath() {
    return '/provisioning-break-recipients';
  }

  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(Domain.SearchParameters.NAME_QUICK).clearSort();
  }
}
