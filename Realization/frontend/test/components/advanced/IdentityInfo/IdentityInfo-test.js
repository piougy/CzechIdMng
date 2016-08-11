import React from 'react';
import TestUtils from 'react-addons-test-utils';
import { expect } from 'chai';
//
import { IdentityInfo } from '../../../../src/components/advanced/IdentityInfo/IdentityInfo';

describe('Advanced IdentityInfo', function() {
  it('[not rendered] - component should not be rendered', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<IdentityInfo rendered={false}/>);
    const component = shallowRenderer.getRenderOutput();
    expect(component).to.be.null;
  });

  it('[showloading] - component should not be the same, when showLoading changes', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<IdentityInfo showLoading={false}/>);
    const renderedComponent = shallowRenderer.getRenderOutput();
    shallowRenderer.render(<IdentityInfo showLoading/>);
    const renderedComponentWithShowLoading = shallowRenderer.getRenderOutput();
    expect(renderedComponent).to.not.eql(renderedComponentWithShowLoading);
  });

  it('without identity defined should not be rendered', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<IdentityInfo />);
    const renderedComponent = shallowRenderer.getRenderOutput();
    expect(renderedComponent.props.children).to.be.null;
  });

  it.skip('internist versus externe identity should not be the same', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<IdentityInfo identity={{ name: 'jn', firstName: 'Jan', lastName: 'Novák'}}/>);
    const internist = shallowRenderer.getRenderOutput();
    shallowRenderer.render(<IdentityInfo identity={{ name: 'jn', firstName: 'Jan', lastName: 'Novák', externe: true }}/>);
    const externe = shallowRenderer.getRenderOutput();
    expect(internist).to.not.eql(externe);
  });
});
