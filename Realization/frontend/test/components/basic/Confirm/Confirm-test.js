import React from 'react';
import TestUtils from 'react-addons-test-utils';
import { expect } from 'chai';
//
import * as Basic from '../../../../src/components/basic';

describe('Basic Confirm', function() {
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
  });*/

  it('- is not shown by default', function() {
    const confirm = TestUtils.renderIntoDocument(<Basic.Confirm/>);
    expect(confirm.props.show).to.be.false;
    expect(confirm.state.show).to.be.false;
  });

  it('- texts check', function() {
    const confirm = TestUtils.renderIntoDocument(<Basic.Confirm />);
    confirm.show('Message', 'Title');
    expect(confirm.state.show).to.be.true;
    expect(confirm.state.message).to.equal('Message');
    expect(confirm.state.title).to.equal('Title');
    confirm.closeModal();
    expect(confirm.state.show).to.be.false;
  });

  it('- promise execution check - on confirm click', function(done) {
    this.result = null;
    const confirm = TestUtils.renderIntoDocument(<Basic.Confirm />);
    confirm.show('Message', 'Title')
    .then(result => {
      done();
    }, (err) => {
      done(new Error('confirm should be confirmed - rejected instead'));
    });
    confirm.confirm();
  });

  it('- promise execution check - on confirm click', function(done) {
    this.result = null;
    const confirm = TestUtils.renderIntoDocument(<Basic.Confirm />);
    confirm.show('Message', 'Title')
    .then(result => {
      done(new Error('confirm should be rejected - confirmed instead'));
    }, (err) => {
      done();
    });
    confirm.reject();
  });
});
