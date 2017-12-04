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
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: false
    };
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
      this.context.router.push(`/automatic-role/attributes/${uuidId}?new=1`);
    } else {
      this.context.router.push('/automatic-role/attributes/' + entity.id);
    }
  }

  render() {
    const { uiKey, manager } = this.props;
    const { filterOpened } = this.state;
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
                <Basic.Row>
                  <div className="col-lg-6">
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text')}/>
                  </div>
                  <div className="col-lg-6 text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </div>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={!filterOpened}
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
          <Advanced.Column property="name" sort />
          <Advanced.Column property="_type" sort />
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
