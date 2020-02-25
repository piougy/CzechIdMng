import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import * as Utils from '../../../utils';
import { TreeTypeManager } from '../../../redux';
import TypeDetail from './TypeDetail';
import TypeConfiguration from './TypeConfiguration';

const treeTypeManager = new TreeTypeManager();

/**
 * Type detail content.
 *
 * @author Radek Tomiška
 */
class TypeContent extends Basic.AbstractContent {

  getContentKey() {
    return 'content.tree.types';
  }

  getNavigationKey() {
    return 'tree-types';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    if (this._getIsNew()) {
      this.context.store.dispatch(treeTypeManager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(treeTypeManager.fetchEntity(entityId));
    }
  }

  _getIsNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('title') } />
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.DetailHeader
          icon="fa:folder-open"
          entity={ entity }
          showLoading={ !entity && showLoading }
          to="/tree/types">
          {
            this._getIsNew()
            ?
            this.i18n('create')
            :
            <span>{ treeTypeManager.getNiceLabel(entity) } <small>{ this.i18n('edit') }</small></span>
          }
        </Advanced.DetailHeader>

        {
          (!entity || Utils.Entity.isNew(entity))
          ||
          <TypeConfiguration treeTypeId={entity.id}/>
        }

        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !entity
            ||
            <TypeDetail entity={entity} />
          }
        </Basic.Panel>

      </Basic.Div>
    );
  }
}

TypeContent.propTypes = {
  node: PropTypes.object,
  showLoading: PropTypes.bool
};
TypeContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: treeTypeManager.getEntity(state, entityId),
    showLoading: treeTypeManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(TypeContent);
