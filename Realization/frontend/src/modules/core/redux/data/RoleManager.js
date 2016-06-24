'use strict'

import EntityManager from './EntityManager';
import { RoleService } from '../../services';

export default class RoleManager extends EntityManager {

  constructor () {
    super();
    this.service = new RoleService();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Role';
  }

  getCollectionType() {
    return 'roles';
  }
}
