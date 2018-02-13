import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect } from 'chai';
import ReactDOM from 'react-dom';
import dirtyChai from 'dirty-chai';
import _ from 'lodash';
import ReactPropTypesSecret from 'react/lib/ReactPropTypesSecret';
chai.use(dirtyChai);
//
import * as Basic from '../../../../src/components/basic';
import * as Advanced from '../../../../src/components/advanced';
import RoleTypeEnum from '../../../../src/enums/RoleTypeEnum';
import { RoleManager } from '../../../../src/redux';

/**
 * Component common properties test
 *
 * @author Radek Tomiška
 */

const componentLibraries = [Basic, Advanced];
const componentLibrariesBasic = [Basic];
const manager = new RoleManager();

/**
 * Fill some commons properties to ensure more component types will be rendered
 *
 * @param  {ReactComponent} ComponentType
 * @return {object} filled props
 */
function getCommonProps(ComponentType) {
  //
  // fill some commons properties to ensure more component types will be rendered
  const commonPropNames = ['title', 'icon', 'label', 'text', 'onDrop', 'onClick', 'onSubmit', 'entityIdentifier', 'value', 'to'];
  const commonProps = {};
  for (const typeSpecName in ComponentType.propTypes) {
    // advanced props
    if (typeSpecName === 'entityType') {
      commonProps[typeSpecName] = 'identity';
      continue;
    }
    if (typeSpecName === 'enum') {
      commonProps[typeSpecName] = RoleTypeEnum;
      continue;
    }
    if (typeSpecName === 'manager') {
      commonProps[typeSpecName] = manager;
      continue;
    }
    if (ComponentType.name === 'DateTimePicker' && typeSpecName === 'value') {
      commonProps[typeSpecName] = '2011-01-01';
      continue;
    }
    if (!_.includes(commonPropNames, typeSpecName)) {
      continue;
    }
    //
    if (ComponentType.propTypes.hasOwnProperty(typeSpecName)) {
      if (ComponentType.propTypes[typeSpecName]({ [typeSpecName]: 'text' }, typeSpecName, ComponentType.name, '', null, ReactPropTypesSecret) === null) {
        // string
        commonProps[typeSpecName] = 'text';
      } else if (ComponentType.propTypes[typeSpecName]({ [typeSpecName]: {} }, typeSpecName, ComponentType.name, '', null, ReactPropTypesSecret) === null) {
        // object
        commonProps[typeSpecName] = {};
      } else if (ComponentType.propTypes[typeSpecName]({ [typeSpecName]: () => {} }, typeSpecName, ComponentType.name, '', null, ReactPropTypesSecret) === null) {
        // fuunction
        commonProps[typeSpecName] = () => {};
      }
    }
  }
  return commonProps;
}

