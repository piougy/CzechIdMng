import { Managers } from 'czechidm-core';
import { ProvisioningArchiveService } from '../services';

const service = new ProvisioningArchiveService();

export default class ProvisioningArchiveManager extends Managers.EntityManager {

  constructor() {
    super();
  }

  getService() {
    return service;
  }

  getEntityType() {
    return 'ProvisioningArchive'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'provisioningArchives';
  }
}
