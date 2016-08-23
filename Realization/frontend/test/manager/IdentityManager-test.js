import faker from 'faker';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import { IdentityManager } from '../../src/modules/core/redux';

describe('IdentityManager', function identityManagerTest() {
  const identityManager = new IdentityManager();

  it('- entity type should be Identity', function test() {
    expect(identityManager.getEntityType()).to.equal('Identity');
  });

  it.skip('- isExterne on externe identity should return true', function test() {
    const identity = { name: faker.name.findName() };
    expect(identityManager.isExterne(identity)).to.be.false();
    identity.externe = true;
    expect(identityManager.isExterne(identity)).to.be.true();
  });

  // TODO: move userContext creation to SecurityManager
  const userContext = {
    username: faker.name.findName(),
    isAuthenticated: false,
    tokenCSRF: 'token',
    roles: []
  };

  it.skip('- not logged user cannot edit identity', function test() {
    const canEditMap = identityManager.canEditMap(userContext, { name: 'jn' });
    expect(canEditMap.get('isSaveEnabled')).to.be.false();
  });

  it.skip('- logged user cannot edit itentity without admin role or if he is not in identity managers', function test() {
    userContext.isAuthenticated = true;
    const canEditMap = identityManager.canEditMap(userContext, { name: 'jn' });
    expect(canEditMap.get('isSaveEnabled')).to.be.false();
    expect(canEditMap.get('idmManager')).to.be.false();
  });

  it.skip('- logged user with admin role can edit identity', function test() {
    userContext.roles.push('superAdminRole');
    const canEditMap = identityManager.canEditMap(userContext, { name: 'jn' });
    expect(canEditMap.get('isSaveEnabled')).to.be.true();
    expect(canEditMap.get('idmManager')).to.be.true();
  });

  it.skip('- logged user with garant role can edit identity, if he is in identity managers', function test() {
    userContext.roles = ['garant'];
    const canEditMap = identityManager.canEditMap(userContext, { name: 'jn', managers: [{ name: userContext.username }] });
    expect(canEditMap.get('isSaveEnabled')).to.be.true();
    // he is not idmManager - he cannot edit identity idmManager
    expect(canEditMap.get('idmManager')).to.be.false();
  });

  it.skip('- logged user with garant role can edit identity and idmManager, if he is identity idmManager', function test() {
    const canEditMap = identityManager.canEditMap(userContext, { name: 'jn', idmManager: userContext.username });
    expect(canEditMap.get('isSaveEnabled')).to.be.true();
    expect(canEditMap.get('idmManager')).to.be.true();
  });
});