describe('Basic AbstractComponent', function abstractComponent() {
  it('- supportsRendered', function test() {
    expect(Basic.AbstractComponent.supportsRendered(Basic.AbstractComponent)).to.be.true();
    expect(Basic.AbstractComponent.supportsRendered(Basic.Icon)).to.be.true();
  });

  it('- supportsShowLoading', function test() {
    expect(Basic.AbstractComponent.supportsShowLoading(Basic.AbstractComponent)).to.be.true();
    expect(Basic.AbstractComponent.supportsShowLoading(Basic.Icon)).to.be.true();
  });


  describe('- when rendered is false, then component should not be rendered', function test() {
    // all components which supports rendered props
    let counter = 0;
    for (const componentLibrary of componentLibraries) {
      for (const component in componentLibrary) {
        if (component.startsWith('Abstract')) {
          continue;
        }
        if (Basic.AbstractComponent.supportsRendered(componentLibrary[component])) {
          /* eslint  no-loop-func: 0 */
          it('- ' + component, function testComponent() {
            const ComponentType = componentLibrary[component];
            const shallowRenderer = TestUtils.createRenderer();
            const componentProps = getCommonProps(ComponentType);
            counter++;
            shallowRenderer.render(<ComponentType key={ `cmp-${counter}` } rendered={false} {...componentProps}/>);
            const renderedComponent = shallowRenderer.getRenderOutput();
            expect(renderedComponent).to.be.null();
          });
        }
      }
    }
  });

  describe('- component should not be the same, when showLoading changes', function test() {
    // all components which supports rendered props
    for (const componentLibrary of componentLibraries) {
      for (const component in componentLibrary) {
        if (component.startsWith('Abstract')) {
          continue;
        }
        if (Basic.AbstractComponent.supportsShowLoading(componentLibrary[component])) {
          /* eslint  no-loop-func: 0 */
          it('- ' + component, function testComponent() {
            const ComponentType = componentLibrary[component];
            const shallowRenderer = TestUtils.createRenderer();
            const componentProps = getCommonProps(ComponentType);
            shallowRenderer.render(<ComponentType showLoading={false} {...componentProps}/>);
            const renderedComponent = shallowRenderer.getRenderOutput();
            shallowRenderer.render(<ComponentType showLoading {...componentProps}/>);
            const renderedComponentWithShowLoading = shallowRenderer.getRenderOutput();
            /*
            if(component === 'Label') {
              console.log('Comp1', renderedComponent);
              console.log('Comp2', renderedComponentWithShowLoading);
            }*/
            if (renderedComponent && renderedComponentWithShowLoading) { // some components need more props to render itself
              expect(renderedComponent).to.not.eql(renderedComponentWithShowLoading);
              if (renderedComponent.props.children && renderedComponentWithShowLoading.props.children) {
                expect(renderedComponent.props.children).to.not.eql(renderedComponentWithShowLoading.props.children);
              }
            }
          });
        }
      }
    }
  });

  describe('- component change dynamicaly readOnly', function test() {
    for (const componentLibrary of componentLibrariesBasic) {
      for (const component in componentLibrary) {
        if (component.startsWith('AbstractFormComponent.')) {
          continue;
        }
        // for now we must skip test for SelectBox and ScriptArea
        // SelectBox want use this.context.store and
        // ScriptArea has 'global leak detected' with react-ace
        if (component.endsWith('SelectBox') || component.endsWith('ScriptArea') || component.endsWith('RichTextArea')) {
          continue;
        }
        const ComponentType = componentLibrary[component];
        if (ComponentType.propTypes && ComponentType.propTypes.readOnly) {
          it('- ' + component, function testComponent() {
            const node = document.createElement('div');
            const componentProps = getCommonProps(ComponentType);
            const comp = ReactDOM.render(<ComponentType readOnly={false} {...componentProps}/>, node);
            if (comp.state.readOnly) {
              expect(comp.state.readOnly).to.be.equal(false);

              ReactDOM.render(<ComponentType readOnly {...componentProps} />, node);
              expect(comp.state.readOnly).to.be.equal(true);
            }
          });
        }
      }
    }
  });

  describe('- component change dynamicaly required', function test() {
    for (const componentLibrary of componentLibrariesBasic) {
      for (const component in componentLibrary) {
        if (component.endsWith('AbstractFormComponent')) {
          continue;
        }
        // for now we must skip test for SelectBox, ScriptArea, RichTextArea, EnumLabel
        // SelectBox want manager
        // ScriptArea has 'global leak detected' with react-ace
        // EnumLabel hasn't use for required
        // RichTextArea try to create state with EditorState, this can't be tested now.
        // ValidationMessage dont work with required
        if (component.endsWith('ScriptArea') || component.endsWith('EnumLabel')
              || component.endsWith('RichTextArea') || component.endsWith('SelectBox')
              || component.endsWith('ValidationMessage')) {
          continue;
        }
        const ComponentType = componentLibrary[component];
        if (ComponentType.propTypes && ComponentType.propTypes.required) {
          it('- ' + component, function testComponent() {
            const node = document.createElement('div');
            const comp = ReactDOM.render(<ComponentType
                                  title="Title" icon="user"
                                  label="label" enum={RoleTypeEnum}
                                  dateFormat="DD.MM.YYYY" mode="date"
                                  required />, node);

            // state must be set to true
            expect(comp.props.required).to.be.equal(true);

            // test with null data
            comp.setValue(null);
            expect(comp.isValid()).to.be.equal(false);

            // test with empty string
            comp.setValue('');
            expect(comp.isValid()).to.be.equal(false);

            if (component.endsWith('DateTimePicker')) {
              // test with string
              comp.setValue('2018-02-01');
            } else if (component.endsWith('EnumSelecBox')) {
              comp.setValue('SYSTEM');
            } else if (component.endsWith('Checkbox')) {
              comp.setValue(true);
            } else {
              // test with string
              comp.setValue('test');
            }
            expect(comp.isValid()).to.be.equal(true);

            // now test with required set to false
            ReactDOM.render(<ComponentType
                                  title="Title" icon="user"
                                  label="label" enum={RoleTypeEnum}
                                  dateFormat="DD.MM.YYYY" mode="date"
                                  required={false} />, node);

            // required must be set to false
            expect(comp.props.required).to.be.equal(false);

            // first test to valid must be true
            expect(comp.isValid()).to.be.equal(true);

            // test with null data
            comp.setValue(null);
            expect(comp.isValid()).to.be.equal(true);

            // test with empty string
            comp.setValue('');
            expect(comp.isValid()).to.be.equal(true);

            if (component.endsWith('DateTimePicker')) {
              // test with string
              comp.setValue('2018-02-01');
            } else if (component.endsWith('EnumSelecBox')) {
              comp.setValue('SYSTEM');
            } else if (component.endsWith('Checkbox')) {
              comp.setValue(true);
            } else {
              // test with string
              comp.setValue('test');
            }
            expect(comp.isValid()).to.be.equal(true);
          });
        }
      }
    }
  });
});
