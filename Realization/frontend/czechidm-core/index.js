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
// import Routes from './routes';
import ComponentService from './src/services/ComponentService';
//
import SearchParameters from './src/domain/SearchParameters';
import FormInstance from './src/domain/FormInstance';
//
import AbstractEnum from './src/enums/AbstractEnum';
import OperationStateEnum from './src/enums/OperationStateEnum';
import PasswordPolicyTypeEnum from './src/enums/PasswordPolicyTypeEnum';
import ScriptCategoryEnum from './src/enums/ScriptCategoryEnum';
import ApiOperationTypeEnum from './src/enums/ApiOperationTypeEnum';
import IdentityAttributeEnum from './src/enums/IdentityAttributeEnum';
import ContractAttributeEnum from './src/enums/ContractAttributeEnum';
//
import ValidationMessage from './src/components/advanced/ValidationMessage/ValidationMessage';

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
  Domain: {
    SearchParameters,
    FormInstance
  },
  Enums: {
    AbstractEnum,
    OperationStateEnum,
    PasswordPolicyTypeEnum,
    ScriptCategoryEnum,
    ApiOperationTypeEnum,
    IdentityAttributeEnum,
    ContractAttributeEnum
  },
  Content: {
    ValidationMessage // backward compatibility
  }
};
ModuleRoot.version = '0.0.1';
module.exports = ModuleRoot;
