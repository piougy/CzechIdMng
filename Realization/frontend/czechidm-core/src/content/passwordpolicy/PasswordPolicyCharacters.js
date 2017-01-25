import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import { PasswordPolicyManager, SecurityManager } from '../../redux';
import PasswordPolicyIdentityAttributeEnum from '../../enums/PasswordPolicyIdentityAttributeEnum';

/**
* Character detail for password policy
* Bases and prohibited characters
*/

const passwordPolicyManager = new PasswordPolicyManager();

class PasswordPolicyCharacters extends Basic.AbstractContent {

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
    this.selectNavigationItems(['system', 'password-policies', 'password-policies-characters']);
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
              <span>{entity.name} <small>{this.i18n('content.passwordPolicies.characters.title')}</small></span>
            </h2>
            <div className="clearfix"></div>
          </Basic.PanelHeader>
        }
        <form onSubmit={this.save.bind(this)}>
          <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')} showLoading={entity === null}>
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

PasswordPolicyCharacters.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
PasswordPolicyCharacters.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: passwordPolicyManager.getEntity(state, entityId),
    showLoading: passwordPolicyManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordPolicyCharacters);
