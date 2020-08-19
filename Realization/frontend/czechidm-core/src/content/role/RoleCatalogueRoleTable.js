import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { RoleCatalogueRoleManager, RoleManager } from '../../redux';

let manager = new RoleCatalogueRoleManager();
let roleManager = new RoleManager();

/**
* Table of role catalogues - assigned to given role
*
* @author Radek Tomiška
*/
export class RoleCatalogueRoleTable extends Advanced.AbstractTableContent {

  getContentKey() {
    return 'content.role.catalogues';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    // Init manager - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    manager = this.getRequestManager(this.props.match.params, manager);
    roleManager = this.getRequestManager(this.props.match.params, roleManager);
    return manager;
  }

  showDetail(entity) {
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    super.showDetail(entity, () => {
      this.refs.roleCatalogue.focus();
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

  render() {
    const {
      uiKey,
      forceSearchParameters,
      _showLoading,
      _permissions,
      className,
      columns
    } = this.props;
    const { detail } = this.state;
    let role = null;
    if (forceSearchParameters.getFilters().has('roleId')) {
      role = forceSearchParameters.getFilters().get('roleId');
    }
    let roleCatalogue = null;
    if (forceSearchParameters.getFilters().has('roleCatalogueId')) {
      roleCatalogue = forceSearchParameters.getFilters().get('roleCatalogueId');
    }
    //
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ manager.canDelete() }
          className={ className }
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
                onClick={ this.showDetail.bind(this, { role, roleCatalogue }) }
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
            property="role"
            sortProperty="role.code"
            face="text"
            sort
            rendered={ _.includes(columns, 'role') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={ entity.role }
                    entity={ entity._embedded.role }
                    face="popover"
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            property="roleCatalogue"
            sortProperty="roleCatalogue.name"
            face="text"
            rendered={ _.includes(columns, 'roleCatalogue') }
            sort
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="roleCatalogue"
                    entityIdentifier={ entity.roleCatalogue }
                    entity={ entity._embedded.roleCatalogue }
                    face="popover"/>
                );
              }
            }/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header
              icon="fa:list-alt"
              closeButton={ !_showLoading }
              text={ this.i18n('create.header')}
              rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header
              icon="fa:list-alt"
              closeButton={ !_showLoading }
              text={ this.i18n('edit.header', { name: manager.getNiceLabel(detail.entity) }) }
              rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading }
                readOnly={ !manager.canSave(detail.entity, _permissions) }>

                <Basic.SelectBox
                  ref="role"
                  manager={ roleManager }
                  label={ this.i18n('entity.RoleGuaranteeRole.role.label') }
                  readOnly={ role !== null }
                  required/>

                <Advanced.RoleCatalogueSelect
                  ref="roleCatalogue"
                  label={ this.i18n('entity.RoleCatalogueRole.roleCatalogue.label') }
                  helpBlock={ this.i18n('entity.RoleCatalogueRole.roleCatalogue.help') }
                  header={ this.i18n('entity.RoleCatalogueRole.roleCatalogue.label') }
                  readOnly={ roleCatalogue !== null }
                  required/>
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
      </Basic.Div>
    );
  }
}

RoleCatalogueRoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Columns rendered in table
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  //
  _showLoading: PropTypes.bool
};

RoleCatalogueRoleTable.defaultProps = {
  forceSearchParameters: null,
  columns: ['role', 'roleCatalogue'],
  _showLoading: false
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(RoleCatalogueRoleTable);
