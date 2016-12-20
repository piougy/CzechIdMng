import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
import Joi from 'joi';
//
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
      null: false,
      '22': true,
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
      '': false,
      '12': false,
      '123': true,
      '123456': false,
      '12345': true
    });

    const textField6 = TestUtils.renderIntoDocument(<Basic.TextField />);
    TestFieldsUtil.testComponentWithValues(textField6, {
      null: true,
      '': true,
      '123': true,
      '1': true
    });

    const textField7 = TestUtils.renderIntoDocument(<Basic.TextField max={5} required />);
    TestFieldsUtil.testComponentWithValues(textField7, {
      '': false,
      '123': true
    });
  });
});

describe('TextFieldValidation', function textFieldTest() {
  it('- text field combination of validation with properties', function test() {
    const textField = TestUtils.renderIntoDocument(<Basic.TextField max={16} min={6} validation={Joi.string().email()} />);
    TestFieldsUtil.testComponentWithValues(textField, {
      'test@example.com': true,
      'test@example-example.com': false,
      'example': false,
      'exa.example.com': false,
      'example@example': true, // email validation for email is ok for name@domain
      'e@x.m': false,
      'exa@mp.le': true
    });

    const textField2 = TestUtils.renderIntoDocument(<Basic.TextField max={5} min={2} validation={Joi.string().alphanum()} />);
    TestFieldsUtil.testComponentWithValues(textField2, {
      'test@example.com': false,
      '@@@': false,
      'test': true,
      'te12': true,
      'test1234': false,
      '1': false,
      'azAz1': true
    });

    const textField3 = TestUtils.renderIntoDocument(<Basic.TextField max={5} required validation={Joi.string().alphanum()} />);
    TestFieldsUtil.testComponentWithValues(textField3, {
      '': false,
      '@': false,
      'test': true,
      'te12': true,
      'test1234': false
    });
  });
});
