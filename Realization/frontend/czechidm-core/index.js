/**
 * Module API
 *
 * import { Basic, Advanced, Managers, Utils } from 'czechidm-core';
 *
 * @author Radek Tomiška
 */
import * as Basic from './src/components/basic';
import * as Advanced from './src/components/advanced';
import * as Services from './src/services';
import * as Managers from './src/redux';
import * as ConfigActions from './src/redux/config/actions';
import * as ConfigReducers from './src/redux/config/reducer';
import * as FlashReducers from './src/redux/flash/reducer';
import * as DataReducers from './src/redux/data/reducer';
import * as SecurityReducers from './src/redux/security/reducer';
import * as Utils from './src/utils';
import * as Domain from './src/domain';
// import Routes from './routes';
import ComponentService from './src/services/ComponentService';
//
import AbstractEnum from './src/enums/AbstractEnum';
import BasePermissionEnum from './src/enums/BasePermissionEnum';
import OperationStateEnum from './src/enums/OperationStateEnum';
import PasswordPolicyTypeEnum from './src/enums/PasswordPolicyTypeEnum';
import ScriptCategoryEnum from './src/enums/ScriptCategoryEnum';
import ApiOperationTypeEnum from './src/enums/ApiOperationTypeEnum';
import IdentityAttributeEnum from './src/enums/IdentityAttributeEnum';
import ContractAttributeEnum from './src/enums/ContractAttributeEnum';
import ContractSliceAttributeEnum from './src/enums/ContractSliceAttributeEnum';
import ConceptRoleRequestOperationEnum from './src/enums/ConceptRoleRequestOperationEnum';
import IdentityStateEnum from './src/enums/IdentityStateEnum';
import TokenTypeEnum from './src/enums/TokenTypeEnum';
//
import ValidationMessage from './src/components/advanced/ValidationMessage/ValidationMessage';
//
import IdentityTableComponent from './src/content/identity/IdentityTable';
import RoleRequestTableComponent from './src/content/requestrole/RoleRequestTable';
import IdentityRoleTableComponent from './src/content/identity/IdentityRoleTable';
import OrganizationPosition from './src/content/identity/OrganizationPosition';
import AbstractIdentityProjection from './src/content/identity/projection/AbstractIdentityProjection';

const ModuleRoot = {
  Basic,
  Advanced,
  Services,
  Managers,
  ConfigActions,
  Reducers: {
    config: ConfigReducers.config,
    messages: FlashReducers.messages,
    data: DataReducers.data,
    security: SecurityReducers.security
  },
  ComponentService,
  Utils,
  Domain,
  Enums: {
    AbstractEnum,
    BasePermissionEnum,
    OperationStateEnum,
    PasswordPolicyTypeEnum,
    ScriptCategoryEnum,
    ApiOperationTypeEnum,
    IdentityAttributeEnum,
    IdentityStateEnum,
    ContractAttributeEnum,
    ContractSliceAttributeEnum,
    ConceptRoleRequestOperationEnum,
    TokenTypeEnum
  },
  Content: {
    ValidationMessage, // backward compatibility
    OrganizationPosition,
    AbstractIdentityProjection
  },
  Table: {
    IdentityTable: IdentityTableComponent,
    RoleRequestTable: RoleRequestTableComponent,
    IdentityRoleTable: IdentityRoleTableComponent
  }
};
ModuleRoot.version = '10.8.0';
module.exports = ModuleRoot;
