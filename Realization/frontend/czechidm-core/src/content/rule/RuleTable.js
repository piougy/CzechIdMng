import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import { SecurityManager } from '../../redux';
import RuleCategoryEnum from '../../enums/RuleCategoryEnum';
import AbstractEnum from '../../enums/AbstractEnum';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
/**
 * Table with definitions of rules
 */
export class RuleTable extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: false
    };
  }

  getContentKey() {
    return 'content.rules';
  }

  componentDidMount() {
    const { ruleManager, uiKey } = this.props;
    const searchParameters = ruleManager.getService().getDefaultSearchParameters();
    // fetch all entities for rules
    this.context.store.dispatch(ruleManager.fetchEntities(searchParameters, uiKey));
  }

  componentWillUnmount() {
    this.cancelFilter();
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    const data = {
      name: this.refs.filterForm.getData().name,
      category: AbstractEnum.findKeyBySymbol(RuleCategoryEnum, this.refs.filterForm.getData().category)
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
    const { uiKey, ruleManager } = this.props;
    const selectedEntities = ruleManager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    // show confirm message for deleting entity or entities
    this.refs['confirm-' + bulkActionValue].show(
      this.i18n(`action.${bulkActionValue}.message`, { count: selectedEntities.length, record: ruleManager.getNiceLabel(selectedEntities[0]), records: ruleManager.getNiceLabels(selectedEntities).join(', ') }),
      this.i18n(`action.${bulkActionValue}.header`, { count: selectedEntities.length, records: ruleManager.getNiceLabels(selectedEntities).join(', ') })
    ).then(() => {
      // try delete
      this.context.store.dispatch(ruleManager.deleteEntities(selectedEntities, uiKey, (entity, error, successEntities) => {
        if (entity && error) {
          this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: ruleManager.getNiceLabel(entity) }) }, error);
        }
        if (!error && successEntities) {
          // refresh data in table
          this.refs.table.getWrappedInstance().reload();
        }
      }));
    }, () => {
      //
    });
  }

  /**
   * Recive new form for create new type else show detail for existing org.
   */
  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    // when create new rule is generate non existing id
    // and set parameter new to 1 (true)
    // this is necessary for RuleDetail
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.router.push(`/rules/${uuidId}?new=1`);
    } else {
      this.context.router.push('/rules/' + entity.id);
    }
  }

  render() {
    const { uiKey, ruleManager } = this.props;
    const { filterOpened } = this.state;

    return (
      <Basic.Row>
        <div className="col-lg-12">
          <Basic.Confirm ref="confirm-delete" level="danger"/>
          <Advanced.Table
            ref="table"
            uiKey={uiKey}
            manager={ruleManager}
            showRowSelection={SecurityManager.hasAuthority('RULE_DELETE')}
            rowClass={({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; }}
            filter={
              <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
                <Basic.AbstractForm ref="filterForm" className="form-horizontal">
                  <Basic.Row>
                    <div className="col-lg-6">
                      <Advanced.Filter.TextField
                        ref="name"
                        placeholder={this.i18n('entity.Rule.name')}
                        label={this.i18n('entity.Rule.name')}/>
                    </div>
                    <div className="col-lg-6 text-right">
                      <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                    </div>
                  </Basic.Row>
                  <Basic.Row>
                    <div className="col-lg-6">
                      <Basic.EnumSelectBox
                        ref="category"
                        labelSpan="col-lg-4"
                        componentSpan="col-lg-8"
                        label={this.i18n('entity.Rule.category')}
                        enum={RuleCategoryEnum}/>
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
                        rendered={SecurityManager.hasAuthority('RULE_WRITE')}>
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
            <Advanced.Column property="name" sort />
            <Advanced.Column property="category" sort face="enum" enumClass={RuleCategoryEnum}/>
          </Advanced.Table>
        </div>
      </Basic.Row>
    );
  }
}

RuleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  ruleManager: PropTypes.object.isRequired
};

RuleTable.defaultProps = {
  _showLoading: false
};

export default connect()(RuleTable);
