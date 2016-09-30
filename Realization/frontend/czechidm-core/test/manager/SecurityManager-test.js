import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import { SecurityManager } from '../../src/redux';

describe('SecurityManager', function securityManagerTest() {
  const TEST_AUTHORITY = 'USER_READ';
  const testUserContext = {
    showLoading: false,
    isExpired: false,
    username: 'testUsername',
    isAuthenticated: true,
    tokenCSRF: null,
    tokenCIDMST: null,
    authorities: [TEST_AUTHORITY], // user authorities
  };

  it('[isAuthenticated] - test user context should be authenticated', function test() {
    expect(SecurityManager.isAuthenticated(testUserContext)).to.be.true();
  });

  it('[hasAuthority] - test user context should have TEST_AUTHORITY', function test() {
    expect(SecurityManager.hasAuthority(TEST_AUTHORITY, testUserContext)).to.be.true();
  });

  it('[hasAuthority] - test user context should not have wrong-authority', function test() {
    expect(SecurityManager.hasAuthority('wrong-authority', testUserContext)).to.be.false();
  });

  it('[hasAnyAuthority] - test user context should have TEST_AUTHORITY', function test() {
    expect(SecurityManager.hasAnyAuthority([TEST_AUTHORITY, 'wrong-authority'], testUserContext)).to.be.true();
  });

  it('[isAdmin] - todo');

  it('[hasAccess] - todo - mock navigation item');

  it('[checkAccess] - todo - mock router nextState');
});
