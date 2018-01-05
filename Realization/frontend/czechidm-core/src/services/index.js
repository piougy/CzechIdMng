import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import FormableEntityService from './FormableEntityService';
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
import LongRunningTaskService from './LongRunningTaskService';
import SchedulerService from './SchedulerService';
import NotificationTemplateService from './NotificationTemplateService';
import RoleRequestService from './RoleRequestService';
import ConceptRoleRequestService from './ConceptRoleRequestService';
import RoleTreeNodeService from './RoleTreeNodeService';
import FormDefinitionService from './FormDefinitionService';
import FormAttributeService from './FormAttributeService';
import AuthorizationPolicyService from './AuthorizationPolicyService';
import ScriptAuthorityService from './ScriptAuthorityService';
import ContractGuaranteeService from './ContractGuaranteeService';
import NotificationRecipientService from './NotificationRecipientService';
import SmsService from './SmsService';
import RecaptchaService from './RecaptchaService';
import LoggingEventService from './LoggingEventService';
import LoggingEventExceptionService from './LoggingEventExceptionService';
import ConfidentialStorageValueService from './ConfidentialStorageValueService';
import LongRunningTaskItemService from './LongRunningTaskItemService';

const ServiceRoot = {
  RestApiService,
  AbstractService,
  FormableEntityService,
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
  EntityEventProcessorService,
  LongRunningTaskService,
  SchedulerService,
  NotificationTemplateService,
  RoleRequestService,
  ConceptRoleRequestService,
  RoleTreeNodeService,
  FormDefinitionService,
  FormAttributeService,
  AuthorizationPolicyService,
  ScriptAuthorityService,
  ContractGuaranteeService,
  NotificationRecipientService,
  SmsService,
  RecaptchaService,
  LoggingEventService,
  LoggingEventExceptionService,
  ConfidentialStorageValueService,
  LongRunningTaskItemService
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
