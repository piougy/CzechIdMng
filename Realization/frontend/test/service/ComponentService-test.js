'use strict';

import { expect } from 'chai';
//
import  ComponentService  from '../../src/services/ComponentService';

describe('ComponentService', function() {

  const componentService = new ComponentService();

  it('- load module components', function(){
    expect(componentService.getComponentDescriptor('core').id).to.equal('core');
    //expect(componentService.getComponentDescriptor('crt').id).to.equal('crt');
  });

  it.skip('- merge module components', function(){
    expect(componentService.getComponent('roleApprovalTaskDetail')).to.not.be.null;
    expect(componentService.getComponent('crt-dashboard')).to.not.be.null;
  });

});
