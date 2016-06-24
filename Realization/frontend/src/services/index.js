'use strict';

import SettingService from './SettingService';
import IdentityAccountService from './IdentityAccountService';
import AttachmentService from './AttachmentService';
import IdentitySubordinateService from './IdentitySubordinateService';
import AuditLogService from './AuditLogService';
import AuditLogForObjectService from './AuditLogForObjectService';
import EmailLogService from './EmailLogService';
import ApprovalTaskService from './ApprovalTaskService';
import RoleApprovalTaskService from './RoleApprovalTaskService';
import IdentityDelegateService from './IdentityDelegateService';

const ServiceRoot = {
  SettingService: SettingService,
  IdentityAccountService: IdentityAccountService,
  AttachmentService: AttachmentService,
  IdentitySubordinateService: IdentitySubordinateService,
  AuditLogService: AuditLogService,
  AuditLogForObjectService: AuditLogForObjectService,
  EmailLogService: EmailLogService,
  ApprovalTaskService: ApprovalTaskService,
  RoleApprovalTaskService: RoleApprovalTaskService,
  IdentityDelegateService: IdentityDelegateService
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
