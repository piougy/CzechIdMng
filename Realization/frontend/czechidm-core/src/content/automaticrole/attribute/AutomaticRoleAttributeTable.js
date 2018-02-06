import React, { PropTypes } from 'react';
import uuid from 'uuid';
import { connect } from 'react-redux';
//
import * as Utils from '../../../utils';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { SecurityManager } from '../../../redux';

/**
 * Table with automatic roles
 *
 * @author Ondřej Kopr
 */
export class AutomaticRoleAttributeTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return this.props.manager;
  }

  getContentKey() {
    return 'content.automaticRoles.attribute';
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
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  /**
   * Recive new form for create new type else show detail for existing automatic role.
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/automatic-role/attributes/${uuidId}/new?new=1`);
    } else {
      this.context.router.push('/automatic-role/attributes/' + entity.id);
    }
  }

  render() {
    const { uiKey, manager } = this.props;
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={uiKey}
          manager={manager}
          showRowSelection={SecurityManager.hasAuthority('AUTOMATIC_ROLE_DELETE')}
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text')}/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button level="success" key="add_button" className="btn-xs"
                      onClick={this.showDetail.bind(this, {})}
                      rendered={SecurityManager.hasAuthority('AUTOMATIC_ROLE_CREATE')}>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                {this.i18n('button.add')}
              </Basic.Button>
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
          <Advanced.Column
            property="name"
            width="20%"
            header={this.i18n('entity.AutomaticRole.name.label')}
            sort/>
          <Advanced.Column
            property="_embedded.role.name"
            header={this.i18n('entity.AutomaticRole.role.label')}
            width="25%"
            sort
            sortProperty="role.name"
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={ entity.role }
                    entity={ entity._embedded.role }
                    face="popover"/>
                );
              }
            }/>
          <Advanced.Column
            property="concept"
            sort
            header={this.i18n('entity.AutomaticRole.attribute.concept.label')}
            cell={
              ({ rowIndex, data }) => {
                if (data && data[rowIndex].concept === true) {
                  return (
                    <Basic.Tooltip value={this.i18n('entity.AutomaticRole.attribute.concept.help')}>
                      <Basic.Label level="warning" text={this.i18n('entity.AutomaticRole.attribute.concept.info')}/>
                    </Basic.Tooltip>
                  );
                }
              }
            }/>
        </Advanced.Table>
      </div>
    );
  }
}

AutomaticRoleAttributeTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired
};

AutomaticRoleAttributeTable.defaultProps = {
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(AutomaticRoleAttributeTable);
