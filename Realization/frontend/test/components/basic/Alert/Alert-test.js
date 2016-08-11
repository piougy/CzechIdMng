import React from 'react';
import TestUtils from 'react-addons-test-utils';
import { expect } from 'chai';
//
import * as Basic from '../../../../src/components/basic';

describe('Basic Alert', function() {
  /**
   * Clean DOM afrer each test
   * @param
   * @return
   */
  /*
  afterEach(function(done) {
    ReactDOM.unmountComponentAtNode(document.body) // Assuming mounted to document.body
    document.body.innerHTML = '';                // Just to be sure :-P
    setTimeout(done);
  });
*/
  it('- without level defined info', function() {
    const alert = TestUtils.renderIntoDocument(<Basic.Alert title="Title"/>);
    expect(alert.props.level).to.equal('info');
  });

  it('- with onClose defined, close button is visible', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Alert title="Title" onClose={()=>{}}/>);
    const alert = shallowRenderer.getRenderOutput();
    const onClose = alert.props.children.find(c => c.ref === 'close');
    expect(onClose).to.not.be.undefined;
  });

  it('- without onClose defined, close button is invisible', function() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Alert title="Title" />);
    const alert = shallowRenderer.getRenderOutput();
    const onClose = alert.props.children.find(c => c.ref === 'close');
    expect(onClose).to.be.undefined;
  });

  it('- onClose fire - dom render', function() {
    this.result = null;
    const alert = TestUtils.renderIntoDocument(<Basic.Alert title="Title" onClose={() => { this.result = 'test'; }}/>);
    const onClose = alert.refs.close;
    // TestUtils.Simulate.click(onClose); // TODO: not works, why?
    expect(onClose).to.not.be.undefined;
    onClose.props.onClick();
    expect(alert.state.closed).to.be.true;
    expect(this.result).to.equal('test');
  });

  it('- onClose fire - shallow', function() {
    this.result = null;
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.Alert title="Title" onClose={() => { this.result = 'test'; }}/>);
    const alert = shallowRenderer.getRenderOutput();
    const onClose = alert.props.children.find(c => c.ref === 'close');
    expect(onClose).to.not.be.undefined;
    onClose.props.onClick();
    expect(this.result).to.equal('test');
  });
});
