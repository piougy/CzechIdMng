import React, { PropTypes } from 'react';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { PasswordPolicyManager, SecurityManager } from '../../redux';
import PasswordPolicyTypeEnum from '../../enums/PasswordPolicyTypeEnum';
import PasswordPolicyGenerateTypeEnum from '../../enums/PasswordPolicyGenerateTypeEnum';
import PasswordPolicyIdentityAttributeEnum from '../../enums/PasswordPolicyIdentityAttributeEnum';

/**
* Detail for password policy
* for type GENERATE and validate is used only one
* detail
*/
export default class PasswordPolicyDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.passwordPolicyManager = new PasswordPolicyManager();
    this.state = {
      showLoading: false,
      formData: props.entity,
      activeKey: 1
    };
  }

  getContentKey() {
    return 'content.passwordPolicies';
  }

  componentDidMount() {
    const { entity } = this.props;
    this.selectNavigationItem('passwordPolicies');
    this._initForm(entity);
  }

  /**
  * Method check if props in this component is'nt different from new props.
  */
  componentWillReceiveProps(nextProps) {
    // check id of old and new entity
    if (nextProps.entity.id !== this.props.entity.id) {
      this._initForm(nextProps.entity);
    }
  }

  /**
  * Method for basic initial form
  */
  _initForm(entity) {
    if (entity !== undefined) {
      // TODO: set diff between generate and validate
      // this.refs.name.focus();
      this.refs.form1.setData(entity);
      this.refs.form2.setData(entity);
      this.refs.form3.setData(entity);
    }
  }

  /**
  * Default save method that catch save event from form.
  */
  save(editContinue = 'CLOSE', event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form1.isFormValid()) {
      return;
    }
    if (!this.refs.form2.isFormValid()) {
      return;
    }
    if (!this.refs.form3.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    },
    this.refs.form1.processStarted(),
    this.refs.form2.processStarted(),
    this.refs.form3.processStarted());

    // get data from forms on different tabs
    const entity1 = this.refs.form1.getData();
    const entity2 = this.refs.form2.getData();
    const entity3 = this.refs.form3.getData();

    // transform identityAttributeCheck
    let identityAttributeCheck = [];
    for (const attribute in entity2.identityAttributeCheck) {
      if (entity2.identityAttributeCheck.hasOwnProperty(attribute)) {
        identityAttributeCheck.push(PasswordPolicyIdentityAttributeEnum.findKeyBySymbol(entity2.identityAttributeCheck[attribute]));
      }
    }
    identityAttributeCheck = _.join(identityAttributeCheck, ', ');
    const entity = {
      // form1
      id: entity1.id,
      type: entity1.type,
      name: entity1.name,
      description: entity1.description,
      generateType: entity1.generateType,
      disabled: entity1.disabled,
      defaultPolicy: entity1.defaultPolicy,
      minPasswordLength: entity1.minPasswordLength,
      maxPasswordLength: entity1.maxPasswordLength,
      minUpperChar: entity1.minUpperChar,
      minLowerChar: entity1.minLowerChar,
      minNumber: entity1.minNumber,
      minSpecialChar: entity1.minSpecialChar,
      passphraseWords: entity1.passphraseWords,
      // form2
      passwordLengthRequired: entity2.passwordLengthRequired,
      lowerCharRequired: entity2.lowerCharRequired,
      upperCharRequired: entity2.upperCharRequired,
      specialCharRequired: entity2.specialCharRequired,
      numberRequired: entity2.numberRequired,
      enchancedControl: entity2.enchancedControl,
      minRulesToFulfill: entity2.minRulesToFulfill,
      maxPasswordAge: entity2.maxPasswordAge,
      minPasswordAge: entity2.minPasswordAge,
      maxHistorySimilar: entity2.maxHistorySimilar,
      identityAttributeCheck,
      // form3
      prohibitedCharacters: entity3.prohibitedCharacters,
      weakPassRequired: entity3.weakPassRequired,
      weakPass: entity3.weakPass,
      lowerCharBase: entity3.lowerCharBase,
      upperCharBase: entity3.upperCharBase,
      numberBase: entity3.numberBase,
      specialCharBase: entity3.specialCharBase
    };

    if (entity.id === undefined) {
      this.context.store.dispatch(this.passwordPolicyManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error, editContinue);
      }));
    } else {
      this.context.store.dispatch(this.passwordPolicyManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this, entity, null, editContinue)));
    }
  }

  /**
  * Method set showLoading to false and if is'nt error then show success message
  */
  _afterSave(entity, error, editContinue) {
    if (error) {
      this.setState({
        showLoading: false
      }, this._processEndedAllForm());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    if (editContinue === 'SAVE_CONTINUE') {
      this.context.router.goBack();
    } else {
      this._processEndedAllForm();
      this.setState({
        showLoading: false
      });
    }
  }

  _processEndedAllForm() {
    this.refs.form1.processEnded();
    this.refs.form2.processEnded();
    this.refs.form3.processEnded();
  }

  _selectTab(key, event) {
    if (event) {
      event.preventDefault();
    }
    if (!isNaN(parseFloat(key)) && isFinite(key)) {
      this.setState({
        activeKey: key
      });
    }
  }

  render() {
    const { uiKey } = this.props;
    const { showLoading, activeKey } = this.state;
    return (
      <div>
        <Basic.Tabs onSelect={this._selectTab.bind(this)} activeKey={activeKey} >
          <Basic.Tab eventKey={1} title={this.i18n('entity.PasswordPolicy.basic')}>
            <form onSubmit={this.save.bind(this)}>
              <Basic.AbstractForm ref="form1" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')} >
                <Basic.EnumSelectBox
                  ref="type"
                  enum={PasswordPolicyTypeEnum}
                  label={this.i18n('entity.PasswordPolicy.type')}/>
                <Basic.TextField
                  ref="name"
                  label={this.i18n('entity.PasswordPolicy.name')}
                  required
                  max={255}/>
                <Basic.EnumSelectBox
                  ref="generateType"
                  enum={PasswordPolicyGenerateTypeEnum}
                  label={this.i18n('entity.PasswordPolicy.generateType')}/>
                <Basic.Checkbox ref="disabled" label={this.i18n('entity.PasswordPolicy.disabled')}/>

                <Basic.Checkbox ref="defaultPolicy" label={this.i18n('entity.PasswordPolicy.defaultPolicy')}/>

                <Basic.TextArea ref="description" label={this.i18n('entity.PasswordPolicy.description')}/>

                <Basic.TextField ref="minPasswordLength"
                  label={this.i18n('entity.PasswordPolicy.minPasswordLength')} />
                <Basic.TextField ref="maxPasswordLength"
                  label={this.i18n('entity.PasswordPolicy.maxPasswordLength')} />

                <Basic.TextField ref="minUpperChar"
                  label={this.i18n('entity.PasswordPolicy.minUpperChar')} />

                <Basic.TextField ref="minLowerChar"
                  label={this.i18n('entity.PasswordPolicy.minLowerChar')} />

                <Basic.TextField ref="minNumber"
                  label={this.i18n('entity.PasswordPolicy.minNumber')} />

                <Basic.TextField ref="minSpecialChar"
                  label={this.i18n('entity.PasswordPolicy.minSpecialChar')} />
                <Basic.TextField ref="passphraseWords" label={this.i18n('entity.PasswordPolicy.passphraseWords')} />
              </Basic.AbstractForm>

              <Basic.PanelFooter showLoading={showLoading} >
                <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
                <Basic.SplitButton level="success" title={this.i18n('button.saveAndContinue')}
                  onClick={this.save.bind(this, 'SAVE')}
                  rendered={SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')}
                  showLoading={showLoading} pullRight dropup>
                  <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'SAVE_CONTINUE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
                </Basic.SplitButton>
              </Basic.PanelFooter>
            </form>
          </Basic.Tab>
          <Basic.Tab eventKey={2} title={this.i18n('entity.PasswordPolicy.enchancedControl')}>
            <form onSubmit={this.save.bind(this)}>
              <Basic.AbstractForm ref="form2" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')} >
                <Basic.Checkbox ref="enchancedControl" label={this.i18n('entity.PasswordPolicy.enchancedControl')}/>

                <Basic.Checkbox ref="passwordLengthRequired"
                  label={this.i18n('entity.PasswordPolicy.passwordLengthRequired')}/>

                <Basic.Checkbox ref="upperCharRequired"
                  label={this.i18n('entity.PasswordPolicy.upperCharRequired')}/>

                <Basic.Checkbox ref="lowerCharRequired"
                  label={this.i18n('entity.PasswordPolicy.lowerCharRequired')}/>

                <Basic.Checkbox ref="numberRequired"
                  label={this.i18n('entity.PasswordPolicy.numberRequired')}/>

                <Basic.Checkbox ref="specialCharRequired"
                    label={this.i18n('entity.PasswordPolicy.specialCharRequired')}/>

                <Basic.TextField ref="minRulesToFulfill" label={this.i18n('entity.PasswordPolicy.minRulesToFulfill')} />

                <Basic.TextField ref="maxPasswordAge" label={this.i18n('entity.PasswordPolicy.maxPasswordAge')} />
                <Basic.TextField ref="minPasswordAge" label={this.i18n('entity.PasswordPolicy.minPasswordAge')} />

                <Basic.TextField ref="maxHistorySimilar" label={this.i18n('entity.PasswordPolicy.maxHistorySimilar')} />
                <Basic.EnumSelectBox ref="identityAttributeCheck"
                  enum={PasswordPolicyIdentityAttributeEnum}
                  multiSelect label={this.i18n('entity.PasswordPolicy.identityAttributeCheck')} />
              </Basic.AbstractForm>

              <Basic.PanelFooter showLoading={showLoading} >
                <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
                <Basic.SplitButton level="success" title={this.i18n('button.saveAndContinue')}
                  onClick={this.save.bind(this, 'SAVE')}
                  rendered={SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')}
                  showLoading={showLoading} pullRight dropup>
                  <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'SAVE_CONTINUE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
                </Basic.SplitButton>
              </Basic.PanelFooter>
            </form>
          </Basic.Tab>
          <Basic.Tab eventKey={3} title={this.i18n('entity.PasswordPolicy.bases')}>
            <form onSubmit={this.save.bind(this)}>
              <Basic.AbstractForm ref="form3" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')} >
                <Basic.TextField ref="prohibitedCharacters"
                  label={this.i18n('entity.PasswordPolicy.prohibitedCharacters')} />

                <Basic.Checkbox ref="weakPassRequired"
                  label={this.i18n('entity.PasswordPolicy.weakPassRequired')}/>
                <Basic.TextField ref="weakPass"
                  label={this.i18n('entity.PasswordPolicy.weakPass')} />

                <Basic.TextField ref="lowerCharBase"
                  label={this.i18n('entity.PasswordPolicy.lowerCharBase')} />
                <Basic.TextField ref="upperCharBase"
                  label={this.i18n('entity.PasswordPolicy.upperCharBase')} />
                <Basic.TextField ref="numberBase"
                  label={this.i18n('entity.PasswordPolicy.numberBase')} />
                <Basic.TextField ref="specialCharBase"
                  label={this.i18n('entity.PasswordPolicy.specialCharBase')} />
              </Basic.AbstractForm>

              <Basic.PanelFooter showLoading={showLoading} >
                <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
                <Basic.SplitButton level="success" title={this.i18n('button.saveAndContinue')}
                  onClick={this.save.bind(this, 'SAVE')}
                  rendered={SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')}
                  showLoading={showLoading} pullRight dropup>
                  <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'SAVE_CONTINUE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
                </Basic.SplitButton>
              </Basic.PanelFooter>
            </form>
          </Basic.Tab>
        </Basic.Tabs>
      </div>
    );
  }
}

PasswordPolicyDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
PasswordPolicyDetail.defaultProps = {
};
