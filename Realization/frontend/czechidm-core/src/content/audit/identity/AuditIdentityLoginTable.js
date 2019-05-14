import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
//
import * as Utils from '../../../utils';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { AuditManager } from '../../../redux';

const auditManager = new AuditManager();

const SUCCESSFUL_LOGIN = 'lastSuccessfulLogin';
const FAILED_LOGIN = 'unsuccessfulAttempts';

/**
* Table of Audit for login
*
* @author Ondřej Kopr
*/
export class AuditIdentityLoginTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit.identityLogin';
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
            <Basic.Col lg={ 4 } className="text-right">
              <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
            </Basic.Col>
          </Basic.Row>
          <Basic.Row className="last">
            <Basic.Col lg={ 4 }>
              <Advanced.Filter.TextField
                className="pull-right"
                rendered={!singleUserMod}
                ref="ownerCode"
                placeholder={this.i18n('content.audit.identities.username')}/>
            </Basic.Col>
          </Basic.Row>
        </Basic.AbstractForm>
      </Advanced.Filter>
    );
  }

  /**
  * Method for show detail of revision, redirect to detail
  *
  * @param entityId id of revision
  */
  showDetail(entityId) {
    this.context.router.push(`/audit/entities/${entityId}/diff/`);
  }

  /**
  * Method for show detail of revision, redirect to detail
  *
  * @param entityId id of revision
  */
  showDetail(entityId) {
    this.context.router.push(`/audit/entities/${entityId}/diff/`);
  }

  _getForceSearchParameters() {
    const { id } = this.props;
    let forceSearchParameters = auditManager.getDefaultSearchParameters().setName('login');
    if (id) {
      forceSearchParameters = forceSearchParameters.setFilter('ownerId', id);
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
          manager={auditManager}
          forceSearchParameters={this._getForceSearchParameters()}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}
          showId
          filter={ this._getAdvancedFilter() }
          _searchParameters={ this.getSearchParameters() }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex].id)}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column
            header={this.i18n('entity.Identity._type')}
            property="identity"
            rendered={!singleUserMod}
            cell={
              ({ rowIndex, data }) => {
                const identity = {
                  id: data[rowIndex].ownerId,
                  username: data[rowIndex].ownerCode
                };
                return (
                  <Advanced.EntityInfo
                    entityType="identity"
                    entityIdentifier={ identity.id }
                    entity={ identity }
                    face="popover"
                    showIdentity={ false }
                    showIcon/>
                );
              }
            }/>
          <Advanced.Column
            header={ this.i18n('loginType') }
            property="modification"
            width={ 100 }
            cell={
              ({ rowIndex, data, property }) => {
                const modification = data[rowIndex][property];
                const changedAttributes = data[rowIndex].changedAttributes;
                let level = 'warning';
                let localizationKey = 'content.audit.identityLogin.modification.';
                if (modification === 'MOD' && changedAttributes && changedAttributes.includes(SUCCESSFUL_LOGIN)) {
                  level = 'success';
                  localizationKey = localizationKey + 'MODSUCCESS';
                } else if (modification === 'MOD' && changedAttributes && changedAttributes.includes(FAILED_LOGIN)) {
                  level = 'danger';
                  localizationKey = localizationKey + 'MODFAILED';
                } else if (modification === 'ADD') {
                  level = 'info';
                  localizationKey = localizationKey + 'ADD';
                }
                return <Basic.Label level={level} text={this.i18n(localizationKey)}/>;
              }}/>
          <Advanced.Column
            property="timestamp"
            header={ this.i18n('entity.Audit.revisionDate') }
            width={ 250 }
            face="datetime"/>
          <Advanced.Column
            property="unsuccessfulAttempts"
            header={ this.i18n('unsuccessfulAttempts') }
            width={ 200 }
            cell={
              ({ rowIndex, data }) => {
                const passwordEntity = data[rowIndex].entity;
                if (passwordEntity && passwordEntity.unsuccessfulAttempts > 0) {
                  return passwordEntity.unsuccessfulAttempts;
                }
              }
            }/>
          <Advanced.Column
            property="mustChange"
            header={ this.i18n('mustChange') }
            face="boolean"
            width={ 200 }
            cell={
              ({ rowIndex, data }) => {
                const passwordEntity = data[rowIndex].entity;
                if (!passwordEntity) {
                  return null;
                }
                return (<Basic.BooleanCell propertyValue={ passwordEntity.mustChange } className="column-face-bool"/>);
              }
            }/>
          <Advanced.Column
            property="validFrom"
            header={ this.i18n('validFrom') }
            width={ 175 }
            face="datetime"
            cell={
              ({ rowIndex, data }) => {
                const passwordEntity = data[rowIndex].entity;
                if (passwordEntity && passwordEntity.validFrom) {
                  // DateCell return invalid date, so we must use moment directly
                  return moment(passwordEntity.validFrom).format(this.i18n('format.date'));
                }
              }
            }/>
          <Advanced.Column
            property="validTill"
            header={ this.i18n('validTill') }
            width={ 175 }
            face="datetime"
            cell={
              ({ rowIndex, data }) => {
                const passwordEntity = data[rowIndex].entity;
                if (passwordEntity && passwordEntity.validTill) {
                  // DateCell return invalid date, so we must use moment directly
                  return moment(passwordEntity.validTill).format(this.i18n('format.date'));
                }
              }
            }/>
          <Advanced.Column
            property="blockLoginDate"
            header={ this.i18n('blockLoginDate') }
            width={ 175 }
            face="datetime"
            cell={
              ({ rowIndex, data }) => {
                const passwordEntity = data[rowIndex].entity;
                if (passwordEntity && passwordEntity.blockLoginDate) {
                  // DateCell return invalid date, so we must use moment directly
                  return moment(passwordEntity.blockLoginDate).format(this.i18n('format.datetime'));
                }
              }
            }/>
        </Advanced.Table>
      </div>
    );
  }
}

AuditIdentityLoginTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  username: PropTypes.string,
  singleUserMod: PropTypes.boolean,
  id: PropTypes.string
};

AuditIdentityLoginTable.defaultProps = {
  singleUserMod: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(AuditIdentityLoginTable);
