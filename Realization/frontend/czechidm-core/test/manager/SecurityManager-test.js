import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);

//
import { security } from '../../src/redux/security/reducer';

import SecurityManager, {
  RECEIVE_LOGIN, RECEIVE_LOGIN_EXPIRED, RECEIVE_LOGIN_ERROR,
  RECEIVE_REMOTE_LOGIN_ERROR, REQUEST_REMOTE_LOGIN } from '../../src/redux/security/SecurityManager';

const TEST_AUTHORITY = 'USER_READ';

describe('security reducer', function reducer() {
  it('[INITIAL_STATE]', function initialState() {
    const uctx = security(undefined, {}).userContext;
    expect(uctx).to.have.property('isAuthenticated', false);
    expect(uctx).to.have.property('tokenCIDMST', null);
    expect(uctx).to.have.property('username', null);
    expect(uctx).to.have.property('authorities').that.eql([]);
  });

  it('[RECEIVE_LOGIN]', function test() {
    const newctx = {
      tokenCIDMST: 'test.jwt.token',
      username: 'test-user-name',
      authorities: [TEST_AUTHORITY]
    };
    const uctx = security(undefined, {
      type: RECEIVE_LOGIN,
      userContext: newctx
    }).userContext;
    expect(uctx).to.have.property('isAuthenticated', true);
    expect(uctx).to.have.property('isExpired', false);
    expect(uctx).to.have.property('tokenCIDMST', newctx.tokenCIDMST);
    expect(uctx).to.have.property('username', newctx.username);
    expect(uctx).to.have.property('authorities').that.eql([TEST_AUTHORITY]);
  });

  it('[RECEIVE_LOGIN_ERROR]', function test() {
    const uctx = security(undefined, { type: RECEIVE_LOGIN_ERROR }).userContext;
    expect(uctx).to.have.property('isAuthenticated', false);
    expect(uctx).to.have.property('tokenCIDMST', null);
  });

  it('[RECEIVE_LOGIN_EXPIRED]', function test() {
    const uctx = security(undefined, { type: RECEIVE_LOGIN_EXPIRED }).userContext;
    expect(uctx).to.have.property('isAuthenticated', false);
    expect(uctx).to.have.property('isExpired', true);
    expect(uctx).to.have.property('isTryRemoteLogin', true);
    expect(uctx).to.have.property('tokenCIDMST', null);
  });

  it('[RECEIVE_REMOTE_LOGIN_ERROR]', function test() {
    const uctx = security(undefined, { type: RECEIVE_REMOTE_LOGIN_ERROR }).userContext;
    expect(uctx).to.have.property('isTryRemoteLogin', false);
  });

  it('[REQUEST_REMOTE_LOGIN]', function test() {
    const uctx = security(undefined, { type: REQUEST_REMOTE_LOGIN }).userContext;
    expect(uctx).to.have.property('isTryRemoteLogin', false);
    expect(uctx).to.have.property('showLoading', true);
  });
});

