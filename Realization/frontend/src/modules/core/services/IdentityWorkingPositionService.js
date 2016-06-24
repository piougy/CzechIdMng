'use strict';

import moment from 'moment';
//
import AbstractService from './AbstractService';

class IdentityWorkingPositionService extends AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/workingPositions';
  }

  isValid(workingPosition) {
    if (!workingPosition || moment().isBefore(workingPosition.validFrom) || moment().isAfter(workingPosition.validTill)) {
      return false;
    }
    return true;
  }
}

export default IdentityWorkingPositionService;
