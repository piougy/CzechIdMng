import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
import Joi from 'joi';
//
chai.use(dirtyChai);
//
import * as Basic from '../../../../src/components/basic';
import TestFieldsUtil from './TestFieldsUtil';

describe('textArea', function textAreaTest() {
  it('- text area max and min propery', function test() {
    const maxProperty = Basic.AbstractComponent.supportsProperty(Basic.TextArea, 'max');
    expect(maxProperty).equal(true);

    const minProperty = Basic.AbstractComponent.supportsProperty(Basic.TextArea, 'min');
    expect(minProperty).equal(true);

    const textArea = TestUtils.renderIntoDocument(<Basic.TextArea max={3} />);
    TestFieldsUtil.testComponentWithValues(textArea, {
      '1234': false,
      '': true
    });

    const textArea2 = TestUtils.renderIntoDocument(<Basic.TextArea max={3} min={2} />);
    TestFieldsUtil.testComponentWithValues(textArea2, {
      '': false,
      '1': false,
      null: false,
      '123': true,
      '12345': false
    });

    const textArea3 = TestUtils.renderIntoDocument(<Basic.TextArea max={3} />);
    TestFieldsUtil.testComponentWithValues(textArea3, {
      '123456': false,
      '123': true
    });

    const textArea4 = TestUtils.renderIntoDocument(<Basic.TextArea min={3} />);
    TestFieldsUtil.testComponentWithValues(textArea4, {
      '12': false,
      '123': true
    });

    const textArea5 = TestUtils.renderIntoDocument(<Basic.TextArea min={3} max={5} required />);
    TestFieldsUtil.testComponentWithValues(textArea5, {
      '12': false,
      '123': true,
      '123456': false,
      '12345': true
    });

    const textArea6 = TestUtils.renderIntoDocument(<Basic.TextArea min={3} max={5} required rows={4} />);
    TestFieldsUtil.testComponentWithValues(textArea6, {
      '\n': false,
      '\n\n\n\n': true
    });
  });
});

describe('TextAreaValidation', function textAreaTest() {
  it('- text area combination of validation with properties', function test() {
    const textField = TestUtils.renderIntoDocument(<Basic.TextArea max={16} min={6} validation={Joi.string().email()} />);
    TestFieldsUtil.testComponentWithValues(textField, {
      'test@example.com': true,
      'test@example-example.com': false,
      'example': false,
      'exa.example.com': false,
      'example@example': true, // email validation for email is ok for name@domain
      'e@x.m': false,
      'exa@mp.le': true
    });

    const textField2 = TestUtils.renderIntoDocument(<Basic.TextArea max={5} min={2} validation={Joi.string().alphanum()} />);
    TestFieldsUtil.testComponentWithValues(textField2, {
      'test@example.com': false,
      '@@@': false,
      'test': true,
      'te12': true,
      'test1234': false,
      '1': false,
      'azAz1': true
    });

    const textField3 = TestUtils.renderIntoDocument(<Basic.TextArea max={5} required validation={Joi.string().alphanum()} />);
    TestFieldsUtil.testComponentWithValues(textField3, {
      '': false,
      '@': false,
      'test': true,
      'te12': true,
      'test1234': false
    });
  });
});
