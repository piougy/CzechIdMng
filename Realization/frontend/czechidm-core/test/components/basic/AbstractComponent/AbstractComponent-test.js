import React from 'react';
import TestUtils from 'react-addons-test-utils';
import chai, { expect } from 'chai';
import ReactDOM from 'react-dom';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);
//
import * as Basic from '../../../../src/components/basic';
import * as Advanced from '../../../../src/components/advanced';
import RoleTypeEnum from '../../../../src/enums/RoleTypeEnum';

const componentLibraries = [Basic, Advanced];

const componentLibrariesBasic = [Basic];

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
            shallowRenderer.render(<ComponentType title="Title" icon="user" show value="empty" text="Text" label="label" rendered={false} />);
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
            // fill some commons properties to ensure more component types wil be rendered
            shallowRenderer.render(<ComponentType title="Title" icon="user" show value="empty" text="Text" label="label" showLoading={false}/>);
            const renderedComponent = shallowRenderer.getRenderOutput();
            shallowRenderer.render(<ComponentType title="Title" icon="user" show value="empty" text="Text" label="label" showLoading/>);
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

  describe('- component change dynamical readOnly', function test() {
    for (const componentLibrary of componentLibrariesBasic) {
      for (const component in componentLibrary) {
        if (component.startsWith('AbstractFormComponent.')) {
          continue;
        }
        // for now we must skip test for SelectBox and ScriptArea
        // SelectBox want use this.context.store and
        // ScriptArea has 'global leak detected' with react-ace
        if (component.endsWith('SelectBox') || component.endsWith('ScriptArea')) {
          continue;
        }
        const ComponentType = componentLibrary[component];
        if (ComponentType.propTypes && ComponentType.propTypes.readOnly) {
          it('- ' + component, function testComponent() {
            const node = document.createElement('div');
            const comp = ReactDOM.render(<ComponentType title="Title" icon="user" show value="empty" text="Text" label="label" enum={RoleTypeEnum} readOnly={false} />, node);
            if (comp.state.readOnly) {
              expect(comp.state.readOnly).to.be.equal(false);

              ReactDOM.render(<ComponentType title="Title" icon="user" show value="empty" text="Text" label="label" enum={RoleTypeEnum} readOnly />, node);
              expect(comp.state.readOnly).to.be.equal(true);
            }
          });
        }
      }
    }
  });
});
