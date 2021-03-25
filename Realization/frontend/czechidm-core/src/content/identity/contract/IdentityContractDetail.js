import PropTypes from 'prop-types';
import React from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { IdentityContractManager, IdentityManager, SecurityManager } from '../../../redux';
import ContractStateEnum from '../../../enums/ContractStateEnum';

const identityContractManager = new IdentityContractManager();

/**
 * Identity contract form.
 *
 * @author Radek Tomiška
 */
class IdentityContractDetail extends Advanced.AbstractFormableContent {

  constructor(props, context) {
    super(props, context);
    //
    this.identityManager = new IdentityManager();
    this.state = {
      _showLoading: false,
      validationErrors: null
    };
  }

  getContentKey() {
    return 'content.identity-contract.detail';
  }

  getManager() {
    return identityContractManager;
  }

  componentDidMount() {
    const { entity } = this.props;
    if (entity !== undefined) {
      this._setSelectedEntity(entity);
    }
  }

  _setSelectedEntity(entity) {
    const treeType = (
      entity._embedded && entity._embedded.workPosition && entity._embedded.workPosition._embedded
      ?
      entity._embedded.workPosition._embedded.treeType
      :
      null
    );
    const entityFormData = _.merge({}, entity);
    //
    this.setState({
      entityFormData
    }, () => {
      if (this.refs.workPosition) {
        if (treeType) {
          this.refs.workPosition.setTreeType(treeType);
          this.refs.workPosition.focus();
        }
      }
    });
  }

