# Wizard component

Basic component for managing a wizard with steps. Extended from AbstractContextComponent.

## Parameters
All parameters from AbstractContextComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| type  | 'basic', 'circle', 'point'   | Type of steps navigation.| 'point' |
| show  | boolean  | Is wizard show? | false |
| modal  | boolean  | Show wizard in the modal window. | true |
| getSteps  | func  | Returns array of wizard's steps. |  |
| onCloseWizard  | func  | This method is calls on wizard close or finish it. |  |
| id  | string  | Wizard unique identifier. Using for dynamic localization. |  |
| module  | string  | Module identifier. Using for dynamic localization. |  |

Wizard component uses 'wizardContext' (context.wizardContext).

## Usage

```html
_getSteps() {
    return ([
      {
        id: 'stepOne',
        label: 'Step one',
        help: 'Help for step one.',
        getComponent: () => {
          return <h1>This is step One detail</h1>;
        }
      },
      {
        id: 'stepTwo',
        label: 'Step two',
        help: 'Help for step two.',
        getComponent: () => {
          return <h1>This is step Two detail</h1>;
        }
      },
      {
        id: 'stepFinish',
        label: 'Step finish',
        help: 'Help for step finish.',
        getComponent: () => {
          return <h1>This is finish of the wizard.</h1>;
        }
      }
    ]);
  }

  render() {
    const {show, modal} = this.props;
    const wizardContext = this.wizardContext;

    return (
      <IdmContext.Provider value={{...this.context, wizardContext}}>
        <Basic.Wizard
          getSteps={this._getSteps.bind(this)}
          modal={modal}
          show={show}
          module="acc"
          id="wizard-one"
          onCloseWizard={this.props.closeWizard}/>
      </IdmContext.Provider>
    );
  }
```
