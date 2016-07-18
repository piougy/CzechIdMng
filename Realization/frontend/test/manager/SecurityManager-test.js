'use strict';

import { expect } from 'chai';
import faker from 'faker';
//
import { SecurityManager } from '../../src/modules/core/redux';

describe('SecurityManager', function() {

  const securityManager = new SecurityManager();

  const TEST_AUTHORITY = 'USER_READ';
  const testUserContext = {
    showLoading: false,
    isExpired: false,
    username: 'testUsername',
    isAuthenticated: true,
    tokenCSRF: null,
    tokenCIDMST: null,
    authorities: [TEST_AUTHORITY], // user authorities
  }

  it('[isAuthenticated] - test user context should be authenticated', function() {
    expect(SecurityManager.isAuthenticated(testUserContext)).to.be.true;
  });

  it('[hasAuthority] - test user context should have TEST_AUTHORITY', function() {
    expect(SecurityManager.hasAuthority(testUserContext, TEST_AUTHORITY)).to.be.true;
  });

  it('[hasAuthority] - test user context should not have wrong-authority', function() {
    expect(SecurityManager.hasAuthority(testUserContext, 'wrong-authority')).to.be.false;
  });

  it('[hasAnyAuthority] - test user context should have TEST_AUTHORITY', function() {
    expect(SecurityManager.hasAnyAuthority(testUserContext, [TEST_AUTHORITY, 'wrong-authority'])).to.be.true;
  });

  it('[isAdmin] - todo');

  it('[hasAccess] - todo - mock navigation item');

  it('[checkAccess] - todo - mock router nextState');
});
