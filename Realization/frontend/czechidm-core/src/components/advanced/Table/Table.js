import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import invariant from 'invariant';
import _ from 'lodash';
import Immutable from 'immutable';
import moment from 'moment';
import classnames from 'classnames';
import { Dropdown, MenuItem } from 'react-bootstrap';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import * as Domain from '../../../domain';
import UuidInfo from '../UuidInfo/UuidInfo';
import RefreshButton from './RefreshButton';
import Filter from '../Filter/Filter';
import {
  DataManager,
  FormAttributeManager,
  LongRunningTaskManager,
  SecurityManager,
  ConfigurationManager,
  AuditManager
} from '../../../redux';
import EavAttributeForm from '../Form/EavAttributeForm';
import LongRunningTask from '../LongRunningTask/LongRunningTask';
import { selectEntities } from '../../../redux/selectors';

const DEFAULT_QUICK_BUTTON_COUNT = 5;
const auditManager = new AuditManager();
const dataManager = new DataManager();

/**
 * Table component with header and columns.
 *
 * @author Radek Tomiška
 */
class AdvancedTable extends Basic.AbstractContextComponent {

  constructor(props, context) {
    super(props, context);
    // resolve static frontend actions (backend actions are loaded asynchronously ... check componentDidMount)
    const { manager, actions } = this.props;
    let _actions = [];
    if (!manager.supportsBulkAction() && actions !== null && actions.length > 0) {
      _actions = actions
        .filter(action => action.rendered === undefined || action.rendered === true || action.rendered === null)
        .map(action => {
          action.showWithSelection = (action.showWithSelection === null || action.showWithSelection === undefined)
            ? true
            : action.showWithSelection;
          action.showWithoutSelection = (action.showWithoutSelection === null || action.showWithoutSelection === undefined)
            ? false
            : action.showWithoutSelection;
          //
          return action;
        });
    }
    //
    this.state = {
      filterOpened: this.props.filterOpened,
      selectedRows: this.props.selectedRows,
      removedRows: new Immutable.Set(),
      showBulkActionDetail: false,
      bulkActionShowLoading: false,
      _actions
    };
    this.attributeManager = new FormAttributeManager();
    this.longRunningTaskManager = new LongRunningTaskManager();
  }

  /**
   * Return component identifier, with can be used in localization etc.
   *
   * @return {string} component identifier
   */
  getComponentKey() {
    return 'component.advanced.Table';
  }

  _getBulkActionLabel(backendBulkAction) {
    return this.i18n(`${backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }.label`, {
      defaultValue: backendBulkAction.description || backendBulkAction.name
    });
  }

  componentDidMount() {
    const { manager, initialReload } = this.props;
    if (initialReload) {
      this.reload();
    }
    //
    if (manager.supportsBulkAction() && manager.canRead()) {
      this.context.store.dispatch(manager.fetchAvailableBulkActions((actions, error) => {
        if (error) {
          if (error.statusCode === 403) {
            // user doesn't have permissions for work with entities in the table
          } else {
            this.addErrorMessage({}, error);
          }
        } else {
          // TODO: react elements are stored in state ... redesign raw data and move cached actions into reducer
          const _actions = actions
            .map(backendBulkAction => {
              const actionKey = `${ backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }`;
              const iconKey = `${ actionKey }.icon`;
              const icon = backendBulkAction.icon || this.i18n(iconKey);
              const label = this._getBulkActionLabel(backendBulkAction);
              return {
                value: backendBulkAction.name,
                order: backendBulkAction.order,
                actionKey,
                icon,
                label,
                level: backendBulkAction.level && backendBulkAction.level !== 'SUCCESS' ? backendBulkAction.level.toLowerCase() : 'default',
                deleteAction: backendBulkAction.deleteAction,
                quickButton: backendBulkAction.quickButton,
                niceLabel: (
                  <span key={ `b-a-${backendBulkAction.name}` }>
                    <Basic.Icon
                      value={ icon }
                      rendered={ backendBulkAction.module + icon !== iconKey }
                      style={{ marginRight: 5, width: 18, textAlign: 'center' }}/>
                    { label }
                  </span>
                ),
                action: this.showBulkActionDetail.bind(this, backendBulkAction),
                disabled: !SecurityManager.hasAllAuthorities(backendBulkAction.authorities),
                showWithSelection: backendBulkAction.showWithSelection,
                showWithoutSelection: backendBulkAction.showWithoutSelection,
              };
            });
          //
          this.setState({
            _actions
          });
        }
      }));
    }
  }

  UNSAFE_componentWillReceiveProps(newProps) {
    if (!Domain.SearchParameters.is(newProps.forceSearchParameters, this.props.forceSearchParameters)) {
      this.reload(newProps);
    } else if (!Domain.SearchParameters.is(newProps.defaultSearchParameters, this.props.defaultSearchParameters)) {
      this.reload(newProps);
    } else if (newProps.rendered !== this.props.rendered) {
      this.reload(newProps);
    }
  }

  reload(props = null) {
    let _props = this.props;
    if (props) {
      _props = {
        ...this.props,
        ...props
      };
    }
    const { rendered, _searchParameters } = _props;
    if (!rendered) {
      return;
    }
    this.fetchEntities(_searchParameters, _props);
  }

  /**
   * Clears row selection
   */
  clearSelectedRows() {
    this.setState({
      selectedRows: []
    });
  }

  /**
   * Process select row in table. The method is ised only for bulk action.
   * Methot has own behavior for select all.
   *
   * @param  rowIndex
   * @param  selected
   * @return
   */
  selectRowForBulkAction(rowIndex, selected) {
    const { selectedRows, removedRows } = this.state;
    let newRemovedRows = new Immutable.Set(removedRows);
    let newSelectedRows = new Immutable.Set(selectedRows);
    if (rowIndex === -1) {
      // de/select all rows
      // reset selected rows
      newSelectedRows = new Immutable.Set();
      if (selected) {
        newSelectedRows = newSelectedRows.add(Basic.Table.SELECT_ALL);
      } else {
        newSelectedRows = newSelectedRows.remove(Basic.Table.SELECT_ALL);
      }
      // reset removed rows
      newRemovedRows = new Immutable.Set();
    } else {
      const recordId = this.refs.table.getIdentifier(rowIndex);
      // de/select one row
      if (_.includes(selectedRows, Basic.Table.SELECT_ALL)) {
        if (selected) {
          newRemovedRows = newRemovedRows.remove(recordId);
        } else {
          newRemovedRows = newRemovedRows.add(recordId);
        }
      } else {
        newSelectedRows = (selected ? newSelectedRows.add(recordId) : newSelectedRows.remove(recordId));
      }
    }
    this.setState({
      selectedRows: newSelectedRows.toArray(),
      removedRows: newRemovedRows
    });
    return newSelectedRows;
  }

