import EntityManager from './EntityManager';
import { WebsocketService } from '../../services';

/**
 * Websocket logs
 *
 * @author Radek Tomiška
 */
export default class WebsocketManager extends EntityManager {

  constructor() {
    super();
    this.service = new WebsocketService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'WebsocketLog';
  }

  getCollectionType() {
    return 'websocketLogs';
  }
}
