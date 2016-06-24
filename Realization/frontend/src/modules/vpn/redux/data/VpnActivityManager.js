import EntityManager from '../../../../modules/core/redux/data/EntityManager';
import { VpnActivityService } from '../../services';

const service = new VpnActivityService();

/**
 * Manager for vpn activities
 */
export default class VpnActivityManager extends EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'vpn-activity';
  }
}
