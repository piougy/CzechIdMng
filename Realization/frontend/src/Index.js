'use strict';

// global babel polyfill - IE Symbol support, Object.assign etc.
import 'babel-polyfill';
import React from 'react';
import ReactDOM from 'react-dom';
// https://github.com/rackt/react-router/blob/master/upgrade-guides/v2.0.0.md#changes-to-thiscontext
// TODO: serving static resources requires different approach - https://github.com/rackt/react-router/blob/master/docs/guides/basics/Histories.md#createbrowserhistory
import { Router, Route, browserHistory, hashHistory } from 'react-router';
import { Provider } from 'react-redux';
import merge from 'object-assign';
import Immutable from 'immutable';
import { combineReducers, compose, createStore, applyMiddleware } from 'redux';
import thunkMiddleware from 'redux-thunk';
import promiseMiddleware from 'redux-promise';
import _ from 'lodash';
import Promise from 'es6-promise';
import log4js from 'log4js';
//
import persistState, {mergePersistedState} from 'redux-localstorage';
import filter from 'redux-localstorage-filter';
//
import { syncHistory, routeReducer } from 'react-router-redux';
//
import config from '../config.json';
import { layout } from './redux/Layout/layoutReducers';
import { messages } from './modules/core/redux/flash/reducer';
import { data } from './modules/core/redux/data/reducer';
import { security } from './modules/core/redux/security/reducer';
import { SecurityManager } from './modules/core/redux';
//
// global promise init
Promise.polyfill();
//
// logger setting e.g. http://stritti.github.io/log4js/docu/users-guide.html
log4js.configure({
  appenders: [
    {
      type: 'console',
      layout: {
        type: 'pattern',
        //pattern: '%d{ISO8601} [%-5p%] %c %m'
        //pattern: '%d{ISO8601} [%p] %n%m',
        pattern: '[%p] %m'
      }
    }
  ],
  // replaceConsole: true
});
const logger = log4js.getLogger();
logger.setLevel(!config.logger || !config.logger.level ? 'DEBUG' : config.logger.level);
// debug setting
if (typeof DEBUG === 'undefined') {
  global.DEBUG = true;
}

/**
 * viz. import adapter from 'redux-localstorage/lib/adapters/localStorage';
 * TODO: move to utils
 */
function adapter(storage) {
  return {
    0: storage,

    put: function put(key, value, callback) {
      try {
        //
        value.messages.messages = value.messages.messages.toArray();
        callback(null, storage.setItem(key, JSON.stringify(value)));
      } catch (e) {
        callback(e);
      }
    },

    get: function get(key, callback) {
      try {
        callback(null, JSON.parse(storage.getItem(key)));
      } catch (e) {
        callback(e);
      }
    },

    del: function del(key, callback) {
      try {
        callback(null, storage.removeItem(key));
      } catch (e) {
        callback(e);
      }
    }
  };
}

const reducersApp = combineReducers({
  layout,
  messages,
  data,
  security,
  routing: routeReducer,
  logger: (state = logger, action) => {
    // TODO: can be moved to separate redecuer and
    return state;
  }
});
//
// persistent local storage
const reducer = compose(
  mergePersistedState((initialState, persistedState) => {
    // constuct immutable maps
    let result = merge({}, initialState, persistedState);
    let messages = Immutable.OrderedMap({});
    persistedState.messages.messages.map(message => {
      messages = messages.set(message.id, message);
    });
    result.messages.messages = messages;
    //
    return result;
  })
)(reducersApp);
//
const storage = compose(
  filter([
    'messages.messages',       // flash messages
    'security.userContext'     // logged user context {username, token, etc}
  ])
)(adapter(window.localStorage));
//
//
const createPersistentStore = compose(
  persistState(storage, 'czechidm-storage')
)(createStore);
//
// Sync dispatched route actions to the history
const reduxRouterMiddleware = syncHistory(hashHistory);
//
// apply middleware
const createStoreWithMiddleware = applyMiddleware(thunkMiddleware, promiseMiddleware, reduxRouterMiddleware)(createPersistentStore);
// redux store
const store = createStoreWithMiddleware(reducer);
// Required for replaying actions from devtools to work
reduxRouterMiddleware.listenForReplays(store);
//
// application routes root
import Root from './layout/Root';
import App from './layout/App';
let routes = {
  component: Root,
  childRoutes: [
    {
      path: '/',
      getComponent: (location, cb) => {
        cb(null, { app: App })
      },
      indexRoute: {
        component: require('./layout/Dashboard'),
        onEnter: SecurityManager.checkAccess,
        access: [{ type: 'IS_AUTHENTICATED' }]
      },
      childRoutes: [
        require('./modules/routes')
      ]
    }
  ]
}

// fills default onEnter on all routes
// TODO: implement route overriding with priority
function appendCheckAccess(route) {
  if (!route.onEnter) {
    route.onEnter = SecurityManager.checkAccess;
  }
  if (!route.access) {
    route.access = [{ type: 'IS_AUTHENTICATED' }];
  }
  if (route.childRoutes) {
    route.childRoutes.forEach(childRoute => {
      appendCheckAccess(childRoute);
    });
  }
}
appendCheckAccess(routes);


//
// app entry point
ReactDOM.render(
  <Provider store={store}>
    <Router history={hashHistory} routes={routes}/>
  </Provider>
  ,
  document.getElementById('content')
);
