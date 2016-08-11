import { expect } from 'chai';
import { ConfigService } from '../../src/modules/core/services';

describe('ConfigService', function() {
  const configService = new ConfigService();

  it('- load module descriptor', function() {
    expect(configService.getModuleDescriptor('core').id).to.equal('core');
    // expect(configService.getModuleDescriptor('crt').id).to.equal('crt');
  });

  it('- merge module descriptor', function() {
    // expect(configService.getNavigation().get('byId').has('certificates-info')).to.be.true;
    expect(configService.getNavigation().get('byId').has('messages')).to.be.true;
  });

  it('- override module descriptor', function() {
    expect(configService.getNavigation().get('byId').get('tasks').disabled).to.be.false;
  });
});
