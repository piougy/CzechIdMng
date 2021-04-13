import { combineReducers, compose, createStore, applyMiddleware } from 'redux';
import log4js from 'log4js';
import merge from 'object-assign';
import Immutable from 'immutable';
import filter from 'redux-localstorage-filter';
import persistState, { mergePersistedState } from 'redux-localstorage';
import ConfigLoader from 'czechidm-core/src/utils/ConfigLoader';
import { Reducers } from 'czechidm-core';
import thunkMiddleware from 'redux-thunk';
import promiseMiddleware from 'redux-promise';
// this parts are genetater dynamicaly to dist - after build will be packed by browserify to sources
import config from '../dist/config.json';

/**
 * Definition of the redux store.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 */
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
const level = ConfigLoader.getConfig('logger.level', !config.logger || !config.logger.level ? 'DEBUG' : config.logger.level);
logger.setLevel(level);
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
        if (value.messages) { // RT: flash messages are not persisted now => F5 => starts from scratch => prevent to see some obsolete error messages
          value.messages.messages = value.messages.messages.toArray();
          value.messages.messages.forEach(message => {
            // prevent to persist react elements
            // FIXME: restore react fragment from text
            message.children = null;
          });
        }
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
  config: Reducers.config,
  messages: Reducers.messages,
  data: Reducers.data,
  security: Reducers.security,
  logger: (state = logger) => {
    // TODO: can be moved to separate redecuer - now is inline
    return state;
  }
});
//
// persistent local storage
const reducer = compose(
  mergePersistedState((initialState, persistedState) => {
    // constuct immutable maps
    const result = merge({}, initialState, persistedState);
    if (persistedState.messages) {
      let composedMessages = new Immutable.OrderedMap({});
      persistedState.messages.messages.forEach(message => {
        composedMessages = composedMessages.set(message.id, message);
      });
      result.messages.messages = composedMessages;
    }
    //
    return result;
  })
)(reducersApp);
//
const storage = compose(
  filter([
    // 'messages.messages',    // RT: flash messages are not persisted now => F5 => starts from scratch => prevent to see some obsolete error messages
    'security.userContext' // logged user context {username, token, etc}
  ])
)(adapter(window.localStorage));

const createPersistentStore = compose(
  persistState(storage, 'czechidm-storage')
)(createStore);

// Sync dispatched route actions to the history
// const reduxRouterMiddleware = routerMiddleware(hashHistory);
//
// before dispatch handler
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
// redux queue middle ware, inpired by https://github.com/zackargyle/redux-async-queue
// TODO: move to utils
function reduxQueue({ dispatch, getState }) {
  const THREAD_COUNT = 3;
  const queues = {}; // queued actions
  const running = {}; // running queue ids

  function dequeue(key) {
    const action = queues[key].find(a => {
      return !running[key].has(a.id);
    });
    if (!action) {
      return;
    }
    running[key] = running[key].add(action.id);
    //
    // execute action
    action.callback(function next() {
      queues[key].shift();
      running[key] = running[key].delete(action.id);
      if (queues[key].length > 0 && running[key].size < THREAD_COUNT) {
        dequeue(key);
      }
    }, dispatch, getState);
  }
  //
  return next => action => {
    const { queue: key, callback, id, type } = action || {};
    if (type === 'RECEIVE_LOGIN') {
      // its needed to clear the queues after login (queues can contain incomplete requests)
      for (const queueKey in running) {
        if (!running.hasOwnProperty(queueKey)) {
          continue;
        }
        running[queueKey] = new Immutable.Set();
      }
    }
    //
    if (key) {
      if (typeof callback !== 'function') {
        throw new Error('Queued actions must have a <callback> property');
      }
      if (!id) {
        throw new Error('Queued actions must have a <id> property');
      }
      // Verify array at <key>
      queues[key] = queues[key] || [];
      running[key] = running[key] || new Immutable.Set();
      // Add new queued callback
      queues[key].push(action);
      //
      // If it's the only one, sync call it.
      if (queues[key].length === 1 || running[key].size < THREAD_COUNT) {
        dequeue(key);
      }
    } else {
      return next(action);
    }
  };
}
//
// apply middleware
let midlewares = [];
if (logger.isTraceEnabled()) {
  midlewares.push(dispatchTrace);
}
midlewares = [...midlewares, thunkMiddleware, promiseMiddleware, reduxQueue];
const createStoreWithMiddleware = applyMiddleware(...midlewares)(createPersistentStore);
// redux store
const store = createStoreWithMiddleware(reducer);
// const store = createStore(reducer);
// const store = createStoreWithMiddleware(reducer, window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__());
// Required for replaying actions from devtools to work
// reduxRouterMiddleware.listenForReplays(store);

export default store;
