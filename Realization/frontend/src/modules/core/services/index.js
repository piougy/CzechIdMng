'use strict';

import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import AuthenticateService from './AuthenticateService';
import ConfigService from './ConfigService';
import IdentityService from './IdentityService';
import WorkflowDefinitionService from './WorkflowDefinitionService';
import OrganizationService from './OrganizationService';
import LocalizationService from './LocalizationService';
import RoleService from './RoleService';
import WorkflowTaskInstanceService from './WorkflowTaskInstanceService';
import IdentityRoleService from './IdentityRoleService';
import IdentityWorkingPositionService from './IdentityWorkingPositionService';
import WorkflowProcessInstanceService from './WorkflowProcessInstanceService';
import WorkflowHistoricProcessInstanceService from './WorkflowHistoricProcessInstanceService';

const ServiceRoot = {
  RestApiService: RestApiService,
  AbstractService: AbstractService,
  AuthenticateService: AuthenticateService,
  ConfigService: ConfigService,
  IdentityService: IdentityService,
  WorkflowDefinitionService: WorkflowDefinitionService,
  OrganizationService: OrganizationService,
  LocalizationService: LocalizationService,
  RoleService: RoleService,
  WorkflowTaskInstanceService: WorkflowTaskInstanceService,
  IdentityRoleService: IdentityRoleService,
  IdentityWorkingPositionService: IdentityWorkingPositionService,
  WorkflowProcessInstanceService: WorkflowProcessInstanceService,
  WorkflowHistoricProcessInstanceService: WorkflowHistoricProcessInstanceService
};

ServiceRoot.version = '0.0.1';
module.exports = ServiceRoot;
