import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import TestFieldsUtil from './TestFieldsUtil';
import * as Basic from '../../../../src/components/basic';

describe('TextField', function textFieldTest() {
  it('- text field max and min propery', function test() {
    const maxProperty = Basic.AbstractComponent.supportsProperty(Basic.TextField, 'max');
    expect(maxProperty).equal(true);

    const minProperty = Basic.AbstractComponent.supportsProperty(Basic.TextField, 'min');
    expect(minProperty).equal(true);

    const textField = TestUtils.renderIntoDocument(<Basic.TextField max={3} />);
    TestFieldsUtil.testComponentWithValues(textField, {
      '': true,
      '1234': false
    });

    const textField2 = TestUtils.renderIntoDocument(<Basic.TextField max={3} min={2} />);
    TestFieldsUtil.testComponentWithValues(textField2, {
      '': false,
      '1': false,
      null: false,
      '123': true,
      '12345': false
    });

    const textField3 = TestUtils.renderIntoDocument(<Basic.TextField max={3} />);
    TestFieldsUtil.testComponentWithValues(textField3, {
      '123456': false,
      '123': true
    });

    const textField4 = TestUtils.renderIntoDocument(<Basic.TextField min={3} />);
    TestFieldsUtil.testComponentWithValues(textField4, {
      '12': false,
      '123': true
    });

    const textField5 = TestUtils.renderIntoDocument(<Basic.TextField min={3} max={5} required />);
    TestFieldsUtil.testComponentWithValues(textField5, {
      '12': false,
      '123': true,
      '123456': false,
      '12345': true
    });
  });
});
