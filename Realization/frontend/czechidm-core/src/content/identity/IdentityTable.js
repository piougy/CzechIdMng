import React, { PropTypes } from 'react';
import uuid from 'uuid';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { DataManager, TreeNodeManager, SecurityManager, ConfigurationManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
// TODO: LocalizationService.getCurrentLanguage()
import filterHelp from '../../components/advanced/Filter/README_cs.md';

/**
* Table of users
*
* @author Radek Tomiška
*/
export class IdentityTable extends Basic.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      text: null,
      treeNodeId: props.treeNodeId
    };
    this.dataManager = new DataManager();
    this.treeNodeManager = new TreeNodeManager();
  }

  componentDidMount() {
  }

  componentDidUpdate() {
  }

  getManager() {
    return this.props.identityManager;
  }

  setTreeNodeId(treeNodeId) {
    this.setState({
      treeNodeId
    });
  }

  /**
   * Filter identities by given tree node id
   *
   * @param  {string} treeNodeId
   */
  filterByTreeNodeId(treeNodeId) {
    this.setState({
      treeNodeId
    }, () => {
      this.useFilter();
    });
  }

  _changeText(event) {
    this.setState({
      text: event.currentTarget.value
    });
  }

  _changeTreeNode(entity, event) {
    if (event) {
      event.preventDefault();
    }
    this.setTreeNodeId(entity ? entity.id : null);
  }

  /**
  * Redirect to user form
  */
  showDetail(entity) {
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/identity/new?id=${uuidId}`);
    } else {
      this.context.router.push(`/identity/${entity.username}/profile`);
    }
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({
      text: null,
      treeNodeId: null
    }, () => {
      this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
    });
  }

  onActivate(bulkActionValue, usernames) {
    const { identityManager } = this.props;
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`content.identities.action.${bulkActionValue}.message`, { count: usernames.length, username: identityManager.getEntity(this.context.store.getState(), usernames[0]).username }),
      this.i18n(`content.identities.action.${bulkActionValue}.header`, { count: usernames.length})
    ).then(() => {
      this.context.store.dispatch(identityManager.setUsersActivity(usernames, bulkActionValue));
    }, () => {
      // nothing
    });
  }

  onReset(bulkActionValue, usernames) {
    // push state to redux
    this.context.store.dispatch(this.dataManager.storeData('selected-usernames', usernames));
    // redirect to reset page
    this.context.router.push(`/identities/password/reset`);
  }

  render() {
    const {
      uiKey,
      identityManager,
      columns,
      forceSearchParameters,
      showAddButton,
      deleteEnabled,
      showRowSelection,
      rendered,
      treeType
    } = this.props;
    const { filterOpened, treeNodeId, text } = this.state;
    if (!rendered) {
      return null;
    }
    //
    let _forceSearchParameters = forceSearchParameters || new SearchParameters();
    let forceTreeNodeSearchParams = new SearchParameters();
    if (!treeType) {
      forceTreeNodeSearchParams = forceTreeNodeSearchParams.setFilter('defaultTreeType', true);
    } else {
      forceTreeNodeSearchParams = forceTreeNodeSearchParams.setFilter('treeTypeId', treeType.id);
      _forceSearchParameters = _forceSearchParameters.setFilter('treeTypeId', treeType.id);
    }
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-deactivate" level="danger"/>
        <Basic.Confirm ref="confirm-activate"/>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={identityManager}
          showRowSelection={showRowSelection && SecurityManager.hasAuthority('IDENTITY_WRITE')}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm data={{ text, treeNodeId }} ref="filterForm" className="form-horizontal">
                <Basic.Row>
                  <div className="col-lg-6">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('content.identities.filter.name.placeholder')}
                      label="Text"
                      onChange={this._changeText.bind(this)}
                      help={filterHelp}/>
                  </div>
                  <div className="col-lg-6 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
                <Basic.Row className="last">
                  <div className="col-lg-6">
                    <Advanced.Filter.SelectBox
                      ref="treeNodeId"
                      placeholder="Prvek v organizační struktuře"
                      label="Org. struktura"
                      forceSearchParameters={forceTreeNodeSearchParams}
                      manager={this.treeNodeManager}
                      onChange={this._changeTreeNode.bind(this)}/>
                  </div>
                  <div className="col-lg-6">
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={filterOpened}
          forceSearchParameters={_forceSearchParameters}
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: !deleteEnabled },
              { value: 'activate', niceLabel: this.i18n('content.identities.action.activate.action'), action: this.onActivate.bind(this) },
              { value: 'deactivate', niceLabel: this.i18n('content.identities.action.deactivate.action'), action: this.onActivate.bind(this) },
              { value: 'password-reset', niceLabel: this.i18n('content.identities.action.reset.action'), action: this.onReset.bind(this), disabled: true }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                type="submit"
                className="btn-xs"
                onClick={this.showDetail.bind(this, {})}
                rendered={showAddButton && SecurityManager.hasAuthority('IDENTITY_WRITE')}>
                <Basic.Icon type="fa" icon="user-plus"/>
                {this.i18n('content.identity.create.button.add')}
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
          <Advanced.Column property="_links.self.href" face="text" rendered={false}/>
          <Advanced.ColumnLink to="identity/:username/profile" property="username" width="20%" sort face="text" rendered={_.includes(columns, 'username')}/>
          <Advanced.Column property="lastName" sort face="text" rendered={_.includes(columns, 'lastName')}/>
          <Advanced.Column property="firstName" width="10%" face="text" rendered={_.includes(columns, 'firstName')}/>
          <Advanced.Column property="email" width="15%" face="text" sort rendered={_.includes(columns, 'email')}/>
          <Advanced.Column property="disabled" face="bool" sort width="100px" rendered={_.includes(columns, 'disabled')}/>
          <Basic.Column
            header={this.i18n('entity.Identity.description')}
            cell={<Basic.TextCell property="description" />}
            rendered={_.includes(columns, 'description')}/>
        </Advanced.Table>
      </div>
    );
  }
}

IdentityTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  identityManager: PropTypes.object.isRequired,
  /**
   * Rendered columns
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Button for create user is rendered
   */
  showAddButton: PropTypes.bool,
  /**
   * Table supports delete identities
   */
  deleteEnabled: PropTypes.bool,
  /**
   * Enable row selection - checkbox in first cell
   */
  showRowSelection: PropTypes.bool,
  /**
   * Rendered
   */
  rendered: PropTypes.bool,
  /**
   * Filter tree type structure - given id ur default - false
   * @deprecated Remove after better tree type - node filter component
   */
  treeType: PropTypes.oneOfType([PropTypes.bool, PropTypes.string]),
};

IdentityTable.defaultProps = {
  columns: ['username', 'lastName', 'firstName', 'email', 'disabled', 'description'],
  filterOpened: false,
  showAddButton: true,
  deleteEnabled: false,
  showRowSelection: false,
  forceSearchParameters: null,
  rendered: true,
  treeType: false
};

function select(state, component) {
  return {
    _searchParameters: state.data.ui[component.uiKey] ? state.data.ui[component.uiKey].searchParameters : {},
    deleteEnabled: ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.core.identity.delete')
  };
}

export default connect(select, null, null, { withRef: true })(IdentityTable);
