import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import ComponentService from '../../src/modules/core/services/ComponentService';

describe('ComponentService', function componentServiceTest() {
  const componentService = new ComponentService();

  it('- load module components', function test() {
    expect(componentService.getComponentDescriptor('core').id).to.equal('core');
    // expect(componentService.getComponentDescriptor('crt').id).to.equal('crt');
  });

  it.skip('- merge module components', function test() {
    expect(componentService.getComponent('roleApprovalTaskDetail')).to.not.be.null();
    expect(componentService.getComponent('crt-dashboard')).to.not.be.null();
  });
});
