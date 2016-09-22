import * as Basic from './src/components/basic';
import * as Advanced from './src/components/advanced';
import * as Services from './src/services';
import * as Managers from './src/redux/data';
import * as LayoutActions from './src/redux/Layout/layoutActions';
import * as LayoutReducers from './src/redux/Layout/layoutReducers';
import * as FlashReducers from './src/redux/flash/reducer';
import * as DataReducers from './src/redux/data/reducer';
import * as SecurityReducers from './src/redux/security/reducer';
import SecurityManager from './src/redux/security/SecurityManager';
import FlashMessagesManager from './src/redux/flash/FlashMessagesManager';
// import Routes from './routes';
import ComponentService from './src/services/ComponentService';

const ModuleRoot = {
  Basic,
  Advanced,
  Services,
  Managers,
  LayoutActions,
  LayoutReducers,
  FlashReducers,
  DataReducers,
  SecurityReducers,
  SecurityManager,
  FlashMessagesManager,
  ComponentService
};
ModuleRoot.version = '0.0.1';
module.exports = ModuleRoot;
