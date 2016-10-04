import * as Basic from './src/components/basic';
import * as Advanced from './src/components/advanced';
import * as Services from './src/services';
import * as Managers from './src/redux';
import * as LayoutActions from './src/redux/layout/layoutActions';
import * as LayoutReducers from './src/redux/layout/layoutReducers';
import * as FlashReducers from './src/redux/flash/reducer';
import * as DataReducers from './src/redux/data/reducer';
import * as SecurityReducers from './src/redux/security/reducer';
import * as Utils from './src/utils';
// import Routes from './routes';
import ComponentService from './src/services/ComponentService';
//
import SearchParameters from './src/domain/SearchParameters';

import AbstractEnum from './src/enums/AbstractEnum';

const ModuleRoot = {
  Basic,
  Advanced,
  Services,
  Managers,
  LayoutActions,
  Reducers: {
    layout: LayoutReducers.layout,
    messages: FlashReducers.messages,
    data: DataReducers.data,
    security: SecurityReducers.security
  },
  ComponentService,
  Utils,
  Domain: {
    SearchParameters
  },
  Enums: {
    AbstractEnum
  }
};
ModuleRoot.version = '0.0.1';
module.exports = ModuleRoot;
