import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import AuthenticateService from './AuthenticateService';
import IdentityService from './IdentityService';
import WorkflowProcessDefinitionService from './WorkflowProcessDefinitionService';
import TreeNodeService from './TreeNodeService';
import TreeTypeService from './TreeTypeService';
import LocalizationService from './LocalizationService';
import RoleService from './RoleService';
import WorkflowTaskInstanceService from './WorkflowTaskInstanceService';
import IdentityRoleService from './IdentityRoleService';
import IdentityContractService from './IdentityContractService';
import WorkflowProcessInstanceService from './WorkflowProcessInstanceService';
import WorkflowHistoricProcessInstanceService from './WorkflowHistoricProcessInstanceService';
import WorkflowHistoricTaskInstanceService from './WorkflowHistoricTaskInstanceService';
import NotificationService from './NotificationService';
import ConfigurationService from './ConfigurationService';
import EmailService from './EmailService';
import BackendModuleService from './BackendModuleService';
import RoleCatalogueService from './RoleCatalogueService';
import AuditService from './AuditService';
import ScriptService from './ScriptService';
import NotificationConfigurationService from './NotificationConfigurationService';
import WebsocketService from './WebsocketService';
import PasswordPolicyService from './PasswordPolicyService';
import EntityEventProcessorService from './EntityEventProcessorService';

const ServiceRoot = {
  RestApiService,
  AbstractService,
  AuthenticateService,
  IdentityService,
  WorkflowProcessDefinitionService,
  TreeNodeService,
  TreeTypeService,
  LocalizationService,
  RoleService,
  WorkflowTaskInstanceService,
  IdentityRoleService,
  IdentityContractService,
  WorkflowProcessInstanceService,
  WorkflowHistoricProcessInstanceService,
  WorkflowHistoricTaskInstanceService,
  NotificationService,
  ConfigurationService,
  EmailService,
  BackendModuleService,
  RoleCatalogueService,
  AuditService,
  ScriptService,
  NotificationConfigurationService,
  WebsocketService,
  PasswordPolicyService,
  EntityEventProcessorService
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
