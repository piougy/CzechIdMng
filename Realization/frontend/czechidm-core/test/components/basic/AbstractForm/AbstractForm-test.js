import React from 'react';
import shallowHelpers from 'react-shallow-renderer-helpers';
import Joi from 'joi';
import * as Basic from '../../../../src/components/basic';

function getForm() {
  return (
    <Basic.AbstractForm readOnly>
      <Basic.TextField ref="name" label="Name" required validation={Joi.string().min(3).max(30)}/>
      <Basic.TextField ref="lastName" label="LastName" required/>
    </Basic.AbstractForm>
  );
}

describe('Basic AbstractForm', function abstractForm() {
  // Does not work for components with ref (throw "Only a ReactOwner can have refs. ..."). I need ref in AbstractFrom and their subcomponents
  it('[readonly] - setting readOnly on form makes all children readOnly', function test() {
    const readOnly = true;
    // https://www.npmjs.com/package/react-shallow-renderer-helpers
    const shallowRenderer = shallowHelpers.createRenderer();
    shallowRenderer.render(() => getForm(readOnly), {});
    // const instance = shallowRenderer.getMountedInstance();
    // console.log('form', instance.getComponent('name'));

    // const form = TestUtils.renderIntoDocument(getForm(readOnly));
  });
});
