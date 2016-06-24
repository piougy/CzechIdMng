'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import TestUtils from 'react-addons-test-utils';
import { expect } from 'chai';
import faker from 'faker';
import moment from 'moment';
//
import * as Basic from '../../../../src/components/basic';

describe('Basic Icon', function() {

  it('- without icon defined is not rendered', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Icon />);
    const icon = shallowRenderer.getRenderOutput();
    expect(icon).to.be.null;
  });

  it('- without type defined - type is set to glyph', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Icon icon="user" />);
    const icon = shallowRenderer.getRenderOutput();
    expect(icon.props.type).to.equal(Basic.Icon.TYPE_GLYPHICON);
  });

  it('- property icon and value alias', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Icon icon="user" />);
    const iconByIcon = shallowRenderer.getRenderOutput();
    shallowRenderer.render(<Basic.Icon value="user" />);
    const iconByValue = shallowRenderer.getRenderOutput();
    expect(iconByIcon).to.eql(iconByValue);
  });

});
