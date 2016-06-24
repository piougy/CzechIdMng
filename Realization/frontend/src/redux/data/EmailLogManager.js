'use strict'

import EntityManager from '../../modules/core/redux/data/EntityManager';
import { EmailLogService } from '../../services';

const service = new EmailLogService();

export default class EmailLogManager extends EntityManager {

  constructor () {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'EmailLog'; // TODO: constant or enumeration
  }
}
