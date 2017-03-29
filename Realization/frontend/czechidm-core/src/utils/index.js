import EntityUtils from './EntityUtils';
import UiUtils from './UiUtils';
import ResponseUtils from './ResponseUtils';
import PermissionUtils from './PermissionUtils';

const UtilsRoot = {
  Entity: EntityUtils,
  Ui: UiUtils,
  Response: ResponseUtils,
  Permission: PermissionUtils
};

UtilsRoot.version = '0.0.1';
module.exports = UtilsRoot;
