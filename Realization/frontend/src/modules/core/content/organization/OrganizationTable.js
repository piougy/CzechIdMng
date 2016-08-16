import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from 'app/components/basic';
import * as Advanced from 'app/components/advanced';
import * as Utils from 'core/utils';
import uuid from 'uuid';

/**
* Table of organization
*/
export class OrganizationTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  getContentKey() {
    return 'content.organizations';
  }

  componentDidMount() {
  }

  componentDidUpdate() {
    const { organizationTree } = this.props;
    this.refs.table.getWrappedInstance().reload();
  }

  componentWillUnmount() {
    this.cancelFilter();
  }

  useFilter(event) {
    const { organizationManager } = this.props;

    if (event) {
      event.preventDefault();
    }
    const data = {
      ... this.refs.filterForm.getData(),
      parent: organizationManager.getSelfLink(this.refs.filterForm.getData().parent)
    };
    this.refs.table.getWrappedInstance().useFilterData(data);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.getWrappedInstance().cancelFilter(this.refs.filterForm);
  }

  onDelete(bulkActionValue, selectedRows) {
    const selectedEntities = this.getManager().getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: this.getManager().getNiceLabel(selectedEntities[0]), records: this.getManager().getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: this.getManager().getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      this.context.store.dispatch(this.getManager().deleteEntities(selectedEntities, this.context.store.uiKey, () => {
        this.refs.table.getWrappedInstance().reload();
      }));
    }, () => {
      // nothing
    });
  }

  /**
   * Recive new form for create new organization else show detail for existing org.
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/organizations/${uuidId}?new=1`);
    } else {
      this.context.router.push('/organizations/' + entity.id);
    }
  }

  render() {
    const { uiKey, organizationManager } = this.props;
    const { filterOpened } = this.state;

    return (
      <div>
      <Advanced.Table
        ref="table"
        uiKey={uiKey}
        manager={organizationManager}
        showRowSelection
        rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
        filter={
          <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
            <Basic.AbstractForm ref="filterForm" className="form-horizontal">
              <Basic.Row className="last">
                <div className="col-lg-4">
                  <Advanced.Filter.TextField
                    ref="text"
                    placeholder={this.i18n('entity.Organization.name')}
                    label={this.i18n('entity.Organization.name')}/>
                </div>
                <div className="col-lg-4">
                  {
                  <Advanced.Filter.SelectBox
                    ref="parent"
                    placeholder={this.i18n('entity.Organization.parentId')}
                    label={this.i18n('filter.parentId.label')}
                    manager={organizationManager}/>
                    }
                </div>
                <div className="col-lg-4 text-right">
                  <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                </div>
              </Basic.Row>
            </Basic.AbstractForm>
          </Advanced.Filter>
        }
        filterOpened={filterOpened}
        actions={
          [
            { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
          ]
        }
        buttons={
          [
            <Basic.Button level="success" key="add_button" className="btn-xs" onClick={this.showDetail.bind(this, {})} >
              <Basic.Icon type="fa" icon="plus"/>
              {' '}
              {this.i18n('button.add')}
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
        <Advanced.Column property="name" sort/>
        <Advanced.Column property="disabled" sort face="bool"/>
        <Advanced.Column property="shortName" sort rendered={false}/>
        <Advanced.Column property="parentId" sort rendered={false}/>
      </Advanced.Table>
      </div>
    );
  }
}

OrganizationTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  filterOpened: PropTypes.bool,
  organizationManager: PropTypes.object.isRequired,
  organizationTree: PropTypes.object.isRequired
};

OrganizationTable.defaultProps = {
  _showLoading: false
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { withRef: true })(OrganizationTable);
