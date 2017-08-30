import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import _ from 'lodash';
import uuid from 'uuid';
//
import { Basic, Advanced, Utils, Managers, Domain } from 'czechidm-core';
import { VsRequestManager } from '../../redux';

const manager = new VsRequestManager();

/**
* Table of virtula system requests
*
* @author Vít Švanda
*
*/
export class VsRequestTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.vs-requests';
  }

  /**
   * Base manager for this agenda (used in `AbstractTableContent`)
   */
  getManager() {
    return manager;
  }

  /**
   * Submit filter action
   */
  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().useFilterForm(this.refs.filterForm);
  }

  /**
   * Cancel filter action
   */
  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  /**
   * Link to detail / create
   */
  showDetail(entity) {
    if (Utils.Entity.isNew(entity)) {
      const uuidId = uuid.v1();
      this.context.router.push(`/vs/request/${uuidId}/new?new=1`);
    } else {
      this.context.router.push(`/vs/request/${entity.id}/detail`);
    }
  }

  _getSystemCell({ rowIndex, data }) {
    return (
      <Advanced.EntityInfo
        entityType="system"
        entityIdentifier={ data[rowIndex]._embedded.systemId.id }
        face="popover" />
    );
  }

  _getImplementersCell({ rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || !entity.implementers) {
      return '';
    }
    const identities = [];
    for (const implementer of entity.implementers) {
      identities.push(implementer.id);
    }

    return (
      <Advanced.IdentitiesInfo identities={identities}/>
    );
  }

  render() {
    const { uiKey, columns, forceSearchParameters, showRowSelection } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          filterOpened={filterOpened}
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ Managers.SecurityManager.hasAuthority('VSREQUEST_UPDATE') && showRowSelection }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <div className="col-lg-4">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text.placeholder')}/>
                  </div>
                  <div className="col-lg-4">
                  </div>
                  <div className="col-lg-4 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false },
            ]
          }
          _searchParameters={ this.getSearchParameters() }
          >

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
          <Advanced.Column property="uid" width="15%" sort face="text" rendered={_.includes(columns, 'uid')}/>
          <Advanced.Column
              header={this.i18n('acc:entity.System.name')}
              rendered={_.includes(columns, 'systemId')}
              cell={this._getSystemCell.bind(this)}/>
          <Advanced.Column property="state" width="15%" sort face="text" rendered={_.includes(columns, 'state')}/>
          <Advanced.Column property="operationType" width="15%" sort face="text" rendered={_.includes(columns, 'operationType')}/>
          <Advanced.Column property="executeImmediately" width="15%" sort face="boolean" rendered={_.includes(columns, 'executeImmediately')}/>
          <Advanced.Column
              header={this.i18n('vs:entity.VsRequest.implementers.label')}
              rendered={_.includes(columns, 'implementers')}
              cell={this._getImplementersCell.bind(this)}/>
          <Advanced.Column property="created" width="15%" sort face="datetime" rendered={_.includes(columns, 'created')}/>
          </Advanced.Table>
      </div>
    );
  }
}

VsRequestTable.propTypes = {
  /**
   * Entities, permissions etc. fro this content are stored in redux under given key
   */
  uiKey: PropTypes.string.isRequired,
  /**
   * Rendered columns (all by default)
   */
  columns: PropTypes.arrayOf(PropTypes.string),
  /**
   * Show filter or collapse
   */
  filterOpened: PropTypes.bool,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  /**
   * Show row selection for bulk actions
   */
  showRowSelection: PropTypes.bool
};

VsRequestTable.defaultProps = {
  columns: ['uid', 'state', 'systemId', 'operationType', 'executeImmediately', 'implementers', 'created'],
  filterOpened: false,
  forceSearchParameters: new Domain.SearchParameters(),
  showAddButton: true,
  showRowSelection: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey), // persisted filter state in redux
  };
}

export default connect(select, null, null, { withRef: true })(VsRequestTable);
