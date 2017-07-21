import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import * as Basic from '../../../../src/components/basic';

/**
 * Basic.Icon tests
 *
 * @author Radek Tomiška
 */
describe('Basic Icon', function iconTest() {
  it('- without icon defined is not rendered', function test() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Icon />);
    const icon = shallowRenderer.getRenderOutput();
    expect(icon).to.be.null();
  });

  it('- without type defined - type is set to glyph', function test() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Icon icon="user" />);
    const icon = shallowRenderer.getRenderOutput();
    expect(icon.props.type).to.equal(Basic.Icon.TYPE_GLYPHICON);
  });

  it('- property icon and value alias', function test() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Icon icon="user" />);
    const iconByIcon = shallowRenderer.getRenderOutput();
    shallowRenderer.render(<Basic.Icon value="user" />);
    const iconByValue = shallowRenderer.getRenderOutput();
    expect(iconByIcon).to.eql(iconByValue);
  });
});
