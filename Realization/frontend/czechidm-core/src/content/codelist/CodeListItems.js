import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import CodeListItemTable from './CodeListItemTable';
import { CodeListManager } from '../../redux';

const manager = new CodeListManager();

/**
* Codel list items
*
* @since 9.4.0
*/
class CodeListItems extends Basic.AbstractContent {

  getContentKey() {
    return 'content.code-lists';
  }

  getNavigationKey() {
    return 'code-list-items';
  }

  render() {
    const { entity, showLoading } = this.props;
    //
    return (
      <Basic.Panel className={'no-border last'}>
        <Basic.PanelHeader text={this.i18n('content.code-lists.items.title')} />
        <Basic.PanelBody style={{ padding: 0 }}>
          {
            !entity || showLoading
            ?
            <Basic.Loading isStatic show />
            :
            <CodeListItemTable uiKey={ `code-list-item-${ entity.id }-table` } codeList={ entity } />
          }
        </Basic.PanelBody>
      </Basic.Panel>
    );
  }
}

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(CodeListItems);
