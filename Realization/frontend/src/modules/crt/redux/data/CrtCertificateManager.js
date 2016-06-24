import EntityManager from '../../../../modules/core/redux/data/EntityManager';
import { CrtCertificateService } from '../../services';

const service = new CrtCertificateService();

/**
 * Manager for certificate task
 */
export default class CrtCertificateManager extends EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'certificates'; // TODO: constant or enumeration
  }
}
