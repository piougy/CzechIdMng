// Deprecated - will be moved to core
import AttachmentService from './AttachmentService';
import ApprovalTaskService from './ApprovalTaskService';
import RoleApprovalTaskService from './RoleApprovalTaskService';

const ServiceRoot = {
  AttachmentService,
  ApprovalTaskService,
  RoleApprovalTaskService
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
