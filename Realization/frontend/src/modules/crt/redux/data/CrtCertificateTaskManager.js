import EntityManager from '../../../../modules/core/redux/data/EntityManager';
import { CrtCertificateTaskService } from '../../services';

const service = new CrtCertificateTaskService();

/**
 * Manager for certificate task
 */
export default class CrtCertificateTaskManager extends EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'certificate-tasks'; // TODO: constant or enumeration
  }
}
