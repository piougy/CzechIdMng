

import IdentityAccountManager from './IdentityAccountManager';
import SettingManager from './SettingManager';
import AttachmentManager from './AttachmentManager';
import IdentitySubordinateManager from './IdentitySubordinateManager';
import AuditLogManager from './AuditLogManager';
import AuditLogForObjectManager from './AuditLogForObjectManager';
import EmailLogManager from './EmailLogManager';
import ApprovalTaskManager from './ApprovalTaskManager';
import RoleApprovalTaskManager from './RoleApprovalTaskManager';
import IdentityDelegateManager from './IdentityDelegateManager';


const ManagerRoot = {
  SettingManager: SettingManager,
  IdentityAccountManager: IdentityAccountManager,
  AttachmentManager: AttachmentManager,
  IdentitySubordinateManager: IdentitySubordinateManager,
  AuditLogManager: AuditLogManager,
  AuditLogForObjectManager: AuditLogForObjectManager,
  EmailLogManager: EmailLogManager,
  ApprovalTaskManager: ApprovalTaskManager,
  RoleApprovalTaskManager: RoleApprovalTaskManager,
  IdentityDelegateManager: IdentityDelegateManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
