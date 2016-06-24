'use strict';

import React from 'react';
import Fetch from 'isomorphic-fetch';
//
import EntityManager from '../../modules/core/redux/data/EntityManager';
import { AuthenticateService } from '../../modules/core/services';
import { AttachmentService } from '../../services';

const ENABLED_MODULES = 'environment.modules.enabled';
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
