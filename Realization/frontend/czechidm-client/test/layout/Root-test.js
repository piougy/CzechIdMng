import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import { Root } from '../../src/layout/Root';


describe('Root', function rootTestSuite() {
  describe('[without redux connected]', function rootTest() {
    it('- without i18n ready is not rendered', function test() {
      const shallowRenderer = TestUtils.createRenderer();
      shallowRenderer.render(<Root/>);
      const root = shallowRenderer.getRenderOutput();
      expect(root.type).to.equal('div');
      expect(root.props.children.length).to.equal(2);
      expect(root.props.children[1]).to.be.true(); // i18n is not ready
    });

    it('- after i18n ready is body', function test() {
      const shallowRenderer = TestUtils.createRenderer();
      shallowRenderer.render(<Root i18nReady/>);
      const root = shallowRenderer.getRenderOutput();
      expect(root.type).to.equal('div');
      expect(root.props.children.length).to.equal(2);
      expect(root.props.children[1].type).to.equal('div');
    });
  });
});
