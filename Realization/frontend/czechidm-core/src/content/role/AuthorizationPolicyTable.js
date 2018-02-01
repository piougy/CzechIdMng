import React, { PropTypes } from 'react';
import _ from 'lodash';
import { connect } from 'react-redux';
import Joi from 'joi';
import Immutable from 'immutable';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
//
import { RoleManager, AuthorizationPolicyManager, DataManager } from '../../redux';

const DEFAULT_EVALUATOR_TYPE = 'eu.bcvsolutions.idm.core.security.evaluator.BasePermissionEvaluator';
const manager = new AuthorizationPolicyManager();
const roleManager = new RoleManager();

/**
* Table of role's granted permissions
*
* @author Radek Tomiška
*/
export class AuthorizationPolicyTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      ...this.state,
      authorizableType: null,
      evaluatorType: null
    };
  }

  getContentKey() {
    return 'content.role.authorization-policies';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
    this.context.store.dispatch(this.getManager().fetchSupportedEvaluators());
    this.context.store.dispatch(this.getManager().fetchAuthorizableTypes());
    this.context.store.dispatch(roleManager.fetchAllAuthorities());
    this.context.store.dispatch(roleManager.fetchAvailableAuthorities());
  }

  showDetail(entity) {
    const { forceSearchParameters, supportedEvaluators, authorizableTypes } = this.props;
    //
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    let roleId = null;
    if (forceSearchParameters.getFilters().has('roleId')) {
      roleId = forceSearchParameters.getFilters().get('roleId');
    }
    //
    const entityFormData = _.merge({}, entity, {
      role: entity._embedded && entity._embedded.role ? entity._embedded.role : roleId,
      basePermissions: entity.basePermissions ? entity.basePermissions.split(',') : null
    });
    if (entity.evaluatorProperties) {
      _.keys(entity.evaluatorProperties).map(parameterName => {
        entityFormData[`parameter-${parameterName}`] = entity.evaluatorProperties[parameterName];
      });
    }
    //
    let authorizableType = null;
    const _authorizableType = !entity.groupPermission ? null : authorizableTypes.find(type => { return type.group === entity.groupPermission; });
    if (_authorizableType) {
      authorizableType = {
        niceLabel: this._getAuthorizableTypeNiceLabel(_authorizableType._authorizableType, _authorizableType.type),
        value: _authorizableType.group,
        group: _authorizableType.group,
        type: _authorizableType.type
      };
    }
    this.setState({
      detail: {
        show: true,
        entity: entityFormData
      },
      evaluatorType: supportedEvaluators.has(entity.evaluatorType) ? supportedEvaluators.get(entity.evaluatorType) : null,
      authorizableType
    }, () => {
      this.refs.form.setData(entityFormData);
      this.refs.groupPermission.focus();
    });
  }

  closeDetail() {
    this.setState({
      detail: {
        ...this.state.detail,
        show: false
      },
      authorizableType: null,
      evaluatorType: null
    });
  }

  save(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formEntity = this.refs.form.getData();
    const { evaluatorType } = this.state;
    //
    // transform parameters
    if (evaluatorType.parameters) {
      formEntity.evaluatorProperties = {};
      evaluatorType.parameters.map(parameterName => {
        formEntity.evaluatorProperties[parameterName] = this.refs[`parameter-${parameterName}`].getValue();
      });
    }
    // transform base permissions
    if (formEntity.basePermissions) {
      formEntity.basePermissions = formEntity.basePermissions.join(',');
    }
    // transform authorizableType
    if (formEntity.groupPermission) {
      formEntity.authorizableType = formEntity.groupPermission.type;
      formEntity.groupPermission = formEntity.groupPermission.group;
    }
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
      // TODO: trimmed vs. not trimmed view ...
      this.refs.table.getWrappedInstance().reload();
    }
    //
    super.afterSave(entity, error);
  }

  onChangeAuthorizableType(authorizableType) {
    const { evaluatorType } = this.state;
    const newState = { authorizableType };
    //
    if (authorizableType && authorizableType.type === null) {
      newState.evaluatorType = DEFAULT_EVALUATOR_TYPE;
    } else if (evaluatorType && Utils.Ui.getSimpleJavaType(evaluatorType.entityType) !== 'Identifiable') {
      newState.evaluatorType = null;
    }
    //
    this.setState(newState, () => {
      if (newState.evaluatorType !== undefined) {
        this.refs.evaluatorType.setValue(newState.evaluatorType);
      }
      this.refs.basePermissions.setValue(null);
    });
  }

  onChangeEvaluatorType(evaluatorType) {
    this.setState({
      evaluatorType
    }, () => {
      if (evaluatorType && !evaluatorType.supportsPermissions) {
        this.refs.basePermissions.setValue(null);
      }
    });
  }

  /**
   * Resolve bese permission with module localization
   *
   * @param  {[type]} perrmission [description]
   * @return {[type]}             [description]
   */
  _getBasePermissionNiceLabel(permission) {
    let _permission = permission;
    const { allAuthorities } = this.props;
    if (!allAuthorities) {
      // will be refreshed after authorities are loaded
      return null;
    }
    if (!permission.name) {
      _permission = this._getUniqueBasePermissions(this.props.allAuthorities).get(permission);
    }
    //
    return this.i18n(`${_permission.module ? _permission.module : 'core'}:permission.base.${_permission.name}`, { defaultValue: _permission.name });
  }

  _getGroupPermissionNiceLabel(groupPermission) {
    return this.i18n(`${groupPermission.module ? groupPermission.module : 'core'}:permission.group.${groupPermission.name}`, { defaultValue: groupPermission.name});
  }

  _getAuthorizableTypeNiceLabel(groupName, authorizableType) {
    const { allAuthorities } = this.props;
    //
    if (!allAuthorities) {
      return '';
    }
    const groupPermission = allAuthorities.find(p => {
      return p.name === groupName;
    });
    if (!groupPermission) {
      return '';
    }
    //
    const label = this._getGroupPermissionNiceLabel(groupPermission);
    return `${label}${!authorizableType ? '' : (' (' + Utils.Ui.getSimpleJavaType(authorizableType) + ')')}`;
  }

  _getUniqueBasePermissions(allAuthorities, authorizableType = null) {
    let _uniqueBasePermissions = new Immutable.Map();
    if (allAuthorities) {
      allAuthorities.forEach(groupPermission => {
        if (!authorizableType || authorizableType.group === groupPermission.name) {
          groupPermission.permissions.forEach(permission => {
            _uniqueBasePermissions = _uniqueBasePermissions.set(permission.name, permission);
          });
        }
      });
    }
    return _uniqueBasePermissions;
  }

  render() {
    const {
      uiKey,
      columns,
      forceSearchParameters,
      _showLoading,
      supportedEvaluators,
      authorizableTypes,
      allAuthorities,
      availableAuthorities,
      _permissions } = this.props;
    const { detail, evaluatorType, authorizableType } = this.state;
    //
    const _supportedEvaluators = [];
    if (supportedEvaluators) {
      supportedEvaluators.forEach(evaluator => {
        // TODO: add filter to BE and evaluate all superclasses
        if ((!authorizableType && (Utils.Ui.getSimpleJavaType(evaluator.entityType) === 'Identifiable') && (Utils.Ui.getSimpleJavaType(evaluator.evaluatorType) !== 'CodeableEvaluator'))
            || (authorizableType && (authorizableType.type === evaluator.entityType || Utils.Ui.getSimpleJavaType(evaluator.entityType) === 'Identifiable'))) {
          _supportedEvaluators.push({
            niceLabel: Utils.Ui.getSimpleJavaType(evaluator.evaluatorType),
            value: evaluator.id,
            description: evaluator.description,
            parameters: evaluator.parameters,
            supportsPermissions: evaluator.supportsPermissions
          });
        }
      });
    }
    const _authorizableTypes = [];
    if (authorizableTypes) {
      authorizableTypes.forEach(type => {
        _authorizableTypes.push({
          niceLabel: this._getAuthorizableTypeNiceLabel(type.group, type.type),
          value: type.group,
          type: type.type,
          group: type.group
        });
      });
      _authorizableTypes.sort((one, two) => {
        return one.niceLabel.localeCompare(two.niceLabel);
      });
    }
    const _uniqueBasePermissions = this._getUniqueBasePermissions(allAuthorities, authorizableType);
    const _basePermissions = _uniqueBasePermissions.toArray().map(permission => {
      return {
        niceLabel: this._getBasePermissionNiceLabel(permission),
        value: permission.name
      };
    }).sort((one, two) => {
      return one.niceLabel.localeCompare(two.niceLabel);
    });
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ manager.canDelete() }
          rowClass={
            ({rowIndex, data}) => {
              // installed vs. available authorities - authority from disabled module
              if (availableAuthorities && !availableAuthorities.find(p => { return p.name === data[rowIndex].groupPermission; })) {
                return 'disabled';
              }
              return Utils.Ui.getDisabledRowClass(data[rowIndex]);
            }
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, { evaluatorType: DEFAULT_EVALUATOR_TYPE }) }
                rendered={ _supportedEvaluators.length > 0 && manager.canSave() }>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
            ]
          }>

          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column
            property="authorizableType"
            header={ this.i18n('entity.AuthorizationPolicy.authorizableType.label') }
            sort
            rendered={_.includes(columns, 'authorizableType')}
            width={ 175 }
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const propertyValue = data[rowIndex][property];
                return (
                  <span title={propertyValue}>{ this._getAuthorizableTypeNiceLabel(data[rowIndex].groupPermission, propertyValue) }</span>
                );
              }
            }/>
          <Advanced.Column
            property="basePermissions"
            header={ this.i18n('entity.AuthorizationPolicy.basePermissions.label') }
            rendered={_.includes(columns, 'basePermissions')}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const propertyValue = data[rowIndex][property];
                if (!propertyValue) {
                  return null;
                }
                return propertyValue.split(',').map(permission => {
                  return (
                    <div>{ this._getBasePermissionNiceLabel(permission) }</div>
                  );
                });
              }
            }/>
          <Advanced.Column
            property="evaluatorType"
            sort
            rendered={_.includes(columns, 'evaluatorType')}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data, property }) => {
                const propertyValue = data[rowIndex][property];
                return (
                  <span title={propertyValue}>{ Utils.Ui.getSimpleJavaType(propertyValue) }</span>
                );
              }
            }/>
          <Advanced.Column
            face="text"
            header={ this.i18n('entity.AuthorizationPolicy.evaluatorProperties.label') }
            rendered={_.includes(columns, 'evaluatorProperties')}
            cell={
              /* eslint-disable react/no-multi-comp */
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (!entity.evaluatorProperties) {
                  return null;
                }
                return _.keys(entity.evaluatorProperties).map(parameterName => {
                  if (parameterName.lastIndexOf('core:', 0) === 0) {
                    return null;
                  }
                  return (<div>{parameterName}: {entity.evaluatorProperties[parameterName]}</div>);
                });
              }
            }/>
          <Advanced.Column
            width="25%"
            property="description"
            face="text"
            sort
            rendered={_.includes(columns, 'description')}/>
          <Advanced.Column
            property="disabled"
            face="bool"
            sort
            rendered={_.includes(columns, 'disabled')}/>
          <Advanced.Column
            property="seq"
            face="text"
            sort
            rendered={_.includes(columns, 'seq')}/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('create.header')} rendered={Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Header closeButton={!_showLoading} text={this.i18n('edit.header', { name: detail.entity.name })} rendered={!Utils.Entity.isNew(detail.entity)}/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={_showLoading}
                readOnly={ !manager.canSave(detail.entity, _permissions) }>
                <Basic.Row>
                  <div className="col-lg-6">
                    <Basic.SelectBox
                      ref="role"
                      manager={ roleManager }
                      label={ this.i18n('entity.AuthorizationPolicy.role') }
                      readOnly={ !Utils.Entity.isNew(detail.entity) || !_.includes(columns, 'role') }
                      required/>
                    <Basic.EnumSelectBox
                      ref="groupPermission"
                      options={ _authorizableTypes }
                      onChange={ this.onChangeAuthorizableType.bind(this) }
                      label={ this.i18n('entity.AuthorizationPolicy.authorizableType.label') }
                      palceholder={ this.i18n('entity.AuthorizationPolicy.authorizableType.placeholder') }
                      helpBlock={ this.i18n('entity.AuthorizationPolicy.authorizableType.help') }
                      searchable
                      useObject/>
                    <Basic.EnumSelectBox
                      ref="basePermissions"
                      options={ _basePermissions }
                      label={ this.i18n('entity.AuthorizationPolicy.basePermissions.label') }
                      palceholder={ this.i18n('entity.AuthorizationPolicy.basePermissions.placeholder') }
                      helpBlock={ this.i18n('entity.AuthorizationPolicy.basePermissions.help') }
                      readOnly={ (evaluatorType && evaluatorType.supportsPermissions !== undefined) ? !evaluatorType.supportsPermissions : false }
                      searchable
                      multiSelect/>
                    <Basic.TextField
                      ref="seq"
                      validation={Joi.number().integer().min(0).max(9999).allow(null)}
                      label={ this.i18n('entity.AuthorizationPolicy.seq.label') }
                      help={ this.i18n('entity.AuthorizationPolicy.seq.help') }/>
                    <Basic.TextArea
                      ref="description"
                      label={this.i18n('entity.AuthorizationPolicy.description.label')}
                      max={2000}/>
                    <Basic.Checkbox
                      ref="disabled"
                      label={ this.i18n('entity.AuthorizationPolicy.disabled.label') }
                      helpBlock={ this.i18n('entity.AuthorizationPolicy.disabled.help') }/>
                  </div>
                  <div className="col-lg-6">
                    <Basic.EnumSelectBox
                      ref="evaluatorType"
                      options={ _supportedEvaluators }
                      onChange={ this.onChangeEvaluatorType.bind(this) }
                      label={ this.i18n('entity.AuthorizationPolicy.evaluatorType.label') }
                      helpBlock={ evaluatorType ? evaluatorType.description : null }
                      readOnly={ authorizableType ? authorizableType.type === null : false }
                      required/>
                    <Basic.Alert
                      text={ this.i18n('evaluator.default') }
                      rendered={ authorizableType ? authorizableType.type === null : false }/>
                    {
                      !evaluatorType || !evaluatorType.parameters || evaluatorType.parameters.length === 0
                      ||
                      <div>
                        <Basic.ContentHeader text={ this.i18n('entity.AuthorizationPolicy.evaluatorProperties.title') } />
                        {
                          evaluatorType.parameters.map(parameterName => {
                            return (
                              <Basic.TextField
                                label={ parameterName }
                                ref={ `parameter-${parameterName}` }
                                max={ 255 }/>
                            );
                          })
                        }
                      </div>
                    }
                  </div>
                </Basic.Row>

              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={this.closeDetail.bind(this)}
                showLoading={_showLoading}>
                {this.i18n('button.close')}
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={ manager.canSave(detail.entity, _permissions) }>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

AuthorizationPolicyTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  //
  _showLoading: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};

