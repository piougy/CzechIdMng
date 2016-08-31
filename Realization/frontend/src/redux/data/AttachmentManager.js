import EntityManager from '../../modules/core/redux/data/EntityManager';
import { AttachmentService } from '../../services';

const attachmentService = new AttachmentService();

/**
 * Manager for setting fetching
 */
export default class AttachmenManager extends EntityManager {

  getService() {
    return attachmentService;
  }

  getEntityType() {
    return 'Attachment'; // TODO: constant or enumeration
  }
}
