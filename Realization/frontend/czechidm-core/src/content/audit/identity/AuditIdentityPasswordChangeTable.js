import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import * as Utils from '../../../utils';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { PasswordHistoryManager } from '../../../redux';

const passwordHistoryManager = new PasswordHistoryManager();

/**
* Table of Audit for password change
*
* @author Ondřej Kopr
*/
export class AuditIdentityPasswordChangeTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit.identityPasswordChange';
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
    if (this.refs.table !== undefined) {
      this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
    }
  }

  _getAdvancedFilter() {
    const { singleUserMod } = this.props;
    return (
      <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
        <Basic.AbstractForm ref="filterForm">
          <Basic.Row>
            <Basic.Col lg={ 8 }>
              <Advanced.Filter.FilterDate ref="fromTill"/>
            </Basic.Col>
            <div className="col-lg-4 text-right">
              <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
            </div>
          </Basic.Row>
          <Basic.Row>
            <div className="col-lg-6">
              <Advanced.Filter.TextField
                className="pull-right"
                ref="creator"
                placeholder={this.i18n('content.audit.identities.modifier')}/>
            </div>
            <div className="col-lg-6">
              <Advanced.Filter.TextField
                className="pull-right"
                rendered={!singleUserMod}
                ref="identityUsername"
                placeholder={this.i18n('content.audit.identities.username')}/>
            </div>
          </Basic.Row>
          <Basic.Alert level="warning" text={this.i18n('idmOnlyInfo')} style={{ margin: 0 }}/>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }

  _getForceSearchParameters() {
    const { id } = this.props;
    let forceSearchParameters = passwordHistoryManager.getDefaultSearchParameters()
      .setFilter('changedAttributesList', ['validFrom']);
    if (id) {
      forceSearchParameters = forceSearchParameters.setFilter('identityId', id);
    }
    return forceSearchParameters;
  }

  render() {
    const { uiKey, singleUserMod } = this.props;
    //
    return (
      <div>
        <Advanced.Table
          ref="table"
          filterOpened
          uiKey={ uiKey }
          manager={passwordHistoryManager}
          forceSearchParameters={this._getForceSearchParameters()}
          filter={ this._getAdvancedFilter() }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header={this.i18n('entity.Identity._type')}
            property="identity"
            rendered={!singleUserMod}
            width={ 250 }
            cell={
              ({ rowIndex, data }) => {
                const identity = data[rowIndex]._embedded.identity;
                if (!identity) {
                  return null;
                }
                return (
                  <Advanced.EntityInfo
                  entityType="identity"
                  entityIdentifier={ identity.id }
                  entity={ identity }
                  face="popover"
                  showIdentity={ false }/>
                );
              }
            }/>
          <Advanced.Column
            property="created"
            header={this.i18n('content.audit.identityPasswordChange.created')}
            width={ 200 }
            sort
            face="datetime"/>
          <Advanced.Column
            property="creator"
            header={this.i18n('content.audit.identityPasswordChange.creator')}
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (!entity) {
                  return null;
                }
                const identity = {
                  id: entity.creatorId,
                  username: entity.creator
                };
                return (
                  <Advanced.EntityInfo
                    entityType="identity"
                    entityIdentifier={ identity.id }
                    entity={ identity }
                    face="popover"
                    showIdentity={ false }/>
                );
              }
            }/>
        </Advanced.Table>
      </div>
    );
  }
}

AuditIdentityPasswordChangeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  username: PropTypes.string,
  singleUserMod: PropTypes.boolean,
  id: PropTypes.string
};

AuditIdentityPasswordChangeTable.defaultProps = {
  singleUserMod: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(AuditIdentityPasswordChangeTable);
