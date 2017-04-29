

import _ from 'lodash';
import {
  REQUEST_LOGIN, RECEIVE_LOGIN, RECEIVE_LOGIN_EXPIRED,
  RECEIVE_LOGIN_ERROR, LOGOUT, RECEIVE_REMOTE_LOGIN_ERROR,
  REQUEST_REMOTE_LOGIN } from './SecurityManager';

// TODO: integrate immutable map with redux-localstorage
const INITIAL_STATE = {
  userContext: { // logged userContext {username, token, isGuest etc .}
    showLoading: false,
    isExpired: false,
    username: null,
    isAuthenticated: false,
    isTryRemoteLogin: true,
    tokenCSRF: null,
    tokenCIDMST: null,
    authorities: [] // user authorities
  }
};

export function security(state = INITIAL_STATE, action) {
  switch (action.type) {
    case REQUEST_LOGIN: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: true
        })
      });
    }
    case REQUEST_REMOTE_LOGIN: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: true,
          isTryRemoteLogin: false
        })
      });
    }
    case RECEIVE_LOGIN: {
      return _.merge({}, state, {
        userContext: _.merge(
          {
            isAuthenticated: true,
            showLoading: false,
            isExpired: false
          },
          action.userContext
        )
      });
    }
    case RECEIVE_LOGIN_ERROR: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: false,
          tokenCIDMST: null,
          isAuthenticated: false
        })
      });
    }
    case RECEIVE_REMOTE_LOGIN_ERROR: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: false,
          isTryRemoteLogin: false
        })
      });
    }
    case RECEIVE_LOGIN_EXPIRED: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          isAuthenticated: false,
          isExpired: true,
          showLoading: false,
          isTryRemoteLogin: true,
          tokenCIDMST: null
        })
      });
    }
    case LOGOUT: {
      return _.merge({}, INITIAL_STATE, {
        isTryRemoteLogin: false
      });
    }
    default:
      return state;
  }
}