  isAllRowSelected() {
    const { selectedRows } = this.state;
    // if selected rows contains SELECT ALL return true
    return _.includes(selectedRows, Basic.Table.SELECT_ALL);
  }

  isRowSelected(identifier) {
    const { selectedRows, removedRows } = this.state;
    if (_.includes(selectedRows, Basic.Table.SELECT_ALL)) {
      return !removedRows.has(identifier);
    }
    return _.includes(selectedRows, identifier);
  }

  prevalidateBulkAction(bulkAction, event) {
    if (event) {
      event.preventDefault();
    }
    const _searchParameters = this._mergeSearchParameters(this.props._searchParameters);

    const { selectedRows, removedRows } = this.state;

    if (bulkAction) {
      const bulkActionToProcess = {
        ...bulkAction
      };
      const { manager } = this.props;
      // remove unnecessary attributes
      delete bulkActionToProcess.formAttributes;
      delete bulkActionToProcess.longRunningTaskId;
      delete bulkActionToProcess.permissions;
      //
      bulkActionToProcess.properties = this.refs.bulkActionAttributes.getValues();
      if (_.includes(selectedRows, Basic.Table.SELECT_ALL)) {
        bulkActionToProcess.filter = _searchParameters.getFilters().toJSON();
        bulkActionToProcess.removeIdentifiers = removedRows.toArray();
      } else {
        bulkActionToProcess.identifiers = selectedRows;
      }
      //
      this.setState({
        bulkActionShowLoading: true
      }, () => {
        this.context.store.dispatch(manager.prevalidateBulkAction(bulkActionToProcess, (resultModel, error) => {
          if (error) {
            this.addErrorMessage({}, error);
            this.setState({
              bulkActionShowLoading: false
            });
          } else if (resultModel) {
            const { backendBulkAction } = this.state;
            backendBulkAction.prevalidateResult = resultModel;
            this.setState({
              bulkActionShowLoading: false,
              backendBulkAction
            });
          } else {
            this.setState({
              bulkActionShowLoading: false
            });
          }
        }));
      });
    }
  }

  processBulkAction(bulkAction, event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.bulkActionAttributes.isValid()) {
      return;
    }
    const _searchParameters = this._mergeSearchParameters(this.props._searchParameters);

    const { selectedRows, removedRows } = this.state;

