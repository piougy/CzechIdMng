'use strict';

import React from 'react';
import ReactDOM from 'react-dom';
import TestUtils from 'react-addons-test-utils';
import shallowHelpers from 'react-shallow-renderer-helpers';
import { expect } from 'chai';
import faker from 'faker';
import moment from 'moment';
import Joi from 'joi';
//
import * as Basic from '../../../../src/components/basic';
import * as Advanced from '../../../../src/components/advanced';


describe('Basic AbstractForm', function() {

  // Does not work for components with ref (throw "Only a ReactOwner can have refs. ..."). I need ref in AbstractFrom and their subcomponents
  //
  it('[readonly] - setting readOnly on form makes all children readOnly', function() {
    let readOnly = true;
    // https://www.npmjs.com/package/react-shallow-renderer-helpers
    const shallowRenderer = shallowHelpers.createRenderer();
    shallowRenderer.render(() => getForm(readOnly), {});
    var instance = shallowRenderer.getMountedInstance();
    // console.log('form', instance.getComponent('name'));

    // const form = TestUtils.renderIntoDocument(getForm(readOnly));
  });


});

function getForm(readOnlyForm) {
  return (
    <Basic.AbstractForm readOnly={true}>
      <Basic.TextField ref="name" label="Name" required validation={Joi.string().min(3).max(30)}/>
      <Basic.TextField ref="lastName" label="LastName" required/>
    </Basic.AbstractForm>
  );
}
