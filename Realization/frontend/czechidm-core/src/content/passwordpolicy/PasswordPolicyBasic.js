import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Utils from '../../utils';
import { PasswordPolicyManager, SecurityManager } from '../../redux';
import PasswordPolicyTypeEnum from '../../enums/PasswordPolicyTypeEnum';
import PasswordPolicyGenerateTypeEnum from '../../enums/PasswordPolicyGenerateTypeEnum';
import PasswordPolicyIdentityAttributeEnum from '../../enums/PasswordPolicyIdentityAttributeEnum';

/**
* Basic detail for password policy,
* this detail is also used for create entity
*/

const passwordPolicyManager = new PasswordPolicyManager();

class PasswordPolicyBasic extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.passwordPolicyManager = new PasswordPolicyManager();
    this.state = {
      showLoading: false
    };
    if (this._getIsNew()) {
      this.state = {
        validateType: true
      };
    }
  }

  getContentKey() {
    return 'content.passwordPolicies';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'password-policies', 'password-policies-basic']);
    const { entityId } = this.props.params;
    if (this._getIsNew()) {
      // if entity is new, set default required rules
      this.context.store.dispatch(passwordPolicyManager.receiveEntity(entityId, {
        type: PasswordPolicyTypeEnum.findKeyBySymbol(PasswordPolicyTypeEnum, PasswordPolicyTypeEnum.VALIDATE),
        passwordLengthRequired: true,
        upperCharRequired: true,
        lowerCharRequired: true,
        numberRequired: true
      }));
    } else {
      this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(passwordPolicyManager.fetchEntity(entityId));
    }
  }

  /**
  * Method check if props in this component is'nt different from new props.
  */
  componentWillReceiveProps(nextProps) {
    if (!this.props.entity || nextProps.entity.id !== this.props.entity.id || nextProps.entity.id !== this.refs.form.getData().id) {
      this._initForm(nextProps.entity);
    }
  }

  _getIsNew() {
    return this.props.isNew;
  }

  /**
  * Method for basic initial form
  */
  _initForm(entity) {
    if (entity && this.refs.form) {
      this._changeType(entity.type);
      const loadedEntity = _.merge({}, entity);
      loadedEntity.identityAttributeCheck = this._transformAttributeToCheck(entity.identityAttributeCheck);
      this.refs.form.setData(loadedEntity);
    }
  }

  /**
   * Method tranform password policy attribute to check to
   * enum
   */
  _transformAttributeToCheck(identityAttributeCheck) {
    // transform identityAttributeCheck
    const identityAttributeCheckFinal = [];
    const attrs = _.split(identityAttributeCheck, ', ');
    for (const attribute in attrs) {
      if (attrs.hasOwnProperty(attribute)) {
        identityAttributeCheckFinal.push(PasswordPolicyIdentityAttributeEnum.findSymbolByKey(attrs[attribute]));
      }
    }
    return identityAttributeCheckFinal;
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

    if (entity.id === undefined) {
      this.context.store.dispatch(this.passwordPolicyManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error, editContinue);
      }));
    } else {
      this.context.store.dispatch(this.passwordPolicyManager.patchEntity(entity, `${uiKey}-detail`, (savedEntity, error) => {
        this._afterSave(entity, error, editContinue);
      }));
    }
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
    if (editContinue === 'SAVE' || editContinue === 'CREATE') {
      this.context.router.goBack();
    } else if (editContinue === 'CREATE_CONTINUE') {
      this.context.router.replace('/password-policies/' + entity.id);
    } else {
      this.refs.form.processEnded();
      this.setState({
        showLoading: false
      });
    }
  }

  _changeType(value) {
    if (value && PasswordPolicyTypeEnum.findSymbolByKey(value.value) === PasswordPolicyTypeEnum.VALIDATE ||
            PasswordPolicyTypeEnum.findSymbolByKey(value) === PasswordPolicyTypeEnum.VALIDATE) {
      this.setState({
        validateType: true
      });
    } else {
      this.setState({
        validateType: false
      });
    }
  }

  render() {
    const { uiKey, entity, isNew } = this.props;
    const { showLoading, validateType } = this.state;
    return (
      <form onSubmit={this.save.bind(this)}>
        <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
          <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('content.passwordPolicies.basic.title')} />
          <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
            <Basic.AbstractForm ref="form" data={entity} uiKey={uiKey} className="form-horizontal" readOnly={!SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')} showLoading={entity === null}>
              <Basic.EnumSelectBox
                ref="type" onChange={this._changeType.bind(this)}
                required readOnly={!Utils.Entity.isNew(entity)}
                enum={PasswordPolicyTypeEnum}
                label={this.i18n('entity.PasswordPolicy.type')}/>
              <Basic.TextField
                ref="name"
                label={this.i18n('entity.PasswordPolicy.name')}
                required
                max={255}/>
              <Basic.EnumSelectBox
                ref="generateType" required={!validateType} hidden={validateType}
                enum={PasswordPolicyGenerateTypeEnum}
                label={this.i18n('entity.PasswordPolicy.generateType')}/>

              <Basic.TextField ref="passphraseWords"
                hidden={validateType}
                label={this.i18n('entity.PasswordPolicy.passphraseWords')} />

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

              <Basic.TextField ref="maxPasswordAge" label={this.i18n('entity.PasswordPolicy.maxPasswordAge')} />

              <Basic.TextField ref="minPasswordAge" label={this.i18n('entity.PasswordPolicy.minPasswordAge')} />
            </Basic.AbstractForm>
          </Basic.PanelBody>
          <Basic.PanelFooter showLoading={showLoading} className="noBorder">
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            {
              isNew
              ?
              <Basic.SplitButton level="success" title={this.i18n('button.createContinue')}
                onClick={this.save.bind(this, 'CREATE_CONTINUE')}
                rendered={SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')}
                showLoading={showLoading} pullRight dropup>
                <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'CREATE')}>{this.i18n('button.create')}</Basic.MenuItem>
              </Basic.SplitButton>
              :
              <Basic.SplitButton level="success" title={this.i18n('button.saveAndContinue')}
                onClick={this.save.bind(this, 'SAVE_CONTINUE')}
                rendered={SecurityManager.hasAuthority('PASSWORDPOLICY_WRITE')}
                showLoading={showLoading} pullRight dropup>
                <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'SAVE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
              </Basic.SplitButton>
            }
          </Basic.PanelFooter>
        </Basic.Panel>
      </form>
    );
  }
}

PasswordPolicyBasic.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
  isNew: PropTypes.bool
};
PasswordPolicyBasic.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: passwordPolicyManager.getEntity(state, entityId),
    _showLoading: passwordPolicyManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(PasswordPolicyBasic);