    if (bulkAction) {
      const bulkActionToProcess = {
        ...bulkAction
      };
      const { manager } = this.props;
      // remove unnecessary attributes
      delete bulkActionToProcess.formAttributes;
      delete bulkActionToProcess.longRunningTaskId;
      delete bulkActionToProcess.permissions;
      //
      bulkActionToProcess.properties = this.refs.bulkActionAttributes.getValues();
      if (_.includes(selectedRows, Basic.Table.SELECT_ALL)) {
        bulkActionToProcess.filter = _searchParameters.toFilterJson();
        bulkActionToProcess.removeIdentifiers = removedRows.toArray();
      } else {
        bulkActionToProcess.identifiers = selectedRows;
      }
      //
      this.setState({
        bulkActionShowLoading: true
      });
      this.context.store.dispatch(manager.processBulkAction(bulkActionToProcess, (processBulkAction, error) => {
        if (error) {
          this.addErrorMessage({}, error);
          this.setState({
            bulkActionShowLoading: false
          });
        } else {
          this.addMessage({
            level: 'info',
            message: this.i18n('bulkAction.created', {
              longRunningTaskId: processBulkAction.longRunningTaskId,
              name: this.i18n(`${ processBulkAction.module }:eav.bulk-action.${ processBulkAction.name }.label`)
            })
          });
          this.setState({
            selectedRows: [],
            removedRows: new Immutable.Set(),
            bulkActionShowLoading: false,
            backendBulkAction: processBulkAction
          });
        }
      }));
    }
  }

  /**
   * Callback after bulk action ends - called only if LRT detail is shown till end.
   *
   * @param  {object} processedBulkAction currently processed bulk action
   * @since 10.6.0
   */
  _afterBulkAction(processedBulkAction) {
    const { afterBulkAction } = this.props;
    let isReload = true;
    if (afterBulkAction) {
      isReload = afterBulkAction(processedBulkAction);
    }
    //
    if (isReload !== false) { // null + undefined + true
      this.reload();
    }
  }

  /**
   * Merge hard, default and user deffined search parameters.
   */
  _mergeSearchParameters(searchParameters, props = null) {
    const _props = props || this.props;
    const { defaultSearchParameters, forceSearchParameters, manager, defaultPageSize } = _props;
    //
    let _forceSearchParameters = null;
    if (forceSearchParameters) {
      _forceSearchParameters = forceSearchParameters.setSize(null).setPage(null); // we dont want override setted pagination
    }
    let _searchParameters = manager.mergeSearchParameters(
      searchParameters || defaultSearchParameters || manager.getDefaultSearchParameters(), _forceSearchParameters
    );
    // default page size by profile
    // defaultPage size in not stored in redux
    if ((!searchParameters || !searchParameters.getSize()) && defaultPageSize) {
      _searchParameters = _searchParameters.setSize(defaultPageSize);
    }
    //
    return _searchParameters;
  }

  fetchEntities(searchParameters, props = null) {
    const _props = props || this.props;
    searchParameters = this._mergeSearchParameters(searchParameters, _props);
    const { onReload } = _props;
    //
    if (!_props.hideTableShowLoading) {
      this._fetchEntities(searchParameters, _props, (data, error) => {
        if (onReload) {
          onReload(data, error);
        }
      });
    } else {
      this.setState({
        hideTableShowLoading: _props.hideTableShowLoading
      }, () => {
        this._fetchEntities(searchParameters, _props, (data, error) => {
          this.setState({
            hideTableShowLoading: null
          }, () => {
            if (onReload) {
              onReload(data, error);
            }
          });
        });
      });
    }
  }

  _fetchEntities(searchParameters, _props, cb) {
    const { uiKey, manager } = _props;
    //
    this.context.store.dispatch(manager.fetchEntities(searchParameters, uiKey, (json, error) => {
      if (error) {
        this.addErrorMessage({
          key: `error-${ manager.getEntityType() }-load`
        }, error);
        cb(null, error);
      // remove selection for unpresent records
      } else if (json && json._embedded) {
        const { selectedRows } = this.state;
        const newSelectedRows = [];
        let data = null;
        if (json._embedded[manager.getCollectionType()]) {
          data = json._embedded[manager.getCollectionType()];
          data.forEach(entity => {
            if (_.includes(selectedRows, entity.id)) { // TODO: custom identifier - move to manager
              newSelectedRows.push(entity.id);
            }
          });
        }
        this.setState({
          selectedRows: newSelectedRows
        }, () => {
          cb(data, null);
        });
      } else {
        cb(null, null); // no data, no error
      }
    }));
  }

  _handlePagination(page, size) {
    const { uiKey, manager } = this.props;
    this.context.store.dispatch(manager.handlePagination(page, size, uiKey));
  }

  _handleSort(property, order, shiftKey) {
    const { uiKey, manager } = this.props;
    //
    this.context.store.dispatch(manager.handleSort(property, order, uiKey, shiftKey));
  }

  _resolveColumns() {
    const children = [];
    //
    React.Children.forEach(this.props.children, (child) => {
      if (child == null) {
        return;
      }
      invariant(
        // child.type.__TableColumnGroup__ ||
        child.type.__TableColumn__ ||
        child.type.__AdvancedColumn__ || child.type.__AdvancedColumnLink__,
        'child type should be <TableColumn /> or ' +
        'child type should be <AdvancedColumn /> or ' +
        '<AdvancedColumnGroup />'
      );
      children.push(child);
    });
    return children;
  }

  useFilterForm(filterForm) {
    this.useFilterData(Domain.SearchParameters.getFilterData(filterForm));
  }

  useFilterData(formData) {
    this.fetchEntities(this._getSearchParameters(formData));
  }

  /**
   * Returns search parameters filled from given filter form data
   *
   * @param  {object} formData
   * @return {SearchParameters}
   */
  _getSearchParameters(formData) {
    const { _searchParameters } = this.props;
    //
    return Domain.SearchParameters.getSearchParameters(formData, _searchParameters);
  }

  /**
   * Returns search parameters filled from given filter form (referernce)
   *
   * @param  {ref} filterForm
   * @return {SearchParameters}
   */
  getSearchParameters(filterForm) {
    return this._getSearchParameters(Domain.SearchParameters.getFilterData(filterForm));
  }

  cancelFilter(filterForm) {
    const { manager, _searchParameters } = this.props;
    //
    const filterValues = filterForm.getData();
    for (const property in filterValues) {
      if (!filterValues[property]) {
        continue;
      }
      const filterComponent = filterForm.getComponent(property);
      if (!filterComponent) {
        // filter is not rendered
        continue;
      }
      filterComponent.setValue(null);
    }
    // prevent sort and pagination
    let userSearchParameters = _searchParameters.setFilters(manager.getDefaultSearchParameters().getFilters());
    userSearchParameters = userSearchParameters.setPage(0);
    //
    this.fetchEntities(userSearchParameters);
  }

  _onRowSelect(rowIndex, selected, selection) {
    const { onRowSelect } = this.props;
    //
    this.setState({
      selectedRows: selection
    }, () => {
      if (onRowSelect) {
        onRowSelect(rowIndex, selected, selection);
      }
    });
  }

  onBulkAction(actionItem) {
    if (actionItem.action) {
      actionItem.action(actionItem.value, this.state.selectedRows, actionItem);
    } else {
      this.addMessage({ level: 'info', message: this.i18n('bulk-action.notImplemented') });
    }
    return false;
  }

  getNoData(noData) {
    if (noData !== null && noData !== undefined) {
      return noData;
    }
    // default noData
    return this.i18n('noData', { defaultValue: 'No record found' });
  }

  _showId() {
    const { showId, appShowId } = this.props;
    //
    if (showId !== null && showId !== undefined) {
      // table prop => highest priority
      return showId;
    }
    return appShowId;
  }

  _filterOpen(open) {
    const { filterOpen } = this.props;
    let result = true;
    if (filterOpen) {
      result = filterOpen(open);
    }
    //
    if (result !== false) {
      this.setState({
        filterOpened: open
      });
    }
  }

  showBulkActionDetail(backendBulkAction) {
    const { showBulkActionDetail } = this.state;
    //
    if (showBulkActionDetail) { // FIXME: ~close bulk action ...
      this.setState({
        showBulkActionDetail: !showBulkActionDetail
      });
    } else {
      // move filter values int bulk action parameters automatically
      const searchParameters = this._mergeSearchParameters(this.props._searchParameters);
      const values = [];
      searchParameters.getFilters().forEach((filter, property) => {
        if (filter !== null && filter !== undefined) {
          if (_.isArray(filter)) {
            filter.forEach(singleValue => {
              values.push({
                _embedded: {
                  formAttribute: {
                    code: property
                  }
                },
                value: singleValue
              });
            });
          } else if (_.isObject(filter)) {
            // TODO: expand nested properties is not supported
          } else {
            values.push({
              _embedded: {
                formAttribute: {
                  code: property
                }
              },
              value: filter
            });
          }
        }
      });
      //
      this.setState({
        showBulkActionDetail: !showBulkActionDetail,
        backendBulkAction,
        now: moment(new Date()).format(this.i18n('format.datetime')),
        formInstance: new Domain.FormInstance({}, values)
      }, () => {
        // @todo-upgrade-10 This is brutal hack!
        // I had to use the timeout, because Modal doesn't have rendered refs in this phase.
        // This problem occured after update on React 16, but primary bug is in react-bootstap.
        // Problem should be fixed, but still doesn't works (in 0.32.4).
        // https://github.com/react-bootstrap/react-bootstrap/issues/2841#issuecomment-378017284.
        setTimeout(() => {
          this.prevalidateBulkAction(backendBulkAction);
        }, 10);
      });
    }
  }

  showAudit(entity, property, event) {
    if (event) {
      event.preventDefault();
    }
    const propertyValue = property === 'entityId' ? entity.id : entity[property];
    // set search parameters in redux
    const searchParameters = auditManager.getDefaultSearchParameters().setFilter(property, propertyValue);
    // co conctete audit table
    this.context.store.dispatch(auditManager.requestEntities(searchParameters, 'audit-table'));
    // prevent to show loading, when transaction id is the same
    this.context.store.dispatch(dataManager.stopRequest('audit-table'));
    // redirect to audit of entities with prefiled search parameters
    if (this.props.uiKey === 'audit-table') {
      // audit table reloads externally ()
    } else {
      this.context.history.push(`/audit/entities?${ property }=${ propertyValue }`);
    }
  }

  /**
 * Removes prohibited actions.
 */
  _removeProhibitedActions(actions) {
    const { prohibitedActions } = this.props;

    return actions.filter(action => {
      if (!prohibitedActions) {
        return true;
      }
      return prohibitedActions
        .filter(prohibitedAction => action.value === prohibitedAction)
        .length === 0;
    });
  }

  _sortActions(one, two) {
    if (one.order === two.order) {
      return this._getBulkActionLabel(one).localeCompare(this._getBulkActionLabel(two));
    }
    //
    return one.order - two.order;
  }

  _renderPrevalidateMessages(backendBulkAction) {
    if (!backendBulkAction.prevalidateResult) {
      return null;
    }
    if (!backendBulkAction.prevalidateResult._infos) {
      return null;
    }

    const result = [];
    for (const model of backendBulkAction.prevalidateResult._infos) {
      result.push(
        <Basic.FlashMessage showHtmlText message={ this.getFlashManager().convertFromResultModel(model) }/>
      );
    }
    return result;
  }

  _renderBulkActionDetail() {
    const {
      backendBulkAction,
      showBulkActionDetail,
      bulkActionShowLoading,
      selectedRows,
      now,
      removedRows,
      formInstance
    } = this.state;
    const { _total, manager } = this.props;
    const count = _total - removedRows.size;

    const isSelectedAll = _.includes(selectedRows, Basic.Table.SELECT_ALL);
    // get entities for currently selected
    let selectedEntities = [];
    if (!isSelectedAll) {
      selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    }
    //
    // get entitties for currently deselected
    let removedEnties = [];
    if (removedRows.size > 0) {
      removedEnties = manager.getEntitiesByIds(this.props._state, removedRows.toArray());
    }
    let modalContent = null;
    if (backendBulkAction && backendBulkAction.longRunningTaskId) {
      if (SecurityManager.hasAuthority('SCHEDULER_AUTOCOMPLETE')) {
        modalContent = (
          <Basic.Modal.Body style={ {padding: 0, marginBottom: -20} }>
            <LongRunningTask
              entityIdentifier={ backendBulkAction.longRunningTaskId }
              header={ this.i18n(`${backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }.label`)}
              showProperties={ false }
              onComplete={ () => this._afterBulkAction(backendBulkAction) }
              footerButtons={
                <Basic.Button
                  level="link"
                  onClick={ this.showBulkActionDetail.bind(this) }>
                  { this.i18n('button.close') }
                </Basic.Button>
              }/>
          </Basic.Modal.Body>
        );
      } else {
        modalContent = (
          <Basic.Div>
            <Basic.Modal.Header
              icon={ this.i18n(`${ backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }.icon`, { defaultValue: '' }) }
              text={ this.i18n(`${ backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }.label`) }/>
            <Basic.Modal.Body>
              <Basic.Alert
                level="info"
                text={ this.i18n('bulkAction.insufficientReadPermission') }/>
            </Basic.Modal.Body>
            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.showBulkActionDetail.bind(this) }>
                { this.i18n('button.close') }
              </Basic.Button>
            </Basic.Modal.Footer>
          </Basic.Div>
        );
      }
    } else if (backendBulkAction) {
      const helpKey = `${ backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }.help`;
      const help = this.i18n(helpKey);
      //
      modalContent = (
        <form onSubmit={this.processBulkAction.bind(this, backendBulkAction)}>
          <Basic.Modal.Header
            icon={ this.i18n(`${ backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }.icon`, { defaultValue: '' }) }
            text={ this.i18n(`${ backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }.label`) }/>
          <Basic.Modal.Body>
            <Basic.AbstractForm ref="bulkActionForm" showLoading={bulkActionShowLoading}>
              <Basic.Alert
                level="warning"
                text={
                  this.i18n('bulkAction.selectAllRecordsWarning', {
                    count,
                    action: this.i18n(`${backendBulkAction.module}:eav.bulk-action.${backendBulkAction.name}.label`),
                    date: now,
                    escape: false
                  })
                }
                rendered={isSelectedAll} />
              <Basic.Row rendered={ !isSelectedAll } style={{ marginLeft: 0, marginRight: 0, marginBottom: 15 }}>
                {
                  this.i18n(`bulkAction.message${ (selectedRows.length === 0 ? '_empty' : '') }`, {
                    count: selectedRows.length,
                    entities: manager.getNiceLabels(selectedEntities).join(', '),
                    name: this.i18n(`${backendBulkAction.module}:eav.bulk-action.${backendBulkAction.name}.label`),
                    escape: false
                  })
                }
              </Basic.Row>
              <Basic.Row rendered={removedEnties.length > 0} style={ { marginLeft: 0, marginRight: 0, marginBottom: 15 } }>
                {
                  this.i18n('bulkAction.removedRecord', {
                    count: removedEnties.length,
                    entities: manager.getNiceLabels(removedEnties).join(', '),
                    escape: false
                  })
                }
              </Basic.Row>
              <Basic.Alert
                level="info"
                showHtmlText
                text={ help }
                rendered={ (`${backendBulkAction.module}:${help}`) !== helpKey } />

              { this._renderPrevalidateMessages(backendBulkAction) }

              <EavAttributeForm
                ref="bulkActionAttributes"
                localizationKey={ backendBulkAction.name }
                localizationModule={ backendBulkAction.module }
                formAttributes={ backendBulkAction.formAttributes }
                formInstance={ formInstance }
                localizationType="bulk-action"/>
            </Basic.AbstractForm>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button
              level="link"
              onClick={ this.showBulkActionDetail.bind(this) }>
              { this.i18n('button.close') }
            </Basic.Button>
            <Basic.Button
              type="submit"
              level={ backendBulkAction.level ? backendBulkAction.level.toLowerCase() : 'success' }
              showLoading={ bulkActionShowLoading }
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }>
              { this.i18n('bulkAction.button.execute') }
            </Basic.Button>
          </Basic.Modal.Footer>
        </form>
      );
    }

    return (
      <Basic.Modal
        show={ showBulkActionDetail }
        onHide={ this.showBulkActionDetail.bind(this) }
        backdrop="static">
        { modalContent }
      </Basic.Modal>
    );
  }

  render() {
    const {
      _entities,
      _total,
      _showLoading,
      _error,
      _quickButtonCount,
      _menuIncluded,
      manager,
      pagination,
      onRowClick,
      onRowDoubleClick,
      rowClass,
      rendered,
      filter,
      filterCollapsible,
      filterViewportOffsetTop,
      buttons,
      noData,
      style,
      showRowSelection,
      showLoading,
      showFilter,
      showPageSize,
      showToolbar,
      showRefreshButton,
      showAuditLink,
      showTransactionId,
      condensed,
      header,
      forceSearchParameters,
      className,
      uuidEnd,
      hover,
      sizeOptions,
      quickButtonCount
    } = this.props;
    const {
      filterOpened,
      selectedRows,
      removedRows,
      _actions,
      hideTableShowLoading
    } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    const columns = this._resolveColumns();
    let range = null;
    const _searchParameters = this._mergeSearchParameters(this.props._searchParameters);
    if (_searchParameters) {
      range = {
        page: _searchParameters.getPage(),
        size: _searchParameters.getSize()
      };
    }
    const renderedColumns = [];
    for (let i = 0; i < columns.length; i++) {
      const column = columns[i];
      // basic column support
      if (column.type.__TableColumn__) {
        // add cloned elemet with data provided
        renderedColumns.push(column);
        continue;
      }
      // common props to all column faces
      const commonProps = {
        // title: column.props.title,
        className: `column-face-${column.props.face}`
      };
      // construct basic column from advanced column definition
      let columnHeader = column.props.header;
      let columnTitle = column.props.title;
      if (column.props.property) {
        if (!columnHeader) {
          columnHeader = this.i18n(
            `${manager.getModule()}:entity.${manager.getEntityType()}.${column.props.property}.label`, // label has higher priority
            { defaultValue: this.i18n(`${manager.getModule()}:entity.${manager.getEntityType()}.${column.props.property}`)}
          );
        }
        if (!columnTitle) {
          columnTitle = this.i18n(`${manager.getModule()}:entity.${manager.getEntityType()}.${column.props.property}.title`, { defaultValue: '' });
        }
      }
      if (column.props.sort) {
        columnHeader = (
          <Basic.BasicTable.SortHeaderCell
            header={ columnHeader }
            sortHandler={ this._handleSort.bind(this) }
            sortProperty={ column.props.sortProperty || column.props.property }
            searchParameters={ _searchParameters }
            className={ commonProps.className }
            title={ columnTitle }/>
        );
      } else if (columnTitle) {
        columnHeader = (
          <span title={ columnTitle }>
            { columnHeader }
          </span>
        );
      }
      //
      const key = `column_${ i }`;
      let cell = null;
      if (column.props.cell) {
        cell = column.props.cell;
      } else if (column.type.__AdvancedColumnLink__) {
        cell = (
          <Basic.BasicTable.LinkCell to={ column.props.to } target={ column.props.target } access={ column.props.access } {...commonProps}/>
        );
      } else {
        switch (column.props.face) {
          case 'text': {
            cell = (
              <Basic.BasicTable.TextCell { ...commonProps } maxLength={ column.props.maxLength }/>
            );
            break;
          }
          case 'date': {
            cell = (
              <Basic.BasicTable.DateCell format={ this.i18n('format.date') } { ...commonProps }/>
            );
            break;
          }
          case 'datetime': {
            cell = (
              <Basic.BasicTable.DateCell format={ this.i18n('format.datetime') } { ...commonProps }/>
            );
            break;
          }
          case 'bool':
          case 'boolean': {
            cell = (
              <Basic.BasicTable.BooleanCell { ...commonProps }/>
            );
            break;
          }
          case 'enum': {
            cell = (
              <Basic.BasicTable.EnumCell enumClass={column.props.enumClass} {...commonProps}/>
            );
            break;
          }
          default: {
            this.getLogger().trace(`[AdvancedTable] usind default for column face [${ column.props.face }]`);
          }
        }
      }
      // add target column with cell by data type
      renderedColumns.push(
        <Basic.BasicTable.Column
          key={ key }
          property={ column.props.property }
          rendered={ column.props.rendered }
          className={ column.props.className }
          width={ column.props.width }
          header={ columnHeader }
          title={ columnTitle }
          cell={ cell }/>
      );
    }
    //
    let _rowClass = rowClass;
    if (!_rowClass) {
      // automatic rowClass by entity's "disabled" attribute
      _rowClass = ({rowIndex, data}) => { return Utils.Ui.getDisabledRowClass(data[rowIndex]); };
    }
    // If is manager in the universal request mode, then row class by requests will be used (REMOVE / ADD / CHANGE).
    if (manager.isRequestModeEnabled && manager.isRequestModeEnabled()) {
      _rowClass = ({rowIndex, data}) => { return Utils.Ui.getRequestRowClass(data[rowIndex]); };
    }
    //
    let count = 0;
    if (_.includes(selectedRows, Basic.Table.SELECT_ALL)) {
      count = _total - removedRows.size;
    } else {
      count = selectedRows.length;
    }
    //
    let _actionsWithSelection = _actions
      .filter(action => { return action.showWithSelection; });
    // Remove prohibited actions
    _actionsWithSelection = this._removeProhibitedActions(_actionsWithSelection);

    let _actionsWithoutSelection = _actions
      .filter(action => { return action.showWithoutSelection; });
    // Remove prohibited actions
    _actionsWithoutSelection = this._removeProhibitedActions(_actionsWithoutSelection);

    let _actionClassName;
    if (selectedRows.length <= 0) {
      _actionClassName = _actionsWithoutSelection.length === 0 ? 'hidden' : 'bulk-action';
    } else {
      _actionClassName = _actionsWithSelection.length === 0 ? 'hidden' : 'bulk-action';
    }
    //
    const _isLoading = (_showLoading || showLoading) && !hideTableShowLoading;
    //
    // resolve bulk actions
    let processActions = selectedRows.length <= 0 ? _actionsWithoutSelection : _actionsWithSelection;
    // quick action buttons - configured
    let buttonActions = processActions.filter(a => a.quickButton);
    processActions = processActions.filter(a => !a.quickButton);
    // delete actions has own menu (at end)
    let deleteActions = processActions.filter(a => a.deleteAction);
    processActions = processActions.filter(a => !a.deleteAction);
    // normal menu actions
    let menuActions = [];
    //
    // add quick buttons - by order
    let buttonActionCount = DEFAULT_QUICK_BUTTON_COUNT;
    if (!Utils.Ui.isEmpty(quickButtonCount)) {
      buttonActionCount = quickButtonCount;
    } else if (!Utils.Ui.isEmpty(_quickButtonCount)) {
      buttonActionCount = parseInt(_quickButtonCount, 10);
    }
    for (let i = 0; i < processActions.length; i++) {
      const action = processActions[i];
      if (action.icon === null || action.icon === undefined) {
        menuActions.push(action);
        // icon is required to quick access button
        continue;
      }
      // add quick button
      if (buttonActions.length >= buttonActionCount) {
        menuActions.push(action);
      } else {
        buttonActions.push(action);
      }
    }
    if (buttonActions.length < buttonActionCount) {
      const newDeleteActions = [];
      // try to add delete button to quick ...
      for (let i = 0; i < deleteActions.length; i++) {
        const action = deleteActions[i];
        if (action.icon === null || action.icon === undefined) {
          newDeleteActions.push(action);
          // icon is required to quick access button
          continue;
        }
        //
        if (buttonActions.length >= buttonActionCount) {
          newDeleteActions.push(action);
        } else {
          buttonActions.push(action);
        }
      }
      deleteActions = newDeleteActions;
    }
    // sort buttons by order and locale
    buttonActions = buttonActions.sort(this._sortActions.bind(this));
    menuActions = menuActions.sort(this._sortActions.bind(this));
    deleteActions = deleteActions.sort(this._sortActions.bind(this));
    // new quick buttons will be rendered
    const showBulkActionSelect = buttonActionCount === 0 || buttonActions.length === 0;
    // included buttons action in menu
    let buttonMenuIncludedActions = null;
    if (_menuIncluded && buttonActions.length > 0) {
      buttonMenuIncludedActions = buttonActions.map(action => (
        <MenuItem
          title={ this.isDevelopment() ? `Action order: ${ action.order }, Action key: ${ action.actionKey }` : null }
          onClick={ this.onBulkAction.bind(this, action) }>
          <Basic.Icon icon={ action.icon } level={ action.level }/>
          { action.label || action.niceLabel }
        </MenuItem>
      ));
      if (menuActions.length > 0 || deleteActions.length > 0) {
        buttonMenuIncludedActions.push(<MenuItem divider />);
        buttonMenuIncludedActions.push(
          <MenuItem header>
            { this.i18n(`bulkAction.button.next`) }
          </MenuItem>
        );
        buttonMenuIncludedActions.push(<MenuItem divider />);
      }
    }
    //
    return (
      <Basic.Div
        className={
          classnames(
            'advanced-table',
            { 'bulk-action-supported': manager.supportsBulkAction() },
            { 'quick-button-supported': !showBulkActionSelect },
            className
          )
        }
        style={ style }>
        {
          (!filter && (_actions.length === 0 || !showRowSelection) && (buttons === null || buttons.length === 0))
          ||
          <Basic.Toolbar
            container={ this }
            viewportOffsetTop={ filterViewportOffsetTop }
            rendered={ showToolbar }
            className="collapse-top">
            <Basic.Collapse in={ filterOpened } rendered={ showFilter }>
              <Basic.Div showLoading={ _isLoading } showAnimation={ false }>
                { filter }
              </Basic.Div>
            </Basic.Collapse>
            <Basic.Div className="advanced-table-heading">
              <Basic.Div className="bulk-action-container">
                {
                  showBulkActionSelect
                  ?
                  <Basic.EnumSelectBox
                    onChange={ this.onBulkAction.bind(this) }
                    ref="bulkActionSelect"
                    componentSpan=""
                    className={ _actionClassName }
                    multiSelect={ false }
                    options={ selectedRows.length <= 0 ? _actionsWithoutSelection : _actionsWithSelection }
                    placeholder={ this.i18n(`bulk-action.selection${ selectedRows.length === 0 ? '_empty' : '' }`, { count }) }
                    rendered={ _actions.length > 0 && showRowSelection }
                    searchable={ false }
                    emptyOptionLabel={ false }/>
                  :
                  <Basic.Div
                    className={ _actionClassName }
                    rendered={ _actions.length > 0 && showRowSelection }>
                    {
                      buttonActions.map(action => {
                        return (
                          <Basic.Button
                            buttonSize="xs"
                            className="bulk-action-button"
                            title={
                              this.isDevelopment()
                              ?
                              `${ action.label || action.niceLabel } - Action order: ${ action.order }, Action key: ${ action.actionKey }`
                              :
                              (action.label || action.niceLabel)
                            }
                            titlePlacement="bottom"
                            onClick={ this.onBulkAction.bind(this, action) }>
                            <Basic.Icon icon={ action.icon } level={ action.level }/>
                          </Basic.Button>
                        );
                      })
                    }
                    {
                      (menuActions.length === 0 && deleteActions.length === 0 && (!_menuIncluded || buttonActions.length === 0))
                      ||
                      <Dropdown>
                        <Basic.Icon
                          className="dropdown-toogle-icon"
                          bsRole="toggle"
                          value="fa:ellipsis-v"
                          title={ _menuIncluded ? this.i18n(`bulkAction.button.all`) : this.i18n(`bulkAction.button.next`) }/>

                        <Dropdown.Menu>
                          <MenuItem header>
                            { this.i18n(`bulk-action.selection${ selectedRows.length === 0 ? '_empty' : '' }`, { count }) }
                          </MenuItem>
                          <MenuItem divider />
                          {
                            buttonMenuIncludedActions
                          }
                          {
                            menuActions.map(action => {
                              return (
                                <MenuItem
                                  title={ this.isDevelopment() ? `Action order: ${ action.order }, Action key: ${ action.actionKey }` : null }
                                  onClick={ this.onBulkAction.bind(this, action) }>
                                  <Basic.Icon icon={ action.icon } level={ action.level }/>
                                  { action.label || action.niceLabel }
                                </MenuItem>
                              );
                            })
                          }
                          {
                            deleteActions.length === 0 || menuActions.length === 0
                            ||
                            <MenuItem divider />
                          }
                          {
                            deleteActions.map(action => {
                              return (
                                <MenuItem
                                  title={ this.isDevelopment() ? `Action order: ${ action.order }` : null }
                                  onClick={ this.onBulkAction.bind(this, action) }>
                                  <Basic.Icon icon={ action.icon } level={ action.level }/>
                                  { action.label || action.niceLabel }
                                </MenuItem>
                              );
                            })
                          }
                        </Dropdown.Menu>
                      </Dropdown>
                    }
                  </Basic.Div>
                }
              </Basic.Div>
              <Basic.Div className="button-container">
                { buttons }

                <Filter.ToogleButton
                  filterOpen={ this._filterOpen.bind(this) }
                  filterOpened={ filterOpened }
                  rendered={ showFilter && filter !== undefined && filterCollapsible }
                  style={{ marginLeft: 3 }}
                  searchParameters={ _searchParameters }
                  forceSearchParameters={ forceSearchParameters }/>

                <RefreshButton
                  onClick={ this.fetchEntities.bind(this, _searchParameters, this.props) }
                  title={ this.i18n('button.refresh') }
                  showLoading={ _showLoading }
                  rendered={ showRefreshButton }/>
              </Basic.Div>
            </Basic.Div>
          </Basic.Toolbar>
        }
        {
          _error
          ?
          <Basic.Alert level="warning">
            <div style={{ textAlign: 'center', display: 'none'}}>
              <Basic.Icon value="fa:warning"/>
              {' '}
              { this.i18n('error.load') }
            </div>
            <div style={{ textAlign: 'center' }}>
              <Basic.Button onClick={ this.reload.bind(this, this.props) }>
                <Basic.Icon value="fa:refresh"/>
                { ' ' }
                { this.i18n('button.refresh') }
              </Basic.Button>
            </div>
          </Basic.Alert>
          :
          <Basic.Div>
            <Basic.BasicTable.Table
              ref="table"
              header={ header }
              data={ _entities }
              hover={ hover }
              showLoading={ _isLoading }
              onRowClick={ onRowClick }
              onRowDoubleClick={ onRowDoubleClick }
              showRowSelection={ _actions.length > 0 && showRowSelection }
              selectedRows={ selectedRows }
              onRowSelect={ this._onRowSelect.bind(this) }
              rowClass={ _rowClass }
              condensed={ condensed }
              noData={ this.getNoData(noData) }
              selectRowCb={ manager.supportsBulkAction() ? this.selectRowForBulkAction.bind(this) : null }
              isRowSelectedCb={ manager.supportsBulkAction() ? this.isRowSelected.bind(this) : null }
              isAllRowsSelectedCb={ manager.supportsBulkAction() ? this.isAllRowSelected.bind(this) : null }>

              { renderedColumns }

              <Basic.Column
                header={ this.i18n('entity.id.label') }
                property="id"
                rendered={ this._showId() }
                className="text-center"
                width={ 115 }
                cell={
                  ({rowIndex, data, property}) => {
                    const entity = data[rowIndex];
                    const identifier = entity[property];
                    const transactionId = entity.transactionId;
                    const _showTransactionId = transactionId && showTransactionId;
                    const content = [];
                    //
                    content.push(
                      <Basic.Div>
                        {
                          !_showTransactionId
                          ||
                          <span title={ this.i18n('entity.id.help') }>
                            { this.i18n('entity.id.short') }:
                          </span>
                        }
                        <UuidInfo
                          header={ this.i18n('entity.id.help') }
                          value={ identifier }
                          uuidEnd={ uuidEnd }
                          placement="left"
                          buttons={
                            SecurityManager.hasAuthority('AUDIT_READ')
                              && this.props.uiKey !== 'audit-table'
                              && showAuditLink
                            ?
                            [
                              <a
                                href="#"
                                onClick={ this.showAudit.bind(this, entity, 'entityId') }
                                title={ this.i18n('button.entityId.title') }>
                                <Basic.Icon icon="component:audit"/>
                                {' '}
                                { this.i18n('button.entityId.label') }
                              </a>
                            ]
                            :
                            null
                          }/>
                      </Basic.Div>
                    );
                    if (_showTransactionId) {
                      content.push(
                        <Basic.Div>
                          <span title={ this.i18n('entity.transactionId.help') }>
                            { this.i18n('entity.transactionId.short') }
                            :
                          </span>
                          <UuidInfo
                            value={ transactionId }
                            uuidEnd={ uuidEnd }
                            header={ this.i18n('entity.transactionId.label') }
                            placement="left"
                            buttons={
                              SecurityManager.hasAuthority('AUDIT_READ')
                                && showAuditLink
                              ?
                              [
                                <a
                                  href="#"
                                  onClick={ this.showAudit.bind(this, entity, 'transactionId') }
                                  title={ this.i18n('button.transactionId.title') }>
                                  <Basic.Icon icon="component:audit"/>
                                  {' '}
                                  { this.i18n('button.transactionId.label') }
                                </a>
                              ]
                              :
                              null
                            }/>
                        </Basic.Div>
                      );
                    }
                    //
                    return content;
                  }
                }/>
            </Basic.BasicTable.Table>
            <Basic.BasicTable.Pagination
              ref="pagination"
              showPageSize={ showPageSize }
              paginationHandler={ pagination ? this._handlePagination.bind(this) : null }
              total={ pagination ? _total : _entities.length }
              sizeOptions={ sizeOptions }
              { ...range } />
          </Basic.Div>
        }
        { this._renderBulkActionDetail() }
      </Basic.Div>
    );
  }
}

