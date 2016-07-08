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

  /**
   * Generate and download diagram of process as PNG image
   */
  downloadDiagram(id, cb) {
    return RestApiService
      .download(this.getApiPath() + `/${id}/diagram`)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.blob();
      })
      .then(blob => {
        cb(blob);
      });
  }
}

export default WorkflowDefinitionService;
