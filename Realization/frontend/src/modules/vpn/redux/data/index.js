'use strict';

import VpnRecordManager from './VpnRecordManager';
import VpnActivityManager from './VpnActivityManager';
import VpnApprovalTaskManager from './VpnApprovalTaskManager';


const ServiceRoot = {
  VpnRecordManager: VpnRecordManager,
  VpnActivityManager: VpnActivityManager,
  VpnApprovalTaskManager: VpnApprovalTaskManager
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