AdvancedTable.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * Table identifier - it's used as key in store
   */
  uiKey: PropTypes.string,
  /**
   * Table Header
   */
  header: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * EntityManager subclass, which provides data fetching
   */
  manager: PropTypes.object.isRequired,
  /**
   * If pagination is shown
   */
  pagination: PropTypes.bool, // enable paginator action
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * "Default filters"
   */
  defaultSearchParameters: PropTypes.object,
  /**
   * Callback that is called when a row is clicked.
   */
  onRowClick: PropTypes.func,
  /**
   * Callback that is called when a row is double clicked.
   */
  onRowDoubleClick: PropTypes.func,
  /**
   * Callback that is called when a row is selected.
   */
  onRowSelect: PropTypes.func,
  /**
   * Callback that is called table is refreshed (after refresh button is clicked and data are refreshed).
   *
   * @since 10.7.0
   */
  onReload: PropTypes.func,
  /**
   * Enable row selection - checkbox in first cell
   */
  showRowSelection: PropTypes.oneOfType([PropTypes.bool, PropTypes.func]),
  /**
   * Shows column with id. Default is id shown in Development stage.
   */
  showId: PropTypes.bool,
  /**
   * selected row indexes as immutable set
   */
  selectedRows: PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.string, PropTypes.number])),
  /**
   * css added to row
   */
  rowClass: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func
  ]),
  /**
   * Filter definition
   * @type {Filter}
   */
  filter: PropTypes.element,
  /**
   * Show filter
   */
  showFilter: PropTypes.bool,
  /**
   * If filter is opened by default
   */
  filterOpened: PropTypes.bool,
  /**
   * External filter open function. If false is returned, internal filterOpened is not set.
   */
  filterOpen: PropTypes.func,
  /**
   * If filter can be collapsed
   */
  filterCollapsible: PropTypes.bool,
  /**
   * When affixed, pixels to offset from top of viewport
   */
  filterViewportOffsetTop: PropTypes.number,
  /**
   * Bulk actions e.g. { value: 'activate', niceLabel: this.i18n('content.identities.action.activate.action'), action: this.onActivate.bind(this) }
   * This prop is ignored, if backend actions are used (see manager.supportsBulkAction())
   */
  actions: PropTypes.arrayOf(PropTypes.object),
  /**
   * Buttons are shown on the right of toogle filter button
   */
  buttons: PropTypes.arrayOf(PropTypes.element),
  /**
   * If table data is empty, then this text will be shown
   */
  noData: PropTypes.oneOfType([PropTypes.string, PropTypes.element]),
  /**
   * Shows page size
   */
  showPageSize: PropTypes.bool,
  /**
   * Shows toolbar.
   */
  showToolbar: PropTypes.bool,
  /**
   * Shows refresh button.
   */
  showRefreshButton: PropTypes.bool,
  /**
   * Shows links to audit
   */
  showAuditLink: PropTypes.bool,
  /**
   * Table css
   */
  className: PropTypes.string,
  /**
   * Table styles
   */
  style: PropTypes.object,
  /**
   * Shows ending uuid characters in shorten label.
   */
  uuidEnd: PropTypes.bool,
  /**
   * Data are loaded automatically, after component is mounted. Set to false, if initial load will be controlled programatically.
   */
  initialReload: PropTypes.bool,
  /**
   * Enable hover table class
   */
  hover: PropTypes.bool,
  /**
   * Prohibited actions. Defines array an keys of a bulk actions, that shouldn't be visible in this table.
   */
  prohibitedActions: PropTypes.arrayOf(PropTypes.string),
  /**
   * Callback after bulk action ends - called only if LRT detail is shown till end. Return 'false' in your callback, when standard table reload is not needed after end.
   */
  afterBulkAction: PropTypes.func,
  /**
   * Count of quick access buttons for bulk actions in tables - the first count of bulk actions will be shown as button - next action will be rendered in drop down select box.
   * Bulk action icon is required for quick access button - action without icon will be rendered in select box.
   * Bulk action can enforce showing in quick access button (by bulk action configuration).
   *
   * @since 10.6.0
   */
  quickButtonCount: PropTypes.number,

  //
  // Private properties, which are used internally for async data fetching
  //

  /**
   * loadinig indicator
   */
  _showLoading: PropTypes.bool,
  _entities: PropTypes.arrayOf(PropTypes.object),
  _total: PropTypes.number,
  /**
   * Persisted / used search parameters in redux
   */
  _searchParameters: PropTypes.object,
  _backendBulkActions: PropTypes.arrayOf(PropTypes.object),
};
AdvancedTable.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  _showLoading: true,
  _entities: [],
  _total: null,
  _searchParameters: null,
  _error: null,
  _backendBulkActions: null,
  _quickButtonCount: null,
  pagination: true,
  showRowSelection: false,
  showFilter: true,
  showId: null,
  selectedRows: [],
  filterCollapsible: true,
  actions: [],
  buttons: [],
  showPageSize: true,
  showToolbar: true,
  showRefreshButton: true,
  showAuditLink: true,
  uuidEnd: false,
  initialReload: true,
  hover: true,
  prohibitedActions: [],
  quickButtonCount: null
};

