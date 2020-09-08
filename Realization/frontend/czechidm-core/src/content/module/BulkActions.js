import React from 'react';
import Helmet from 'react-helmet';
import Immutable from 'immutable';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { DataManager, BulkActionManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import NotificationLevelEnum from '../../enums/NotificationLevelEnum';

const UIKEY = 'bulk-actions';
const manager = new BulkActionManager();

/**
 * Registered bulk actions.
 *
 * @author Radek Tomiška
 * @since 10.6.0
 */
class BulkActions extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true
    };
  }

  getContentKey() {
    return `content.system.${UIKEY}`;
  }

  getNavigationKey() {
    return UIKEY;
  }

  getManager() {
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    this.reload();
    this.context.store.dispatch(manager.fetchRegisteredBulkActions());
  }

  reload() {
    const {_searchParameters} = this.props;
    //
    this.fetchBulkActions(_searchParameters);
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchBulkActions(
      SearchParameters.getSearchParameters(
        SearchParameters.getFilterData(this.refs.filterForm),
        this.props._searchParameters
      )
    );
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.fetchBulkActions(null);
    this.refs.filterForm.setData({});
  }

  fetchBulkActions(searchParameters) {
    this.context.store.dispatch(this.getManager().fetchEntities(searchParameters, UIKEY));
  }

  onEnable(entity, enable, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-${enable ? '' : 'de'}activate`].show(
      this.i18n(`action.${enable ? '' : 'de'}activate.message`, { count: 1, record: entity.name }),
      this.i18n(`action.${enable ? '' : 'de'}activate.header`, { count: 1 })
    ).then(() => {
      this.context.store.dispatch(this.getManager().setEnabled(entity.id, enable, (patchedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n(`action.${enable ? '' : 'de'}activate.success`, { count: 1, record: entity.name }) });
          // refresh table with processors
          this.reload();
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // rejected
    });
  }

  render() {
    const { bulkActions, showLoading, registeredBulkActions, _searchParameters } = this.props;
    const { filterOpened } = this.state;
    let _bulkActions = new Immutable.OrderedMap();
    let _entityClasses = new Immutable.OrderedSet();
    let _registeredEntityClasses = new Immutable.OrderedSet();
    if (bulkActions) {
      bulkActions.forEach(bulkAction => {
        if (!_bulkActions.has(bulkAction.entityClass)) {
          _bulkActions = _bulkActions.set(bulkAction.entityClass, []);
        }
        const actions = _bulkActions.get(bulkAction.entityClass);
        actions.push(bulkAction);
        _bulkActions = _bulkActions.set(bulkAction.entityClass, actions);
        _entityClasses = _entityClasses.add(bulkAction.entityClass);
      });
    }
    if (registeredBulkActions) {
      registeredBulkActions.forEach(bulkAction => {
        _registeredEntityClasses = _registeredEntityClasses.add(bulkAction.entityClass);
      });
      // sort _entityTypes
      _registeredEntityClasses = _registeredEntityClasses.sort((one, two) => one > two);
    }
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') }/>
        <Basic.Confirm ref="confirm-deactivate" level="warning"/>
        <Basic.Confirm ref="confirm-activate" level="success"/>

        <Basic.Toolbar className="collapse-top">
          <Basic.Collapse in={ filterOpened }>
            <Basic.Div>
              <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
                <Basic.AbstractForm ref="filterForm">
                  <Basic.Row>
                    <Basic.Col lg={ 8 }>
                      <Advanced.Filter.TextField
                        ref="text"
                        placeholder={ this.i18n('filter.text.placeholder') }/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 } className="text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                    </Basic.Col>
                  </Basic.Row>
                  <Basic.Row className="last">
                    <Basic.Col lg={ 4 }>
                      <Advanced.Filter.TextField
                        ref="module"
                        placeholder={this.i18n('filter.module.placeholder')}/>
                    </Basic.Col>
                    <Basic.Col lg={ 4 }>
                      <Advanced.Filter.EnumSelectBox
                        ref="entityClass"
                        placeholder={ this.i18n('filter.entityClass.placeholder') }
                        options={ _registeredEntityClasses.toArray().map(value => ({ value, niceLabel: Utils.Ui.getSimpleJavaType(value) })) }
                        searchable/>
                    </Basic.Col>
                  </Basic.Row>
                </Basic.AbstractForm>
              </Advanced.Filter>
            </Basic.Div>
          </Basic.Collapse>
          <Basic.Div>
            <Basic.Div className="pull-right">
              <Advanced.Filter.ToogleButton
                filterOpen={ (open) => this.setState({filterOpened: open}) }
                filterOpened={ filterOpened }
                style={{ marginLeft: 3 }}
                searchParameters={ _searchParameters }/>
              <Advanced.RefreshButton
                onClick={ this.fetchBulkActions.bind(this, _searchParameters) }
                title={ this.i18n('button.refresh') }
                showLoading={ showLoading }/>
            </Basic.Div>
            <Basic.Div className="clearfix"/>
          </Basic.Div>

        </Basic.Toolbar>
        <Basic.Loading isStatic show={ showLoading }/>
        <Basic.Alert
          level="info"
          text={ this.i18n('component.basic.Table.noData') }
          style={{ margin: 15 }}
          rendered={ !showLoading && _bulkActions.size === 0 } />
        {
          showLoading || _bulkActions.size === 0
          ||
          <Basic.Div>
            {
              [..._entityClasses.map((entityClass) => (
                <Basic.Div className="tab-pane-table-body" style={{ marginBottom: 15 }}>
                  <Basic.ContentHeader>
                    {
                      entityClass
                      ?
                      Utils.Ui.getSimpleJavaType(entityClass)
                      :
                      this.i18n('emptyEntityClass.header', { escape: false })
                    }
                  </Basic.ContentHeader>
                  <Basic.Table
                    data={ _bulkActions.get(entityClass) }
                    showLoading={ showLoading }
                    noData={ this.i18n('component.basic.Table.noData') }
                    rowClass={ ({rowIndex, data}) => Utils.Ui.getRowClass(data[rowIndex]) }>
                    <Basic.Column
                      property="module"
                      header={ this.i18n('entity.BulkAction.module.label') }
                      width={ 100 }/>
                    <Basic.Column
                      property="name"
                      width="20%"
                      header={
                        <span title={ this.i18n('entity.BulkAction.name.title') }>
                          { this.i18n('entity.BulkAction.name.label') }
                        </span>
                      }/>
                    <Basic.Column
                      property="description"
                      header={ this.i18n('entity.BulkAction.description.label') }/>
                    <Basic.Column
                      property="level"
                      header={ this.i18n('entity.BulkAction.level.label') }
                      width={ 100 }
                      cell={ <Basic.EnumCell className="column-face-enum" enumClass={ NotificationLevelEnum }/> }/>
                    <Basic.Column
                      property="icon"
                      header={
                        <Basic.Cell title={ this.i18n('entity.BulkAction.icon.title') }>
                          { this.i18n('entity.BulkAction.icon.label') }
                        </Basic.Cell>
                      }
                      className="center"
                      width={ 100 }
                      cell={
                        ({rowIndex, data, property}) => {
                          const action = data[rowIndex];
                          const icon = (
                            <Basic.Icon
                              icon={ action[property] }
                              level={ action.level !== 'SUCCESS' ? action.level.toLowerCase() : 'default' }/>
                          );
                          if (!icon) {
                            return null;
                          }
                          if (action.quickButton) {
                            return (
                              <Basic.Button
                                buttonSize="xs"
                                className="bulk-action-button"
                                style={{ marginRight: 0 }}
                                title={ this.i18n('entity.BulkAction.quickButton.help') }>
                                { icon }
                              </Basic.Button>
                            );
                          }
                          //
                          return icon;
                        }
                      }/>
                    <Basic.Column
                      property="quickButton"
                      header={
                        <Basic.Cell
                          className="column-face-bool"
                          title={ this.i18n('entity.BulkAction.quickButton.title') }>
                          { this.i18n('entity.BulkAction.quickButton.label') }
                        </Basic.Cell>
                      }
                      cell={ <Basic.BooleanCell className="column-face-bool"/> }
                      width={ 100 }/>
                    <Basic.Column
                      property="deleteAction"
                      header={
                        <Basic.Cell
                          className="column-face-bool"
                          title={ this.i18n('entity.BulkAction.deleteAction.title') }>
                          { this.i18n('entity.BulkAction.deleteAction.label') }
                        </Basic.Cell>
                      }
                      cell={ <Basic.BooleanCell className="column-face-bool"/> }
                      width={ 100 }/>
                    <Basic.Column
                      property="order"
                      header={ this.i18n('entity.BulkAction.order.label') }
                      width={ 100 }/>
                    <Basic.Column
                      property="disabled"
                      header={ <Basic.Cell className="column-face-bool">{ this.i18n('entity.BulkAction.disabled.label') }</Basic.Cell> }
                      cell={ <Basic.BooleanCell className="column-face-bool"/> }
                      width={ 100 }/>
                    <Basic.Column
                      header={ this.i18n('entity.id.label') }
                      property="id"
                      rendered={ this.isDevelopment() }
                      className="text-center"
                      width={ 100 }
                      cell={
                        ({rowIndex, data, property}) => (
                          <Advanced.UuidInfo value={ data[rowIndex][property] }/>
                        )
                      }/>
                    <Basic.Column
                      header={this.i18n('label.action')}
                      className="action"
                      cell={
                        ({ rowIndex, data }) => {
                          if (!data[rowIndex].disabled) {
                            return (
                              <Basic.Button
                                level="warning"
                                onClick={ this.onEnable.bind(this, data[rowIndex], false) }
                                className="btn-xs"
                                title={ this.i18n('button.deactivate') }
                                titlePlacement="bottom"
                                rendered={ data[rowIndex].disableable }>
                                { this.i18n('button.deactivate') }
                              </Basic.Button>
                            );
                          }
                          return (
                            <Basic.Button
                              level="success"
                              onClick={ this.onEnable.bind(this, data[rowIndex], true) }
                              className="btn-xs"
                              title={ this.i18n('button.activate') }
                              titlePlacement="bottom">
                              { this.i18n('button.activate') }
                            </Basic.Button>
                          );
                        }
                      }/>
                  </Basic.Table>
                </Basic.Div>
              )).values()]
            }
            <Basic.Pagination total={ bulkActions.length } />
          </Basic.Div>
        }
      </Basic.Div>
    );
  }
}

BulkActions.propTypes = {
  userContext: PropTypes.object,
  registeredBulkActions: PropTypes.object, // immutable
  bulkActions: PropTypes.arrayOf(PropTypes.object),
  showLoading: PropTypes.bool
};

BulkActions.defaultProps = {
  userContext: null,
  showLoading: true
};

function select(state) {
  return {
    registeredBulkActions: DataManager.getData(state, BulkActionManager.UI_KEY_BULK_ACTIONS),
    bulkActions: manager.getEntities(state, UIKEY),
    showLoading: Utils.Ui.isShowLoading(state, UIKEY)
            || Utils.Ui.isShowLoading(state, BulkActionManager.UI_KEY_BULK_ACTIONS),
    _searchParameters: Utils.Ui.getSearchParameters(state, UIKEY)
  };
}

export default connect(select)(BulkActions);
