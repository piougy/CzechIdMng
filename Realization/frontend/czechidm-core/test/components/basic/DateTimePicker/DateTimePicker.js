import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect, fail } from 'chai';
import dirtyChai from 'dirty-chai';
import _ from 'lodash';
import ReactDOM from 'react-dom';
// import Joi from 'joi';
//
chai.use(dirtyChai);
//
// import TestFieldsUtil from './TestFieldsUtil';
import * as Basic from '../../../../src/components/basic';

const mode = 'date';
const dateFormat = 'MM.YY.DD.';

describe('DateTimePicker', function dateTimePickerTest() {
  const cleanDateTime = TestUtils.renderIntoDocument(<Basic.DateTimePicker />);
  const dateTimePicker = TestUtils.renderIntoDocument(<Basic.DateTimePicker mode={mode} dateFormat={dateFormat} />);

  it('- simple create test for DateTimePicker', function test() {
    expect(dateTimePicker).to.not.null();
    expect(dateTimePicker.props.mode).to.be.equal(mode);
    expect(dateTimePicker.getFormat()).to.be.equal(dateFormat);
  });

  /**
   * TODO: Possible error? When create default DateTimePicker,
   * without define props. It expect that mode will 'datetime' and format 'DD.MM.YYYY';
   */
  it.skip('- DateTimePicker with default value', function test() {
    expect(cleanDateTime).to.not.null();
    expect(cleanDateTime.getFormat()).to.be.equal('DD.MM.YYYY');
  });

  it('- DateTimePicker set value', function test() {
    dateTimePicker.setValue('11.11. 2000');
    expect(dateTimePicker.isValid()).to.be.equal(true);
    dateTimePicker.setValue('13.13. 2000');
    expect(dateTimePicker.isValid()).to.be.equal(false);
  });

  it('- DateTimePicker create two different dateTimePicker', function test() {
    const shallowRenderer = TestUtils.createRenderer();
    shallowRenderer.render(<Basic.DateTimePicker readOnly/>);
    const dateTime1 = shallowRenderer.getRenderOutput();

    shallowRenderer.render(<Basic.DateTimePicker readOnly={false}/>);
    const dateTime2 = shallowRenderer.getRenderOutput();
    expect(dateTime1).not.be.equal(dateTime2);
  });

  // If you rerender the element with different props in the same container node, it will be updated instead of remounted
  it('- DateTimePicker dynamical change props', function test() {
    const node = document.createElement('div');
    const component = ReactDOM.render(<Basic.DateTimePicker readOnly={false} />, node);
    expect(component.state.readOnly).to.be.equal(false);

    ReactDOM.render(<Basic.DateTimePicker readOnly />, node);
    expect(component.state.readOnly).to.be.equal(true);
  });
});
