'use strict';

import VpnRecordService from './VpnRecordService';
import VpnActivityService from './VpnActivityService';
import VpnApprovalTaskService from './VpnApprovalTaskService';


const ServiceRoot = {
  VpnRecordService: VpnRecordService,
  VpnActivityService: VpnActivityService,
  VpnApprovalTaskService: VpnApprovalTaskService
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
