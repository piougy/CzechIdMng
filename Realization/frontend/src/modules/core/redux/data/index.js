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
import NotificationManager from './NotificationManager';
import ConfigurationManager from './ConfigurationManager';

const ManagerRoot = {
  DataManager,
  IdentityManager,
  OrganizationManager,
  RoleManager,
  WorkflowTaskInstanceManager,
  IdentityRoleManager,
  IdentityWorkingPositionManager,
  WorkflowProcessInstanceManager,
  WorkflowHistoricProcessInstanceManager,
  WorkflowHistoricTaskInstanceManager,
  WorkflowProcessDefinitionManager,
  NotificationManager,
  ConfigurationManager
};

ManagerRoot.version = '0.0.1';
module.exports = ManagerRoot;
