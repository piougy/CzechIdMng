import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
import uuid from 'uuid';
import classNames from 'classnames';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import * as Domain from '../../domain';
import RoleTypeEnum from '../../enums/RoleTypeEnum';
import ConfigLoader from '../../utils/ConfigLoader';
//
import {
  RoleManager,
  RequestManager,
  SecurityManager,
  RoleCatalogueManager,
  ConfigurationManager,
  CodeListManager
} from '../../redux';

// Table uiKey
const requestManager = new RequestManager();
const codeListManager = new CodeListManager();

/**
* Table of roles.
*
* FIXME: use default RoleManager and rename prop to 'manager'.
*
* @author Radek Tomiška
*/
class RoleTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened
    };
    this.roleCatalogueManager = new RoleCatalogueManager();
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (this.props.showEnvironment) {
      this.context.store.dispatch(codeListManager.fetchCodeListIfNeeded('environment'));
    }
    this.refs.text.focus();
  }

  getContentKey() {
    return 'content.roles';
  }

  getManager() {
    return this.props.roleManager;
  }

  getDefaultSearchParameters() {
    let searchParameters = this.getManager().getDefaultSearchParameters();
    //
    if (this.props.showEnvironment) {
      searchParameters = searchParameters.setFilter('environment', ConfigLoader.getConfig('role.table.filter.environment', []));
    }
    //
    return searchParameters;
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    const filterData = Domain.SearchParameters.getFilterData(this.refs.filterForm);
    //
    // resolve additional filter options
    if (this.refs.roleCatalogue) {
      const roleCatalogue = this.refs.roleCatalogue.getValue();
      if (roleCatalogue && roleCatalogue.additionalOption) {
        filterData.roleCatalogue = null;
        filterData.withoutCatalogue = true;
      }
    }
    //
    this.refs.table.useFilterData(filterData);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    if (this.refs.roleCatalogue) {
      this.refs.roleCatalogue.setValue(null);
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  /**
   * Loads filter from redux state or default.
   */
  loadFilter() {
    if (!this.refs.filterForm) {
      return;
    }
    //  filters from redux
    const _searchParameters = this.getSearchParameters();
    if (_searchParameters) {
      const filterData = {};
      _searchParameters.getFilters().forEach((v, k) => {
        filterData[k] = v;
      });
      // set without catalogue option
      if (filterData.withoutCatalogue) {
        filterData.withoutCatalogue = null;
        filterData.roleCatalogue = this._getWithoutCatalogueOption();
      }
      //
      this.refs.filterForm.setData(filterData);
    }
  }

  _getWithoutCatalogueOption() {
    return {
      [Basic.SelectBox.NICE_LABEL]: this.i18n('filter.roleCatalogue.option.withoutCatalogue.label'),
      [Basic.SelectBox.ITEM_FULL_KEY]: this.i18n('filter.roleCatalogue.option.withoutCatalogue.label'),
      [Basic.SelectBox.ITEM_VALUE]: 'core:no-catalogue'
    };
  }

  _validateCreateRequestDialog(result) {
    if (result === 'reject') {
      return true;
    }
    if (result === 'confirm' && this.refs['new-request-form'].isFormValid()) {
      return true;
    }
    return false;
  }

  _focusOnRequestDialog() {
    this.refs['role-name'].focus();
  }

  createRequest(event) {
    if (event && event.preventDefault) {
      event.preventDefault();
    }

    this.refs[`confirm-new-request`].show(
      null,
      this.i18n(`content.roles.action.createRequest.header`),
      this._validateCreateRequestDialog.bind(this),
      this._focusOnRequestDialog.bind(this)
    ).then(() => {
      const roleName = this.refs[`role-name`].getValue();
      const promise = requestManager.getService().createRequest('roles', {name: roleName, baseCode: roleName});
      promise.then((json) => {
        // Init universal request manager (manually)
        const manager = this.getRequestManager({requestId: json.id}, new RoleManager());
        // Fetch entity - we need init permissions for new manager
        this.context.store.dispatch(manager.fetchEntityIfNeeded(json.ownerId, null, (e, error) => {
          this.handleError(error);
        }));
        // Redirect to new request
        this.context.history.push(`${this.addRequestPrefix('role', {requestId: json.id})}/${json.ownerId}/detail`);
      }).catch(ex => {
        this.setState({
          showLoading: false
        });
        this.handleError(ex);
      });
    });
  }

  showDetail(entity) {
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.history.push(`/role/${uuidId}/new?new=1`);
    } else {
      this.context.history.push(`/role/${ encodeURIComponent(entity.id) }/detail`);
    }
  }

  onDelete(bulkActionValue, selectedRows) {
    const { roleManager, uiKey } = this.props;
    const selectedEntities = roleManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs[`confirm-${ bulkActionValue }`].show(
      this.i18n(`action.${ bulkActionValue }.message`, {
        count: selectedEntities.length,
        record: roleManager.getNiceLabel(selectedEntities[0]),
        records: roleManager.getNiceLabels(selectedEntities).join(', ')
      }),
      this.i18n(`action.${ bulkActionValue }.header`, {
        count: selectedEntities.length,
        records: roleManager.getNiceLabels(selectedEntities).join(', ')
      })
    ).then(() => {
      this.context.store.dispatch(roleManager.deleteEntities(selectedEntities, uiKey, (entity, error, successEntities) => {
        if (entity && error) {
          // redirect to role detail with identities table
          if (error.statusEnum === 'ROLE_DELETE_FAILED_IDENTITY_ASSIGNED') {
            this.context.history.push(`/role/${ entity.id }/identities`);
            this.addMessage({
              position: 'tc',
              level: 'info',
              title: this.i18n('delete.identityAssigned.title'),
              message: this.i18n('delete.identityAssigned.message', { role: roleManager.getNiceLabel(entity) })
            });
          } else {
            this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: roleManager.getNiceLabel(entity) }) }, error);
          }
        }
        if (!error && successEntities) {
          this.refs.table.reload();
        }
      }));
    }, () => {
      // nothing
    });
  }

  _useFilterByTree(nodeId, event) {
    if (event) {
      event.preventDefault();
      // Stop propagation is important for prohibition of node tree expand.
      // After click on link node, we want only filtering ... not node expand.
      event.stopPropagation();
    }
    const data = {
      ...this.refs.filterForm.getData(),
      roleCatalogue: nodeId,
      withoutCatalogue: null
    };
    this.refs.roleCatalogue.setValue(nodeId);
    this.refs.table.useFilterData(data);
  }

  onChangeRoleCatalogue(option) {
    if (!option) {
      const { roleManager, uiKey, _searchParameters } = this.props;
      // cleanup redux state search parameters for additional options
      this.context.store.dispatch(roleManager.setSearchParameters(_searchParameters.clearFilter('withoutCatalogue'), uiKey));
    }
  }

  render() {
    const {
      uiKey,
      roleManager,
      columns,
      showCatalogue,
      forceSearchParameters,
      _requestsEnabled,
      className,
      showAddButton,
      showEnvironment,
      showBaseCode,
      prohibitedActions,
      treePaginationRootSize,
      treePaginationNodeSize
    } = this.props;
    const { filterOpened, showLoading } = this.state;
    const _showTree = showCatalogue && SecurityManager.hasAuthority('ROLECATALOGUE_AUTOCOMPLETE');
    //
    return (
      <Basic.Row
        className={ _showTree ? 'tree-select-wrapper' : '' }>
        <Basic.Confirm ref="confirm-new-request" level="success">
          <Basic.AbstractForm ref="new-request-form" uiKey="confirm-new-request" >
            <Basic.TextField
              label={ this.i18n('content.roles.action.createRequest.name') }
              ref="role-name"
              placeholder={ this.i18n('content.roles.action.createRequest.message') }
              required/>
          </Basic.AbstractForm>
        </Basic.Confirm>

        <Basic.Col
          lg={ 3 }
          rendered={ _showTree }
          className="tree-select-tree-container">
          <Advanced.Tree
            ref="roleCatalogueTree"
            uiKey="role-catalogue-tree"
            manager={ this.roleCatalogueManager }
            onSelect={ this._useFilterByTree.bind(this) }
            header={ this.i18n('content.roles.roleCataloguePick') }
            rendered={ _showTree }
            paginationRootSize={ treePaginationRootSize }
            paginationNodeSize={ treePaginationNodeSize }/>
        </Basic.Col>

        <Basic.Col
          lg={ !_showTree ? 12 : 9 }
          className="tree-select-table-container">
          <Basic.Confirm ref="confirm-delete" level="danger"/>

          <Advanced.Table
            ref="table"
            uiKey={ uiKey }
            manager={ roleManager }
            rowClass={ ({ rowIndex, data }) => Utils.Ui.getRowClass(data[rowIndex]) }
            columns={ columns }
            className={
              _showTree
              ?
              classNames('show-tree', className)
              :
              className
            }
            filterOpened={ filterOpened }
            forceSearchParameters={ forceSearchParameters }
            showRowSelection
            showLoading={ showLoading }
            prohibitedActions={ prohibitedActions }
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row className={ _showTree ? '' : 'last'}>
                    <Basic.Col lg={ showEnvironment ? 4 : 8 }>
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={this.i18n('content.roles.filter.text.placeholder')}
                        help={ Advanced.Filter.getTextHelp() }/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } className={ showEnvironment ? '' : 'hidden'}>
                      <Advanced.CodeListSelect
                        ref="environment"
                        code="environment"
                        label={ null }
                        placeholder={ this.i18n('entity.Role.environment.label') }
                        multiSelect/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                    </Basic.Col>
                  </Basic.Row>
                  <Basic.Row className="last" rendered={ _showTree }>
                    <Basic.Col lg={ 4 }>
                      <Advanced.Filter.RoleCatalogueSelect
                        ref="roleCatalogue"
                        label={ null }
                        placeholder={ this.i18n('entity.Role.roleCatalogue.name') }
                        header={ this.i18n('entity.Role.roleCatalogue.name') }
                        additionalOptions={[ this._getWithoutCatalogueOption() ]}
                        onChange={ this.onChangeRoleCatalogue.bind(this) }/>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            }
            buttons={[
              <span>
                <Basic.Button
                  level="success"
                  key="add_button"
                  className="btn-xs"
                  onClick={ this.showDetail.bind(this, { }) }
                  rendered={ showAddButton && !_requestsEnabled && SecurityManager.hasAuthority('ROLE_CREATE') }
                  icon="fa:plus">
                  { this.i18n('button.add') }
                </Basic.Button>
                <Basic.Button
                  level="success"
                  key="add_request"
                  className="btn-xs"
                  onClick={ this.createRequest.bind(this) }
                  rendered={ showAddButton && _requestsEnabled && SecurityManager.hasAuthority('ROLE_CREATE') }
                  icon="fa:plus">
                  { this.i18n('button.add') }
                </Basic.Button>
              </span>
            ]}
            _searchParameters={ this.getSearchParameters() }>

            <Advanced.Column
              header=""
              className="detail-button"
              cell={
                ({ rowIndex, data }) => {
                  return (
                    <Advanced.DetailButton
                      title={ this.i18n('button.detail') }
                      onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                  );
                }
              }
              sort={false}/>
            <Advanced.Column
              property="name"
              width="25%"
              sort
              face="text"
              cell={
                ({ rowIndex, data }) => (
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={ data[rowIndex].id }
                    entity={ data[rowIndex] }
                    face="popover"
                    showIcon/>
                )
              }/>
            <Advanced.Column
              property="baseCode"
              width={ 125 }
              face="text"
              sort
              rendered={ showBaseCode }/>
            <Advanced.Column
              property="environment"
              width={ 100 }
              face="text"
              sort
              rendered={ showEnvironment }
              cell={
                ({ rowIndex, data, property }) => (
                  <Advanced.CodeListValue code="environment" value={ data[rowIndex][property] }/>
                )
              }
            />
            <Advanced.Column
              property="roleType"
              width={ 75 }
              sort
              face="enum"
              enumClass={ RoleTypeEnum }
              rendered={ false }/>
            <Advanced.Column
              property="roleCatalogue.name"
              width={ 75 }
              face="text"/>
            <Advanced.Column
              property="description"
              sort
              face="text"
              maxLength={ 100 }/>
            <Advanced.Column
              property="disabled"
              sort
              face="bool"
              width={ 75 }/>
          </Advanced.Table>
        </Basic.Col>
      </Basic.Row>
    );
  }
}

RoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  roleManager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  /**
   * If role catalogue is shown
   */
  showCatalogue: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Css
   */
  className: PropTypes.string,
  /**
   * Button for create user will be shown
   */
  showAddButton: PropTypes.bool
};

RoleTable.defaultProps = {
  columns: ConfigLoader.getConfig('role.table.columns', ['name', 'baseCode', 'environment', 'description', 'disabled']),
  filterOpened: true,
  showCatalogue: true,
  forceSearchParameters: null,
  showAddButton: true
};

function select(state, component) {
  return {
    showEnvironment: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.show.environment', true),
    showBaseCode: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.show.role.baseCode', true),
    treePaginationRootSize: ConfigurationManager.getValue(state, 'idm.pub.app.show.roleCatalogue.tree.pagination.root.size'),
    treePaginationNodeSize: ConfigurationManager.getValue(state, 'idm.pub.app.show.roleCatalogue.tree.pagination.node.size'),
    columns: component.columns || ConfigurationManager.getPublicValueAsArray(
      state,
      'idm.pub.app.show.role.table.columns',
      RoleTable.defaultProps.columns
    ),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _requestsEnabled: ConfigurationManager.getPublicValueAsBoolean(state, component.roleManager.getEnabledPropertyKey())
  };
}

export default connect(select, null, null, { forwardRef: true })(RoleTable);
