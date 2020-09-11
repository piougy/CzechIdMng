import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import * as Basic from '../../basic';
import * as Utils from '../../../utils';
import { ConfigurationManager, SecurityManager, AuditManager, DataManager } from '../../../redux';
import EntityInfo from './EntityInfo';
import DateValue from '../DateValue/DateValue';

const auditManager = new AuditManager();
const dataManager = new DataManager();


/**
 * Auditable system information (info card).
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export class AuditableInfo extends Basic.AbstractContextComponent {

  getComponentKey() {
    return 'component.advanced.AuditableInfo';
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

  getPopoverContent(entity) {
    const { showAuditLink, additionalOptions } = this.props;
    //
    let content = [];
    if (additionalOptions) {
      content = content.concat(additionalOptions);
    }
    content.push(
      {
        label: this.i18n('entity.id.label'),
        value: (
          <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
            <Basic.Div style={{ flex: 1 }}>
              <input
                ref="input-id"
                type="text"
                value={ entity.id }
                readOnly
                style={{ fontSize: '0.85em', width: '100%' }}
                onClick={ () => {
                  // ~ctrl+c
                  this.refs['input-id'].select();
                  document.execCommand('copy');
                  this.addMessage({ level: 'success', message: this.i18n('component.advanced.UuidInfo.copy.message') });
                }}/>
            </Basic.Div>
            <Basic.Div rendered={ showAuditLink && SecurityManager.hasAuthority('AUDIT_READ') } style={{ marginLeft: 3 }}>
              <Basic.Button
                className="btn-xs"
                href="#"
                onClick={ this.showAudit.bind(this, entity, 'entityId') }
                title={ this.i18n('component.advanced.Table.button.entityId.title') }
                titlePlacement="left"
                icon="component:audit"/>
            </Basic.Div>
          </Basic.Div>
        )
      }
    );
    //
    if (entity.created) {
      content.push(
        {
          label: this.i18n('entity.created'),
          value: (<DateValue value={ entity.created } format={ this.i18n('format.datetimemilis') }/>)
        },
        {
          label: this.i18n('entity.creator'),
          value: (
            <EntityInfo
              entityType="identity"
              entityIdentifier={ entity.creator }
              face="link" />
          )
        }
      );
    }
    //
    if (entity.modified) {
      content.push(
        {
          label: this.i18n('entity.modified.short'),
          value: (<DateValue value={ entity.modified } format={ this.i18n('format.datetimemilis') }/>)
        },
        {
          label: this.i18n('entity.modifier.short'),
          value: (
            <EntityInfo
              entityType="identity"
              entityIdentifier={ entity.modifier }
              face="link" />
          )
        }
      );
    }
    //
    if (entity.transactionId) {
      content.push(
        {
          label: (
            <span title={ this.i18n('entity.transactionId.label') }>
              { this.i18n('entity.transactionId.short') }
            </span>
          ),
          value: (
            <Basic.Div style={{ display: 'flex', alignItems: 'center' }}>
              <Basic.Div style={{ flex: 1 }}>
                <input
                  ref="input-transaction-id"
                  type="text"
                  value={ entity.transactionId }
                  readOnly
                  style={{ fontSize: '0.85em', width: '100%' }}
                  onClick={ () => {
                    // ~ctrl+c
                    this.refs['input-transaction-id'].select();
                    document.execCommand('copy');
                    this.addMessage({ level: 'success', message: this.i18n('component.advanced.UuidInfo.copy.message') });
                  }}/>
              </Basic.Div>
              <Basic.Div rendered={ showAuditLink && SecurityManager.hasAuthority('AUDIT_READ') } style={{ marginLeft: 3 }}>
                <Basic.Button
                  className="btn-xs"
                  href="#"
                  onClick={ this.showAudit.bind(this, entity, 'transactionId') }
                  title={ this.i18n('component.advanced.Table.button.transactionId.title') }
                  titlePlacement="left"
                  icon="component:audit"/>
              </Basic.Div>
            </Basic.Div>
          )
        }
      );
    }
    //
    return content;
  }

  _renderContent(entity) {
    return (
      <Basic.Table
        condensed
        hover={ false }
        noHeader
        data={ this.getPopoverContent(entity) }>
        <Basic.Column property="label"/>
        <Basic.Column property="value"/>
      </Basic.Table>
    );
  }

  _renderPopover(entity) {
    const { placement } = this.props;
    //
    return (
      <Basic.Popover
        trigger={ ['click'] }
        placement={ placement }
        value={ this._renderFull(entity) }
        className="abstract-entity-info-popover">
        {
          <span className="popover-link" title={ this.i18n('link.title') }>
            <Basic.Icon value="fa:cog" style={{ color: '#ccc' }} />
          </span>
        }
      </Basic.Popover>
    );
  }

  _renderFull(entity) {
    return (
      <Basic.Panel className="panel-success" style={{ width: 300 }}>
        <Basic.PanelHeader>
          <Basic.Icon value="fa:cog" style={{ marginRight: 5 }}/>
          { this.i18n('header') }
        </Basic.PanelHeader>
        { this._renderContent(entity) }
      </Basic.Panel>
    );
  }

  render() {
    const { rendered, entity, show, showLoading, face } = this.props;
    //
    if (!rendered || !show || !entity || Utils.Entity.isNew(entity)) {
      return null;
    }
    if (showLoading) {
      return (
        <Basic.Icon value="fa:cog" showLoading/>
      );
    }
    switch (face) {
      case 'popover': {
        return this._renderPopover(entity);
      }
      case 'content': {
        return this._renderContent(entity);
      }
      default: {
        return this._renderFull(entity);
      }
    }
  }
}

AuditableInfo.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  entity: PropTypes.object,
  /**
   * Decorator
   */
  face: PropTypes.oneOf(['popover', 'content', 'full']),
  /**
   * Shows links to audit.
   * FIXME: embedded content in info component doesn't have context.history ...  why?
   */
  showAuditLink: PropTypes.bool,
  /**
   * Additional options to show in table.
   */
  additionalOptions: PropTypes.arrayOf(
    PropTypes.shape({
      label: PropTypes.string,
      value: PropTypes.object,
    })
  ),
  /**
   * Popover placement.
   *
   * @since 10.6.0
   */
  placement: PropTypes.oneOf(['left', 'right', 'top', 'bottom'])
};
AuditableInfo.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps,
  entity: null,
  face: 'popover',
  showAuditLink: true,
  placement: 'left'
};

function select(state) {
  //
  return {
    show: ConfigurationManager.showSystemInformation(state)
  };
}
export default connect(select)(AuditableInfo);
