'use strict';

import DataManager from './DataManager';
import IdentityManager from './IdentityManager';
import OrganizationManager from './OrganizationManager';
import RoleManager from './RoleManager';
import WorkflowTaskInstanceManager from './WorkflowTaskInstanceManager';
import IdentityRoleManager from './IdentityRoleManager';
import IdentityWorkingPositionManager from './IdentityWorkingPositionManager';
import WorkflowProcessInstanceManager from './WorkflowProcessInstanceManager';
import WorkflowHistoricProcessInstanceManager from './WorkflowHistoricProcessInstanceManager';
import WorkflowHistoricTaskInstanceManager from './WorkflowHistoricTaskInstanceManager';
import WorkflowProcessDefinitionManager from './WorkflowProcessDefinitionManager';

const ManagerRoot = {
  DataManager: DataManager,
  IdentityManager: IdentityManager,
  OrganizationManager: OrganizationManager,
  RoleManager: RoleManager,
  WorkflowTaskInstanceManager: WorkflowTaskInstanceManager,
  IdentityRoleManager: IdentityRoleManager,
  IdentityWorkingPositionManager: IdentityWorkingPositionManager,
  WorkflowProcessInstanceManager: WorkflowProcessInstanceManager,
  WorkflowHistoricProcessInstanceManager: WorkflowHistoricProcessInstanceManager,
  WorkflowHistoricTaskInstanceManager: WorkflowHistoricTaskInstanceManager,
  WorkflowProcessDefinitionManager: WorkflowProcessDefinitionManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
