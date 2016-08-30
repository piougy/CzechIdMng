import AttachmentManager from './AttachmentManager';
import ApprovalTaskManager from './ApprovalTaskManager';
import RoleApprovalTaskManager from './RoleApprovalTaskManager';

const ManagerRoot = {
  AttachmentManager,
  ApprovalTaskManager,
  RoleApprovalTaskManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
