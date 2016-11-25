// global babel polyfill - IE Symbol support, Object.assign etc.
import 'babel-polyfill';
import React from 'react';
import ReactDOM from 'react-dom';
// https://github.com/rackt/react-router/blob/master/upgrade-guides/v2.0.0.md#changes-to-thiscontext
// TODO: serving static resources requires different approach - https://github.com/rackt/react-router/blob/master/docs/guides/basics/Histories.md#createbrowserhistory
import { Router, hashHistory } from 'react-router';
import { Provider } from 'react-redux';
import merge from 'object-assign';
import Immutable from 'immutable';
import { combineReducers, compose, createStore, applyMiddleware } from 'redux';
import thunkMiddleware from 'redux-thunk';
import promiseMiddleware from 'redux-promise';
import Promise from 'es6-promise';
import log4js from 'log4js';
//
import persistState, {mergePersistedState} from 'redux-localstorage';
import filter from 'redux-localstorage-filter';
//
import { syncHistory, routeReducer } from 'react-router-redux';
//
import config from '../dist/config.json';
import { Reducers, Managers } from 'czechidm-core';
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
        // pattern: '%d{ISO8601} [%-5p%] %c %m'
        // pattern: '%d{ISO8601} [%p] %n%m',
        pattern: '[%p] %m'
      }
    }
  ],
  // replaceConsole: true
});
const logger = log4js.getLogger();
logger.setLevel(!config.logger || !config.logger.level ? 'DEBUG' : config.logger.level);
global.LOGGER = logger;

// debug setting
// global DEBUG is true only if is application compiled/runned via watchify task. When is application only build, then is always DEBUG set on FALSE.
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
  layout: Reducers.layout,
  messages: Reducers.messages,
  data: Reducers.data,
  security: Reducers.security,
  routing: routeReducer,
  logger: (state = logger) => {
    // TODO: can be moved to separate redecuer and
    return state;
  }
});
//
// persistent local storage
const reducer = compose(
  mergePersistedState((initialState, persistedState) => {
    // constuct immutable maps
    const result = merge({}, initialState, persistedState);
    let composedMessages = new Immutable.OrderedMap({});
    persistedState.messages.messages.map(message => {
      composedMessages = composedMessages.set(message.id, message);
    });
    result.messages.messages = composedMessages;
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
// before dispatch handfler
function dispatchTrace({ getState }) {
  return (next) => (action) => {
    logger.trace('will dispatch', action);
    // Call the next dispatch method in the middleware chain.
    const returnValue = next(action);
    logger.trace('state after dispatch', getState());
    // This will likely be the action itself, unless
    // a middleware further in chain changed it.
    return returnValue;
  };
}
//
// apply middleware
let midlewares = [];
if (logger.isTraceEnabled()) {
  midlewares.push(dispatchTrace);
}
midlewares = [...midlewares, thunkMiddleware, promiseMiddleware, reduxRouterMiddleware];
const createStoreWithMiddleware = applyMiddleware(...midlewares)(createPersistentStore);
// redux store
const store = createStoreWithMiddleware(reducer);
// Required for replaying actions from devtools to work
reduxRouterMiddleware.listenForReplays(store);
//
// application routes root
import App from './layout/App';
const routes = {
  component: 'div',
  childRoutes: [
    {
      path: '/',
      getComponent: (location, cb) => {
        cb(null, App );
      },
      indexRoute: {
        component: require('./layout/Dashboard'),
        onEnter: Managers.SecurityManager.checkAccess,
        access: [{ type: 'IS_AUTHENTICATED' }]
      },
      childRoutes: [
        require('../dist/modules/routeAssembler')
      ]
    }
  ]
};

// fills default onEnter on all routes
// TODO: implement route overriding with priority
function appendCheckAccess(route, moduleId) {
  if (!route.onEnter) {
    route.onEnter = Managers.SecurityManager.checkAccess;
  }
  if (!route.access) {
    route.access = [{ type: 'IS_AUTHENTICATED' }];
  }
  // fill module to route from parent route
  if (route.module === undefined) {
    route.module = moduleId;
  } else {
    moduleId = route.module;
  }
  if (route.childRoutes) {
    route.childRoutes.forEach(childRoute => {
      appendCheckAccess(childRoute, moduleId);
    });
  }
}
appendCheckAccess(routes, null);


//
// app entry point
ReactDOM.render(
  <Provider store={store}>
    <Router history={hashHistory} routes={routes}/>
  </Provider>
  ,
  document.getElementById('content')
);
