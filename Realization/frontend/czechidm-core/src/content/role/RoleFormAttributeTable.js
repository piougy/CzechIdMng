import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { RoleFormAttributeManager, FormAttributeManager, RoleManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import Joi from 'joi';

let manager = new RoleFormAttributeManager();
let roleManager = new RoleManager();
const formAttributeManager = new FormAttributeManager();

/**
* Table of role attributes (sub-definition)
*
* @author Vít Švanda
*/
export class RoleFormAttributeTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.formAttributes';
  }

  getManager() {
    // Init manager - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    manager = this.getRequestManager(this.props.params, manager);
    roleManager = this.getRequestManager(this.props.params, roleManager);
    return manager;
  }

  showDetail(entity) {
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    super.showDetail(entity, () => {
      this.refs.formAttribute.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ message: this.i18n('save.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
    }
    //
    super.afterSave(entity, error);
  }

  _onChangeFormAttribute(value) {
    if (value) {
      this.refs.defaultValue.setValue(value.defaultValue);
      this.refs.required.setValue(value.required);
    }
  }

  render() {
    const { forceSearchParameters, _showLoading, _permissions, className, formDefinition } = this.props;
    const { detail } = this.state;
    const role = forceSearchParameters.getFilters().get('role');
    const formAttributeForceSearch = new SearchParameters()
        .setFilter('definitionId', formDefinition);
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ this.getUiKey() }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          className={ className }
          showRowSelection={ manager.canDelete() }
          _searchParameters={ this.getSearchParameters() }
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
                onClick={ this.showDetail.bind(this, { role }) }
                rendered={ manager.canSave() }>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                { this.i18n('button.add') }
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
            property="_embedded.formAttribute.code"
            sortProperty="created"
            face="text"
            header={ this.i18n('entity.RoleFormAttribute.formAttribute.label') }
            sort/>
          <Advanced.Column
            property="defaultValue"
            face="text"
            header={ this.i18n('entity.RoleFormAttribute.defaultValue.label') }/>
          <Advanced.Column
            property="required"
            face="bool"
            header={ this.i18n('entity.FormAttribute.required') }/>
          <Advanced.Column
            property="unique"
            face="bool"
            header={ this.i18n('entity.FormAttribute.unique.label') }/>
          <Advanced.Column
            property="regex"
            face="text"
            header={ this.i18n('entity.FormAttribute.regex.label') }/>
          <Advanced.Column
            property="min"
            face="text"
            header={ this.i18n('entity.FormAttribute.min.label') }/>
          <Advanced.Column
            property="max"
            face="text"
            header={ this.i18n('entity.FormAttribute.max.label') }/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={ !_showLoading } text={ this.i18n('create.header')} rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header closeButton={ !_showLoading } text={ this.i18n('edit.header', { name: manager.getNiceLabel(detail.entity) }) } rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading }
                readOnly={ !manager.canSave(detail.entity, _permissions) }>
                <Basic.SelectBox
                  ref="role"
                  manager={ roleManager }
                  label={ this.i18n('entity.RoleFormAttribute.role.label') }
                  readOnly
                  required/>
                <Basic.SelectBox
                  ref="formAttribute"
                  manager={ formAttributeManager }
                  label={ this.i18n('entity.RoleFormAttribute.formAttribute.label') }
                  forceSearchParameters={formAttributeForceSearch}
                  onChange={this._onChangeFormAttribute.bind(this)}
                  useFirst
                  required/>
                <Basic.TextField
                  ref="defaultValue"
                  label={ this.i18n('entity.RoleFormAttribute.defaultValue.label') }
                />
                <Basic.Checkbox
                  ref="required"
                  label={this.i18n('entity.FormAttribute.required')}/>
                <Basic.Checkbox
                  ref="unique"
                  label={this.i18n('entity.FormAttribute.unique.label')}/>
                <Basic.TextField
                  ref="regex"
                  label={this.i18n('entity.FormAttribute.regex.label')}
                  helpBlock={this.i18n('entity.FormAttribute.regex.help')}
                  />
                <Basic.TextField
                  ref="min"
                  label={this.i18n('entity.FormAttribute.min.label')}
                  validation={Joi.number().precision(2).allow(null)}/>
                <Basic.TextField
                  ref="max"
                  label={this.i18n('entity.FormAttribute.max.label')}
                  validation={Joi.number().precision(2).allow(null)}/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                rendered={ manager.canSave(detail.entity, _permissions) }
                showLoading={ _showLoading}
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

RoleFormAttributeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  //
  _showLoading: PropTypes.bool
};

RoleFormAttributeTable.defaultProps = {
  forceSearchParameters: null,
  _showLoading: false
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(RoleFormAttributeTable);
