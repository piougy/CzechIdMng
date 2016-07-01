'use strict';

import EntityUtils from './EntityUtils';
import UiUtils from './UiUtils';
import ResponseUtils from './ResponseUtils';

const UtilsRoot = {
  Entity: EntityUtils,
  Ui: UiUtils,
  Response: ResponseUtils
};

UtilsRoot.version = '0.0.1';
module.exports = UtilsRoot;
