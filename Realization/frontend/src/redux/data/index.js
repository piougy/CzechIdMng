import IdentityAccountManager from './IdentityAccountManager';
import SettingManager from './SettingManager';
import AttachmentManager from './AttachmentManager';
import IdentitySubordinateManager from './IdentitySubordinateManager';
import ApprovalTaskManager from './ApprovalTaskManager';
import RoleApprovalTaskManager from './RoleApprovalTaskManager';
import IdentityDelegateManager from './IdentityDelegateManager';

const ManagerRoot = {
  SettingManager,
  IdentityAccountManager,
  AttachmentManager,
  IdentitySubordinateManager,
  ApprovalTaskManager,
  RoleApprovalTaskManager,
  IdentityDelegateManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