AuthorizationPolicyTable.defaultProps = {
  columns: ['disabled', 'description', 'seq', 'authorizableType', 'evaluatorType', 'evaluatorProperties', 'basePermissions'],
  forceSearchParameters: null,
  _showLoading: false,
  _permissions: null
};

function select(state, component) {
  return {
    supportedEvaluators: DataManager.getData(state, AuthorizationPolicyManager.UI_KEY_SUPPORTED_EVALUATORS),
    authorizableTypes: DataManager.getData(state, AuthorizationPolicyManager.UI_KEY_AUTHORIZABLE_TYPES),
    availableAuthorities: DataManager.getData(state, RoleManager.UI_KEY_AVAILABLE_AUTHORITIES),
    allAuthorities: DataManager.getData(state, RoleManager.UI_KEY_ALL_AUTHORITIES),
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`)
      || Utils.Ui.isShowLoading(state, AuthorizationPolicyManager.UI_KEY_SUPPORTED_EVALUATORS)
      || Utils.Ui.isShowLoading(state, AuthorizationPolicyManager.UI_KEY_AUTHORIZABLE_TYPES)
      || Utils.Ui.isShowLoading(state, RoleManager.UI_KEY_ALL_AUTHORITIES_UIKEY),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`)
  };
}

export default connect(select)(AuthorizationPolicyTable);