const makeMapStateToProps = () => {
  const selectEntitiesSelector = selectEntities();
  const mapStateToProps = function select(state, component) {
    const uiKey = component.manager.resolveUiKey(component.uiKey);
    const ui = state.data.ui[uiKey];
    const result = {
      i18nReady: state.config.get('i18nReady'),
      appShowId: ConfigurationManager.showId(state),
      showTransactionId: ConfigurationManager.showTransactionId(state),
      defaultPageSize: ConfigurationManager.getDefaultPageSize(state),
      sizeOptions: ConfigurationManager.getSizeOptions(state),
      _quickButtonCount: ConfigurationManager.getPublicValue(state, 'idm.pub.app.show.table.quickButton.count'),
      _menuIncluded: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.show.table.quickButton.menuIncluded', false)
    };
    //
    if (!ui) {
      return result;
    }
    return {
      ...result,
      _showLoading: ui.showLoading,
      _entities: selectEntitiesSelector(state, component),
      _total: ui.total,
      _searchParameters: ui.searchParameters,
      _error: ui.error,
      _backendBulkActions: component.manager.supportsBulkAction() ? DataManager.getData(state, component.manager.getUiKeyForBulkActions()) : null
    };
  };
  return mapStateToProps;
};

export default connect(makeMapStateToProps, null, null, { forwardRef: true})(AdvancedTable);
