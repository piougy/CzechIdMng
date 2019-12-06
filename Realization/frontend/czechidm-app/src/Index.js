//
// Aplication entry point
import 'babel-polyfill';
import React from 'react';
import ReactDOM from 'react-dom';
import Helmet from 'react-helmet';
import { HashRouter as Router, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import Promise from 'es6-promise';
//
import { Managers, Basic, ConfigActions } from 'czechidm-core';
//
// this parts are genetater dynamicaly to dist - after build will be packed by browserify to sources
import IdmContext from 'czechidm-core/src/context/idm-context';
import config from '../dist/config.json';
import { moduleDescriptors } from '../dist/modules/moduleAssembler';
import { componentDescriptors } from '../dist/modules/componentAssembler';
// application routes root
import App from './layout/App';
import store from './store';

// global promise init
// TODO: https://github.com/qubyte/fetch-ponyfill
Promise.polyfill();

store.dispatch(ConfigActions.appInit(config, moduleDescriptors, componentDescriptors, (error) => {
  if (!error) {
    // We need to init routes after configuration will be loaded
    const routes = require('./routes');
    // App entry point
    ReactDOM.render(
      <Provider store={store}>
        <IdmContext.Provider value={{store, routes}}>
          <Router>
            <Route path="/" component={App} />
          </Router>
        </IdmContext.Provider>
      </Provider>,
      document.getElementById('content')
    );
  } else {
    const flashManager = new Managers.FlashMessagesManager();
    if (store) {
      const logger = store.getState().logger;
      logger.error(`[APP INIT]: Error during app init:`, error);
    }
    ReactDOM.render(
      <div style={{ margin: 15 }}>
        <Helmet title="503" />
        <Basic.FlashMessage
          icon="exclamation-sign"
          message={flashManager.convertFromError(error)}/>
      </div>,
      document.getElementById('content')
    );
  }
}));
