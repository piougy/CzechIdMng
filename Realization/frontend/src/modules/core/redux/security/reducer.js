'use strict';

import _ from 'lodash';
import Immutable from 'immutable';
//
import { REQUEST_LOGIN, RECEIVE_LOGIN, RECEIVE_LOGIN_EXPIRED, RECEIVE_LOGIN_ERROR, LOGOUT } from './SecurityManager';

// TODO: integrate immutable map with redux-localstorage
const INITIAL_STATE = {
  userContext: { // logged userContext {username, token, isGuest etc .}
    showLoading: false,
    isExpired: false,
    username: null,
    isAuthenticated: false,
    tokenCSRF: null,
    tokenCIDMST: null,
    authorities: [], // user aauthorities
  }
}

export function security(state = INITIAL_STATE, action) {
  switch (action.type) {
    case REQUEST_LOGIN: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          showLoading: true
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
          showLoading: false
        })
      });
    }
    case RECEIVE_LOGIN_EXPIRED: {
      return _.merge({}, state, {
        userContext: _.merge({}, state.userContext, {
          isExpired: true,
          showLoading: false
        })
      });
    }
    case LOGOUT: {
      return INITIAL_STATE;
    }
    default:
      return state;
  }
}
