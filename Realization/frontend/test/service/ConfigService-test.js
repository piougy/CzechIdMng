import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import ConfigLoader from '../../src/modules/core/utils/ConfigLoader';

describe('ConfigLoader', function configServiceTest() {
  const configLoader = new ConfigLoader();

  it('- load module descriptor', function test() {
    expect(configLoader.getModuleDescriptor('core').id).to.equal('core');
    // expect(configService.getModuleDescriptor('crt').id).to.equal('crt');
  });

  it('- merge module descriptor', function test() {
    // expect(configService.getNavigation().get('byId').has('certificates-info')).to.be.true;
    expect(configLoader.getNavigation().get('byId').has('messages')).to.be.true();
  });

  it('- override module descriptor', function test() {
    expect(configLoader.getNavigation().get('byId').get('tasks').disabled).to.be.false();
  });
});
