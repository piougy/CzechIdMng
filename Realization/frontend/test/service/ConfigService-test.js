import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
// import ConfigLoader from '../../src/utils/ConfigLoader';
import modules from '../../dist/modules/moduleAssembler';

describe('ConfigLoader', function configServiceTest() {
  // const ModuleLoaderService = new ModuleLoaderService(modules);
  // const configLoader = new ConfigLoader();

  it.skip('- load module descriptor', function test() {
    expect(configLoader.getModuleDescriptor('core').id).to.equal('core');
    // expect(configService.getModuleDescriptor('crt').id).to.equal('crt');
  });

  it.skip('- merge module descriptor', function test() {
    // expect(configService.getNavigation().get('byId').has('certificates-info')).to.be.true;
    expect(configLoader.getNavigation().get('byId').has('messages')).to.be.true();
  });

  it.skip('- override module descriptor', function test() {
    expect(configLoader.getNavigation().get('byId').get('tasks').disabled).to.be.false();
  });
});
