import EntityUtils from './EntityUtils';
import UiUtils from './UiUtils';
import ResponseUtils from './ResponseUtils';
import PermissionUtils from './PermissionUtils';
import ConfigLoader from './ConfigLoader';

const UtilsRoot = {
  Entity: EntityUtils,
  Ui: UiUtils,
  Response: ResponseUtils,
  Permission: PermissionUtils,
  Config: ConfigLoader
};

UtilsRoot.version = '0.0.1';
module.exports = UtilsRoot;
