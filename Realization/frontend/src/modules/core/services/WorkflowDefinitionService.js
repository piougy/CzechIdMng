'use strict';

import _ from 'lodash';
import Immutable from 'immutable';
//
import * as Utils from '../utils';
import AbstractService from './AbstractService';
import RestApiService from './RestApiService';
import AuthenticateService from './AuthenticateService';

class WorkflowDefinitionService extends AbstractService {

  getApiPath(){
    return '/workflow/definitions/';
  }

  getNiceLabel(entity) {
    if (entity) {
      return (entity.name);
    }
    return '-';
  }

  /**
   * Find all current Workflow definitions
   */
  getAllDefinitions(){
    return RestApiService
      .get(this.getApiPath())
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }
}

export default WorkflowDefinitionService;
