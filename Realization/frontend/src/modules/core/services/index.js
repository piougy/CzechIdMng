import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import AuthenticateService from './AuthenticateService';
import ConfigService from './ConfigService';
import IdentityService from './IdentityService';
import WorkflowProcessDefinitionService from './WorkflowProcessDefinitionService';
import OrganizationService from './OrganizationService';
import LocalizationService from './LocalizationService';
import RoleService from './RoleService';
import WorkflowTaskInstanceService from './WorkflowTaskInstanceService';
import IdentityRoleService from './IdentityRoleService';
import IdentityWorkingPositionService from './IdentityWorkingPositionService';
import WorkflowProcessInstanceService from './WorkflowProcessInstanceService';
import WorkflowHistoricProcessInstanceService from './WorkflowHistoricProcessInstanceService';
import WorkflowHistoricTaskInstanceService from './WorkflowHistoricTaskInstanceService';
import NotificationService from './NotificationService';
import ConfigurationService from './ConfigurationService';
import EmailService from './EmailService';

const ServiceRoot = {
  RestApiService,
  AbstractService,
  AuthenticateService,
  ConfigService,
  IdentityService,
  WorkflowProcessDefinitionService,
  OrganizationService,
  LocalizationService,
  RoleService,
  WorkflowTaskInstanceService,
  IdentityRoleService,
  IdentityWorkingPositionService,
  WorkflowProcessInstanceService,
  WorkflowHistoricProcessInstanceService,
  WorkflowHistoricTaskInstanceService,
  NotificationService,
  ConfigurationService,
  EmailService
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
