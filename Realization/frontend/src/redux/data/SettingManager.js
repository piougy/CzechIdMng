'use strict';

import React from 'react';
//
import EntityManager from '../../modules/core/redux/data/EntityManager';
import { SettingService } from '../../services';

const ENABLED_MODULES = 'environment.modules.enabled';
const settingService = new SettingService();

/**
 * Manager for setting fetching
 */
export default class SettingManager extends EntityManager {

  getService() {
    return settingService;
  }

  getEntityType() {
    return 'Setting'; // TODO: constant or enumeration
  }

  /**
   * Returns setting value
   */
  getValue(state, key) {
    if (state.data.entity.Setting && state.data.entity.Setting.has(key)) {
      return state.data.entity.Setting.get(key).value;
    }
    return null;
  }

  isEnableModule(state, key){
    let modules =  this.getValue(state, ENABLED_MODULES);
    if (modules){
      let modulesArray = modules.split(',');
      for (var variable in modulesArray) {
        if (modulesArray.hasOwnProperty(variable) && modulesArray[variable] === key) {
          return true;
        }
      }
    }

    return false;
  }
}

SettingManager.CORE_USER_MODULE = 'CORE_USER';
SettingManager.CERTIFICATES_MODULE = 'CERTIFICATES';
