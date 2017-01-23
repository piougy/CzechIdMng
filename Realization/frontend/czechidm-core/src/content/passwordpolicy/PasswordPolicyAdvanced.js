import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { PasswordPolicyManager, SecurityManager } from '../../redux';
import PasswordPolicyIdentityAttributeEnum from '../../enums/PasswordPolicyIdentityAttributeEnum';

/**
* Advanced detail for password policy
*/

const passwordPolicyManager = new PasswordPolicyManager();

class PasswordPolicyAdvanced extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.passwordPolicyManager = new PasswordPolicyManager();
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.passwordPolicies';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'password-policies', 'password-policies-advanced']);
    const { entityId } = this.props.params;
    this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
    this.context.store.dispatch(passwordPolicyManager.fetchEntity(entityId));
  }

  /**
  * Method check if props in this component is'nt different from new props.
  */
  componentWillReceiveProps(nextProps) {
    // check id of old and new entity
    if (!this.props.entity || nextProps.entity.id !== this.props.entity.id || nextProps.entity.id !== this.refs.form.getData().id) {
      this._initForm(nextProps.entity);
    }
  }

  /**
  * Method for basic initial form
  */
  _initForm(entity) {
    if (entity !== undefined && this.refs.form) {
      this.refs.form.setData(entity);
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
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    },
    this.refs.form.processStarted());

    // get data from forms on different tabs
    const entity = this.refs.form.getData();

    // transform identityAttributeCheck
    let identityAttributeCheck = [];
    for (const attribute in entity.identityAttributeCheck) {
      if (entity.identityAttributeCheck.hasOwnProperty(attribute)) {
        identityAttributeCheck.push(PasswordPolicyIdentityAttributeEnum.findKeyBySymbol(entity.identityAttributeCheck[attribute]));
      }
    }
    identityAttributeCheck = _.join(identityAttributeCheck, ', ');
    entity.identityAttributeCheck = identityAttributeCheck;

    this.context.store.dispatch(this.passwordPolicyManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this, entity, null, editContinue)));
  }

  /**
  * Method set showLoading to false and if is'nt error then show success message
  */
  _afterSave(entity, error, editContinue) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    if (editContinue === 'SAVE') {
      this.context.router.goBack();
    } else {
      this.refs.form.processEnded();
      this.setState({
        showLoading: false
      });
    }
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading } = this.state;
    return (
      <div className="tab-pane-panel-body">
        {
          showLoading || entity === null
          ?
          <Basic.Loading isStatic showLoading/>
          :
          <Basic.PanelHeader>
            <h2>
              <span>{entity.name} <small>{this.i18n('content.passwordPolicies.advanced.title')}</small></span>
            </h2>
            <div className="clearfix"></div>
          </Basic.PanelHeader>
        }
        <form onSubmit={this.save.bind(this)}>
          <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')} showLoading={entity === null}>
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
              onClick={this.save.bind(this, 'SAVE_CONTINUE')}
              rendered={SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')}
              showLoading={showLoading} pullRight dropup>
              <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'SAVE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
            </Basic.SplitButton>
          </Basic.PanelFooter>
        </form>
      </div>
    );
  }
}

PasswordPolicyAdvanced.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
PasswordPolicyAdvanced.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: passwordPolicyManager.getEntity(state, entityId),
    showLoading: passwordPolicyManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordPolicyAdvanced);
