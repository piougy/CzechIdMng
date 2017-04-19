import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import { SecurityManager } from '../../src/redux';

describe('SecurityManager', function securityManagerTest() {
  //
  const testUserContext = {
    showLoading: false,
    isExpired: false,
    username: 'testUsername',
    isAuthenticated: true,
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
