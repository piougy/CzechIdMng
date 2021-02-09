import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import FormAttributeTable from '../form/FormAttributeTable';
import { CodeListManager } from '../../redux';

const manager = new CodeListManager();

/**
* Attributes for the code list.
*
* @author Radek Tomiška
* @since 9.4.0
*/
class CodeListAttributes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.code-lists';
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getNavigationKey() {
    return 'code-list-attributes';
  }

  render() {
    const { entity, showLoading } = this.props;
    //
    return (
      <Basic.Div>
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }
        {
          !entity || showLoading
          ?
          <Basic.Loading isStatic show />
          :
          <FormAttributeTable uiKey={ `code-list-attribute-${ entity.id }-table`} definitionId={ entity.formDefinition.id } className="no-margin" />
        }
      </Basic.Div>
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

export default connect(select)(CodeListAttributes);
