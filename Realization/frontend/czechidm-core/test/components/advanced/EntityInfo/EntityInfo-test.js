import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
import Immutable from 'immutable';
//
import EntityInfo from '../../../../src/components/advanced/EntityInfo/EntityInfo';
import { IdentityManager } from '../../../../src/redux';
import ComponentLoader from '../../../../src/utils/ComponentLoader';

/**
 * Entity info test
 *
 * @author Radek Tomiška
 */
describe('Advanced EntityInfo', function entityInfo() {
  const identityManager = new IdentityManager();
  // initialize core components
  let componentDescriptors = new Immutable.Map();
  componentDescriptors = componentDescriptors.set('core', require('../../../../component-descriptor.js'));
  ComponentLoader.initComponents(componentDescriptors);

  it('[empty nice label] - nice label should not be rendered', function test() {
    expect(EntityInfo.getNiceLabel(null)).to.be.null();
  });

  it('[identity nice label] - nice label should be rendered', function test() {
    const identity = {
      username: 'test',
      firstName: 'f',
      lastName: 'L'
    };
    //
    expect(EntityInfo.getNiceLabel('identity', identity)).to.equal(identityManager.getNiceLabel(identity));
  });
});
