import React, { PropTypes } from 'react';
import * as Basic from '../../components/basic';
import { PasswordPolicyManager, SecurityManager } from '../../redux';
import PasswordPolicyTypeEnum from '../../enums/PasswordPolicyTypeEnum';
import PasswordPolicyGenerateTypeEnum from '../../enums/PasswordPolicyGenerateTypeEnum';
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
      formData: props.entity
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
      this.refs.form.setData(entity);
    }
  }

  /**
   * Default save method that catch save event from form.
   */
  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();
    if (entity.id === undefined) {
      this.context.store.dispatch(this.passwordPolicyManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.passwordPolicyManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  /**
   * Method set showLoading to false and if is'nt error then show success message
   */
  _afterSave(entity, error) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.router.goBack();
  }

  /**
   * Prepare for dynamical change required
   */
  reloadForm(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      formData: this.refs.form.getData()
    });
  }

  render() {
    const { uiKey } = this.props;
    const { showLoading } = this.state;
    return (
      <div>
        <form onSubmit={this.save.bind(this)}>
          <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')} >
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

            <Basic.Checkbox ref="passwordLengthRequired"
              label={this.i18n('entity.PasswordPolicy.passwordLengthRequired')}/>
            <Basic.TextField ref="minPasswordLength"
              label={this.i18n('entity.PasswordPolicy.minPasswordLength')} />
            <Basic.TextField ref="maxPasswordLength"
              label={this.i18n('entity.PasswordPolicy.maxPasswordLength')} />

            <Basic.Checkbox ref="upperCharRequired"
              label={this.i18n('entity.PasswordPolicy.upperCharRequired')}/>
            <Basic.TextField ref="minUpperChar"
              label={this.i18n('entity.PasswordPolicy.minUpperChar')} />

            <Basic.Checkbox ref="lowerCharRequired"
              label={this.i18n('entity.PasswordPolicy.upperCharRequired')}/>
            <Basic.TextField ref="minLowerChar"
              label={this.i18n('entity.PasswordPolicy.minLowerChar')} />

            <Basic.Checkbox ref="numberRequired"
              label={this.i18n('entity.PasswordPolicy.numberRequired')}/>
            <Basic.TextField ref="minNumber"
              label={this.i18n('entity.PasswordPolicy.minNumber')} />

            <Basic.Checkbox ref="specialCharRequired"
              label={this.i18n('entity.PasswordPolicy.specialCharRequired')}/>
            <Basic.TextField ref="minSpecialChar"
              label={this.i18n('entity.PasswordPolicy.minSpecialChar')} />

            <Basic.TextField ref="prohibitedCharacters"
              label={this.i18n('entity.PasswordPolicy.prohibitedCharacters')} />

            <Basic.Checkbox ref="weakPassRequired"
              label={this.i18n('entity.PasswordPolicy.weakPassRequired')}/>
            <Basic.TextField ref="weakPass"
              label={this.i18n('entity.PasswordPolicy.weakPass')} />

            <Basic.TextField ref="passphraseWords" label={this.i18n('entity.PasswordPolicy.passphraseWords')} />

            <Basic.TextField ref="maxHistorySimilar" label={this.i18n('entity.PasswordPolicy.maxHistorySimilar')} />

            <Basic.TextField ref="maxPasswordAge" label={this.i18n('entity.PasswordPolicy.maxPasswordAge')} />
            <Basic.TextField ref="minPasswordAge" label={this.i18n('entity.PasswordPolicy.minPasswordAge')} />

            <Basic.Checkbox ref="enchancedControl" label={this.i18n('entity.PasswordPolicy.enchancedControl')}/>

            <Basic.TextField ref="minRulesToFulfill" label={this.i18n('entity.PasswordPolicy.minRulesToFulfill')} />
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </form>
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
