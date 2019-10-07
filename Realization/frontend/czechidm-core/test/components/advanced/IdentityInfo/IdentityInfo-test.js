import React from 'react';
import TestUtils from 'react-dom/test-utils'; import ShallowRenderer from 'react-test-renderer/shallow';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import { IdentityInfo } from '../../../../src/components/advanced/IdentityInfo/IdentityInfo';

/**
 * Advanced.IdentityInfo tests
 *
 * @author Radek Tomiška
 */
describe('Advanced IdentityInfo', function identityInfo() {
  it('[not rendered] - component should not be rendered', function test() {
    const shallowRenderer = new ShallowRenderer();
    shallowRenderer.render(<IdentityInfo rendered={false}/>);
    const component = shallowRenderer.getRenderOutput();
    expect(component).to.be.null();
  });

  it('[showloading] - component should not be the same, when showLoading changes', function test() {
    const shallowRenderer = new ShallowRenderer();
    shallowRenderer.render(<IdentityInfo showLoading={false}/>);
    const renderedComponent = shallowRenderer.getRenderOutput();
    shallowRenderer.render(<IdentityInfo showLoading/>);
    const renderedComponentWithShowLoading = shallowRenderer.getRenderOutput();
    expect(renderedComponent).to.not.eql(renderedComponentWithShowLoading);
  });

  it('without identity defined should not be rendered', function test() {
    const shallowRenderer = new ShallowRenderer();
    shallowRenderer.render(<IdentityInfo />);
    const renderedComponent = shallowRenderer.getRenderOutput();
    expect(renderedComponent).to.be.null();
  });

  it.skip('internist versus externe identity should not be the same', function test() {
    const shallowRenderer = new ShallowRenderer();
    shallowRenderer.render(<IdentityInfo identity={{ name: 'jn', firstName: 'Jan', lastName: 'Novák'}}/>);
    const internist = shallowRenderer.getRenderOutput();
    shallowRenderer.render(<IdentityInfo identity={{ name: 'jn', firstName: 'Jan', lastName: 'Novák', externe: true }}/>);
    const externe = shallowRenderer.getRenderOutput();
    expect(internist).to.not.eql(externe);
  });
});
