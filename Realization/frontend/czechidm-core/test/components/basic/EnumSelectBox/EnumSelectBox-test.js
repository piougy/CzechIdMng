import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect, fail } from 'chai';
import dirtyChai from 'dirty-chai';
import _ from 'lodash';
// import Joi from 'joi';
// import Select from 'react-select';
//
chai.use(dirtyChai);
//
// import TestFieldsUtil from './TestFieldsUtil';
import * as Basic from '../../../../src/components/basic';

const options = new Array();
options.push({niceLabel: 'test 001', value: '1'});
options.push({niceLabel: 'test 002', value: '2'});
options.push({niceLabel: 'test 003', value: '3'});

describe('EnumSelectBox', function enumSelectBoxTest() {
  const enumSelectBox = TestUtils.renderIntoDocument(<Basic.EnumSelectBox multiSelect options={options} />);

  it('- simple create test for EnumSelectBox', function test() {
    expect(enumSelectBox).to.not.null();
    expect(enumSelectBox.props.multiSelect).to.be.equal(true);
    expect(enumSelectBox.props.options).to.be.equal(options);
  });

  it('- enum select box fill and pick some values', function test() {
    enumSelectBox.state.value = (_.take(options, 2));
    for (const index in enumSelectBox.getValue()) {
      if (enumSelectBox.getValue().hasOwnProperty(index)) {
        expect(enumSelectBox.getValue()[index]).to.not.null();
        expect(enumSelectBox.getValue()[index]).to.equal(options[index].value);
      } else {
        fail(index, 'Own propery of enumSelectBox');
      }
    }
  });

  it('- enum select box check all options data', function test() {
    console.log(enumSelectBox.getOptions(null, function callback(param, data) {
      for (const index in data.options) {
        if (data.options.hasOwnProperty(index)) {
          console.log(data.options[index]);
          expect(data.options[index].value).to.equal(options[index].value)
        }
      }
    }));
  });

  it('- enum select box normalized value ', function test() {
    console.log(enumSelectBox.getOptions(null, function callback(param, data) {
      for (const index in data.options) {
        if (data.options.hasOwnProperty(index)) {
          console.log(data.options[index]);
          expect(data.options[index].itemFullKey).to.equal(options[index].value)
        }
      }
    }));
  });
});
