'use strict';

import { expect } from 'chai';
import faker from 'faker';
import Immutable from 'immutable';
//
import { IdentityManager } from '../../src/modules/core/redux';

describe('IdentityManager', function() {

  const identityManager = new IdentityManager();

  it('- entity type should be Identity', function() {
    expect(identityManager.getEntityType()).to.equal('Identity');
  });

  it.skip('- isExterne on externe identity should return true', function() {
    let identity = { name: faker.name.findName() }
    expect(identityManager.isExterne(identity)).to.be.false;
    identity.externe = true;
    expect(identityManager.isExterne(identity)).to.be.true;
  });

  // TODO: move userContext creation to SecurityManager
  let userContext = {
    username: faker.name.findName(),
    isAuthenticated: false,
    tokenCSRF: 'token',
    roles: []
  };

  it.skip('- not logged user cannot edit identity', function() {
    let canEditMap = identityManager.canEditMap(userContext, { name: 'jn' });
    expect(canEditMap.get('isSaveEnabled')).to.be.false;
  });

  it.skip('- logged user cannot edit itentity without admin role or if he is not in identity managers', function() {
    userContext.isAuthenticated = true;
    let canEditMap = identityManager.canEditMap(userContext, { name: 'jn' });
    expect(canEditMap.get('isSaveEnabled')).to.be.false;
    expect(canEditMap.get('idmManager')).to.be.false;
  });

  it.skip('- logged user with admin role can edit identity', function() {
    userContext.roles.push('superAdminRole');
    let canEditMap = identityManager.canEditMap(userContext, { name: 'jn' });
    expect(canEditMap.get('isSaveEnabled')).to.be.true;
    expect(canEditMap.get('idmManager')).to.be.true;
  });

  it.skip('- logged user with garant role can edit identity, if he is in identity managers', function() {
    userContext.roles = ['garant'];
    let canEditMap = identityManager.canEditMap(userContext, { name: 'jn', managers: [{ name: userContext.username }] });
    expect(canEditMap.get('isSaveEnabled')).to.be.true;
    // he is not idmManager - he cannot edit identity idmManager
    expect(canEditMap.get('idmManager')).to.be.false;
  });

  it.skip('- logged user with garant role can edit identity and idmManager, if he is identity idmManager', function() {
    let canEditMap = identityManager.canEditMap(userContext, { name: 'jn', idmManager: userContext.username  });
    expect(canEditMap.get('isSaveEnabled')).to.be.true;
    expect(canEditMap.get('idmManager')).to.be.true;
  });
});
