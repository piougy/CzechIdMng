'use strict';

import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import SecurityManager from '../security/SecurityManager';
import { ConfigService, IdentityService } from '../../services';

/**
 * Manager for identity fetching
 */
export default class IdentityManager extends EntityManager {

  constructor() {
    super();
    this.identityService = new IdentityService();
    this.configService = new ConfigService();
  }

  getService() {
    return this.identityService;
  }

  getEntityType() {
    return 'Identity'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'identities';
  }

  /**
   * Return true, if given identity is exterine
   */
  isExterne(identity) {
    return this.getService().isExterne(identity);
  }

  /**
   * Who can edit identity - just for ui, rest is secured as well
   *
   * @return {Immutable.Map<string, boolean>} UI elements, which is editable <key, boolean>
   */
  canEditMap(userContext, identity) {
    let canEditMap = Immutable.Map();
    canEditMap = canEditMap.set('isSaveEnabled', false);
    //
    // super admin or user's garant can edit user profile
    if (SecurityManager.isAdmin(userContext)) {
      canEditMap = canEditMap.set('isSaveEnabled', true);
    }
    return canEditMap;
  }

  /**
   * Sets user activity
   *
   * @param {array[string]} usernames selected usernames
   * @param {string} bulkActionName activate|deactivate
   */
  setUsersActivity(usernames, bulkActionName) {
    return ((dispatch, getState) => {
      dispatch(
        this.startBulkAction(
          {
            name: bulkActionName,
            title: this.i18n(`content.users.action.${bulkActionName}.header`, { count: usernames.length })
          },
          usernames.length
        )
      );
      let successUsernames = [];
      usernames.reduce((sequence, username) => {
        return sequence.then(() => {
          if (bulkActionName === 'activate') {
            return this.getService().activate(username);
          } else {
            return this.getService().deactivate(username);
          }
        }).then(json => {
          dispatch(this.updateBulkAction());
          successUsernames.push(username);
          // new entity to redux store
          dispatch(this.receiveEntity(username, json));
        }).catch(error => {
          dispatch(this.flashMessagesManager.addErrorMessage({ title: this.i18n(`content.users.action.${bulkActionName}.error`, { username: username }) }, error));
          throw error;
        });
      }, Promise.resolve())
      .catch((error) => {
        // nothing - message is propagated before
        // catch is before then - we want execute nex then clausule
      })
      .then(() => {
        dispatch(this.flashMessagesManager.addMessage({
          level: successUsernames.length === usernames.length ? 'success' : 'info',
          message: this.i18n(`content.users.action.${bulkActionName}.success`, { usernames: successUsernames.join(', ') })
        }));
        dispatch(this.stopBulkAction());
      });
    });
  }
}