  save(afterAction, event) {
    if (event) {
      event.preventDefault();
    }
    //
    const { uiKey } = this.props;
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      _showLoading: true
    }, () => {
      const entity = this.refs.form.getData();
      const { identityId } = this.props.match.params;
      //
      const state = this.context.store.getState();
      const identity = this.identityManager.getEntity(state, identityId);
      entity.identity = identity.id;
      //
      if (Utils.Entity.isNew(entity)) {
        this.context.store.dispatch(this.getManager().createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
          if (!error) {
            this.addMessage({
              message: this.i18n('create.success', {
                position: this.getManager().getNiceLabel(createdEntity),
                username: this.identityManager.getNiceLabel(identity)
              })
            });
          }
          this._afterSave(createdEntity, error, afterAction);
        }));
      } else {
        this.context.store.dispatch(this.getManager().updateEntity(entity, `${uiKey}-detail`, (patchedEntity, error) => {
          if (!error) {
            this.addMessage({
              message: this.i18n('edit.success', {
                position: this.getManager().getNiceLabel(patchedEntity),
                username: this.identityManager.getNiceLabel(identity)
              })
            });
          }
          this._afterSave(patchedEntity, error, afterAction);
        }));
      }
    });
  }

  _afterSave(entity, error, afterAction = 'CLOSE') {
    if (error) {
      let validationErrors = null;
      if (error.statusEnum === 'FORM_INVALID' && error.parameters) {
        validationErrors = error.parameters.attributes;
        // focus the first invalid component
        if (validationErrors && validationErrors.length > 0) {
          // identity owner
          const firstValidationError = validationErrors[0];
          if (this.refs[firstValidationError.attributeCode] && firstValidationError.definitionCode === 'idm:basic-fields') {
            this.refs[firstValidationError.attributeCode].focus();
          }
        }
      }
      this.setState({
        _showLoading: false,
        validationErrors
      }, () => {
        this.addError(error);
      });
      //
      return;
    }
    //
    // ok
    this.setState({
      _showLoading: false,
      validationErrors: null
    }, () => {
      if (afterAction === 'CLOSE') {
        // go back to tree types or organizations
        this.context.history.goBack();
      } else {
        const { identityId } = this.props.match.params;
        this.context.history.replace(`/identity/${ encodeURIComponent(identityId) }/identity-contract/${ entity.id }/detail`);
      }
    });
  }

  onChangeState(state) {
    this.refs.disabled.setValue(state ? ContractStateEnum.isContractDisabled(state.value) : null);
  }

  render() {
    const { uiKey, entity, showLoading, match, userContext, _permissions } = this.props;
    const { _showLoading, entityFormData, validationErrors } = this.state;
    const formInstance = this.getBasicAttributesFormInstance(entity);
    const _readOnly = !identityContractManager.canSave(entity, _permissions);
    //
    return (
      <Basic.Div>
        <Helmet title={ Utils.Entity.isNew(entity) ? this.i18n('create.title') : this.i18n('edit.title') } />

        <form onSubmit={ this.save.bind(this, 'CONTINUE')}>
          <Basic.Panel className={ Utils.Entity.isNew(entity) ? '' : 'no-border last' }>
            <Basic.PanelHeader text={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('label') } />

            <Basic.PanelBody style={ Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }>
              <Basic.AbstractForm
                ref="form"
                data={ entityFormData }
                showLoading={ _showLoading || showLoading }
                uiKey={ uiKey }
                readOnly={ _readOnly }>
                <Basic.LabelWrapper readOnly ref="identity" label={ this.i18n('entity.IdentityContract.identity') }>
                  <Advanced.IdentityInfo username={ match.params.identityId }/>
                </Basic.LabelWrapper>

                <Basic.TextField
                  ref="position"
                  label={ this.i18n('entity.IdentityContract.position') }
                  readOnly={ this.isReadOnly(formInstance, 'position') }
                  required={ this.isRequired(formInstance, 'position', _readOnly) }
                  min={ this.getMin(formInstance, 'position', _readOnly) }
                  max={ this.getMax(formInstance, 'position', _readOnly, 255) }
                  validationMessage={ this.getValidationMessage(formInstance, 'position') }
                  validationErrors={
                    this.getInvalidBasicField(validationErrors, 'position')
                  }/>

                <Advanced.TreeNodeSelect
                  ref="workPosition"
                  header={ this.i18n('entity.IdentityContract.workPosition') }
                  treeNodeLabel={ this.i18n('entity.IdentityContract.workPosition') }
                  useFirstType
                  readOnly={ this.isReadOnly(formInstance, 'workPosition', _readOnly) }
                  required={ this.isRequired(formInstance, 'workPosition', _readOnly) }
                  validationMessage={ this.getValidationMessage(formInstance, 'workPosition') }
                  validationErrors={
                    this.getInvalidBasicField(validationErrors, 'workPosition')
                  }/>

                <Basic.DateTimePicker
                  mode="date"
                  ref="validFrom"
                  label={ this.i18n('label.validFrom') }
                  readOnly={ this.isReadOnly(formInstance, 'validFrom', _readOnly) }
                  required={ this.isRequired(formInstance, 'validFrom', _readOnly) }
                  minDate={ this.getMinDate(formInstance, 'validFrom', _readOnly) }
                  maxDate={ this.getMaxDate(formInstance, 'validFrom', _readOnly) }
                  validationMessage={ this.getValidationMessage(formInstance, 'validFrom') }
                  validationErrors={
                    this.getInvalidBasicField(validationErrors, 'validFrom')
                  }/>

                <Basic.DateTimePicker
                  mode="date"
                  ref="validTill"
                  label={ this.i18n('label.validTill') }
                  readOnly={ this.isReadOnly(formInstance, 'validTill', _readOnly) }
                  required={ this.isRequired(formInstance, 'validTill', _readOnly) }
                  minDate={ this.getMinDate(formInstance, 'validTill', _readOnly) }
                  maxDate={ this.getMaxDate(formInstance, 'validTill', _readOnly) }
                  validationMessage={ this.getValidationMessage(formInstance, 'validTill') }
                  validationErrors={
                    this.getInvalidBasicField(validationErrors, 'validTill')
                  }/>

                <Basic.Checkbox
                  ref="main"
                  label={ this.i18n('entity.IdentityContract.main.label') }
                  helpBlock={ this.i18n('entity.IdentityContract.main.help') }/>

                <Basic.Checkbox
                  ref="externe"
                  label={ this.i18n('entity.IdentityContract.externe') }/>

                <Basic.EnumSelectBox
                  ref="state"
                  enum={ ContractStateEnum }
                  useSymbol={ false }
                  label={ this.i18n('entity.IdentityContract.state.label') }
                  helpBlock={ this.i18n('entity.IdentityContract.state.help') }
                  onChange={ this.onChangeState.bind(this) }/>

                <Basic.Checkbox
                  ref="disabled"
                  label={ this.i18n('entity.IdentityContract.disabled.label') }
                  helpBlock={ this.i18n('entity.IdentityContract.disabled.help') }
                  readOnly />

                <Basic.TextArea
                  ref="description"
                  label={ this.i18n('entity.IdentityContract.description') }
                  rows={ 4 }
                  max={ 1000 }/>
              </Basic.AbstractForm>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" showLoading={ _showLoading } onClick={ this.context.history.goBack }>
                { this.i18n('button.back') }
              </Basic.Button>

              {
                !entity || !entity._embedded || !entity._embedded.identity || !entity._embedded.identity.formProjection
                ||
                <Basic.Button
                  type="button"
                  level="link"
                  rendered={ SecurityManager.hasAllAuthorities(['FORMPROJECTION_UPDATE'], userContext) && this.isDevelopment() }
                  onClick={ () => this.context.history.push(`/form-projections/${ entity._embedded.identity.formProjection }/detail`) }>
                  { this.i18n('content.identity.projection.button.formProjection.label') }
                </Basic.Button>
              }

              <Basic.SplitButton
                level="success"
                title={ this.i18n('button.saveAndContinue') }
                onClick={ this.save.bind(this, 'CONTINUE') }
                showLoading={ _showLoading }
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }
                rendered={ !_readOnly }
                pullRight
                dropup>
                <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'CLOSE')}>
                  { this.i18n('button.saveAndClose') }
                </Basic.MenuItem>
              </Basic.SplitButton>
            </Basic.PanelFooter>
          </Basic.Panel>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
        </form>
      </Basic.Div>
    );
  }
}

IdentityContractDetail.propTypes = {
  entity: PropTypes.object,
  type: PropTypes.string,
  uiKey: PropTypes.string.isRequired,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
IdentityContractDetail.defaultProps = {
  _permissions: null
};

function select(state, component) {
  return {
    userContext: state.security.userContext,
    _permissions: identityContractManager.getPermissions(state, null, component.match.params.entityId)
  };
}
export default connect(select)(IdentityContractDetail);
