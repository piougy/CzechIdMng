import React, { PropTypes } from 'react';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import {RequestItemManager } from '../../redux';
import ConceptRoleRequestOperationEnum from '../../enums/ConceptRoleRequestOperationEnum';

const uiKey = 'universal-request';
const requestItemManager = new RequestItemManager();

/**
 * Table for request item changes
 *
 * @author Vít Švanda
 */
class RequestItemChangesTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return requestItemManager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'content.requestDetail';
  }

  /**
   * Create value (highlights changes) cell for attributes table
   */
  _getWishValueCell( old = false, showChanges = true, { rowIndex, data}) {
    const entity = data[rowIndex];
    if (!entity || (!entity.value && !entity.values)) {
      return '';
    }
    if (entity.multivalue) {
      const listResult = [];
      if (!entity.values) {
        return '';
      }
      for (const item of entity.values) {
        const value = old ? item.oldValue : item.value;
        if (!old && item.change && showChanges) {
          listResult.push(<Basic.Label
            key={value}
            level={ConceptRoleRequestOperationEnum.getLevel(item.change)}
            title={item.change ? this.i18n(`attribute.diff.${item.change}`) : null}
            style={item.change === 'REMOVE' ? {textDecoration: 'line-through'} : null}
            text={value}/>);
        } else {
          listResult.push(value ? (item.value + ' ') : '');
        }
        listResult.push(' ');
      }
      return listResult;
    }

    if (!entity.value) {
      return '';
    }
    const value = old ? entity.value.oldValue : entity.value.value;
    if (!old && entity.value.change && showChanges) {
      return (<Basic.Label
        title={entity.value.change ? this.i18n(`attribute.diff.${entity.value.change}`) : null}
        level={ConceptRoleRequestOperationEnum.getLevel(entity.value.change)}
        text={value !== null ? value + '' : '' }/>);
    }
    return value !== null ? value + '' : '';
  }

  render() {
    const {itemData, isOperationUpdate} = this.props;

    return (
      <div>
        <Basic.Table
          data={itemData}
          noData={this.i18n('component.basic.Table.noData')}
          rowClass={({rowIndex, data}) => { return (data[rowIndex].changed) && isOperationUpdate ? 'warning' : ''; }}
          className="table-bordered">
          <Basic.Column
            property="name"
            header={this.i18n('itemDetail.changes.property')}/>
          <Basic.Column
            property="oldValue"
            rendered={isOperationUpdate}
            header={this.i18n('itemDetail.changes.oldValue')}
            cell={this._getWishValueCell.bind(this, true, true)}/>
          <Basic.Column
            property="value"
            header={this.i18n('itemDetail.changes.newValue')}
            cell={this._getWishValueCell.bind(this, false, true)}/>
        </Basic.Table>
      </div>
    );
  }
}

RequestItemChangesTable.propTypes = {
  itemData: PropTypes.object,
  isOperationUpdate: PropTypes.bool
};

export default RequestItemChangesTable;