describe('SecurityManager', function securityManagerTest() {
  //
  const testUserContext = {
    showLoading: false,
    isExpired: false,
    username: 'testUsername',
    isAuthenticated: true,
    isTryRemoteLogin: false,
    tokenCSRF: null,
    tokenCIDMST: null,
    authorities: ['IDENTITY_READ', 'IDENTITY_WRITE', 'ROLE_ADMIN'], // user authorities
  };
  //
  const adminUserContext = {
    showLoading: false,
    isExpired: false,
    username: 'testUsername',
    isAuthenticated: true,
    tokenCSRF: null,
    tokenCIDMST: null,
    authorities: [SecurityManager.ADMIN_AUTHORITY], // user authorities
  };

  it('[isAuthenticated] - test user context should be authenticated', function test() {
    expect(SecurityManager.isAuthenticated(testUserContext)).to.be.true();
  });

  it('[hasAuthority] - test user context should have IDENTITY_READ authority', function test() {
    expect(SecurityManager.hasAuthority('IDENTITY_READ', testUserContext)).to.be.true();
  });

  it('[hasAuthority] - test user context should not have wrong-authority', function test() {
    expect(SecurityManager.hasAuthority('wrong-authority', testUserContext)).to.be.false();
  });

  it('[hasAuthority] - test user context should not have wildcard authority on IDENTITY group', function test() {
    expect(SecurityManager.hasAuthority('IDENTITY_DELETE', testUserContext)).to.be.false();
  });

  it('[hasAuthority] - test user context should have wildcard authority on ROLE group', function test() {
    expect(SecurityManager.hasAuthority('ROLE_READ', testUserContext)).to.be.true();
  });

  it('[hasAnyAuthority] - test user context should have IDENTITY_READ authority', function test() {
    expect(SecurityManager.hasAnyAuthority(['IDENTITY_READ', 'wrong-authority'], testUserContext)).to.be.true();
  });

  it('[hasAnyAuthority] - test user context should have ROLE_READ authority', function test() {
    // wild card
    expect(SecurityManager.hasAnyAuthority(['ROLE_READ', 'wrong-authority'], testUserContext)).to.be.true();
  });

  it('[hasAnyAuthority] - test user context should not have IDENTITY_DELETE authority', function test() {
    expect(SecurityManager.hasAnyAuthority(['IDENTITY_DELETE'], testUserContext)).to.be.false();
  });

  it('[hasAllAuthorities] - test user context should have IDENTITY_READ, IDENTITY_WRITE, ROLE_READ authority', function test() {
    expect(SecurityManager.hasAllAuthorities(['IDENTITY_READ', 'wrong-authority'], testUserContext)).to.be.false();
    expect(SecurityManager.hasAllAuthorities(['IDENTITY_READ', 'IDENTITY_WRITE', 'ROLE_READ'], testUserContext)).to.be.true();
  });

  it('[isAdmin] - admin context should be true', function test() {
    expect(SecurityManager.hasAnyAuthority(['wrong-authority'], adminUserContext)).to.be.true();
    expect(SecurityManager.hasAllAuthorities(['IDENTITY_READ', 'wrong-authority'], adminUserContext)).to.be.true();
    expect(SecurityManager.hasAllAuthorities(['IDENTITY_READ', 'IDENTITY_WRITE', 'ROLE_READ'], adminUserContext)).to.be.true();
    expect(SecurityManager.hasAuthority('IDENTITY_ADMIN', adminUserContext)).to.be.true();
    expect(SecurityManager.isAdmin(adminUserContext)).to.be.true();
  });

  it('[isAdmin] - test context should be false', function test() {
    expect(SecurityManager.hasAnyAuthority(['wrong-authority'], testUserContext)).to.be.false();
    expect(SecurityManager.hasAuthority('IDENTITY_ADMIN', testUserContext)).to.be.false();
    expect(SecurityManager.isAdmin(testUserContext)).to.be.false();
  });

  it('[hasAccess] - HAS_ANY_AUTHORITY - test user context should have IDENTITY_READ authority', function test() {
    expect(SecurityManager.hasAccess({ 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ']}, testUserContext)).to.be.true();
  });

  it('[hasAccess] - DENY_ALL', function test() {
    expect(SecurityManager.hasAccess([
      { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['IDENTITY_READ', 'wrong']},
      { 'type': 'DENY_ALL'}
    ], testUserContext)).to.be.false();
  });

  it('[hasAccess] - PERMIT_ALL', function test() {
    expect(SecurityManager.hasAccess([
      { 'type': 'HAS_ANY_AUTHORITY', 'authorities': ['wrong']},
      { 'type': 'PERMIT_ALL' }
    ], testUserContext)).to.be.true();
  });

  it('[hasAccess] - IS_AUTHENTICATED', function test() {
    expect(SecurityManager.hasAccess({ 'type': 'IS_AUTHENTICATED'}, testUserContext)).to.be.true();
  });

  it('[hasAccess] - NOT_AUTHENTICATED', function test() {
    expect(SecurityManager.hasAccess([
      { 'type': 'NOT_AUTHENTICATED'}
    ], testUserContext)).to.be.false();
  });
});
