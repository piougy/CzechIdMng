import EntityManager from '../../../../modules/core/redux/data/EntityManager';
import { VpnRecordService } from '../../services';

const service = new VpnRecordService();

/**
 * Manager for vpn records
 */
export default class VpnRecordManager extends EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'VpnRecord';
  }
}
