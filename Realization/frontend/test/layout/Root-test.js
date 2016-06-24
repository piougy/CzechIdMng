'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import TestUtils from 'react-addons-test-utils';
import { expect } from 'chai';
import faker from 'faker';
import moment from 'moment';
import Immutable from 'immutable';
//
import { Root } from '../../src/layout/Root';


describe('Root', function() {

  describe('[without redux connected]', function() {

    it('- without i18n ready is not rendered', function() {
      const shallowRenderer = TestUtils.createRenderer();
      shallowRenderer.render(<Root/>);
      const root = shallowRenderer.getRenderOutput();
      expect(root.type).to.equal('div');
      expect(root.props.children.length).to.equal(2);
      expect(root.props.children[1]).to.be.true; // i18n is not ready
    });

    it('- after i18n ready is body', function() {
      const shallowRenderer = TestUtils.createRenderer();
      shallowRenderer.render(<Root i18nReady={true}/>);
      const root = shallowRenderer.getRenderOutput();
      expect(root.type).to.equal('div');
      expect(root.props.children.length).to.equal(2);
      expect(root.props.children[1].type).to.equal('div');
    });
  });

});
