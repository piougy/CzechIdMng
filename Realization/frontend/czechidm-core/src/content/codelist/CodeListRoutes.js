import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { CodeListManager } from '../../redux';
import CodeListDetail from './CodeListDetail';

const manager = new CodeListManager();

/**
 * Code list detail with registered tabs
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
class CodeListRoutes extends Basic.AbstractContent {

  getContentKey() {
    return 'content.code-lists';
  }

  componentDidMount() {
    const { entityId } = this.props.match.params;

    if (!this._getIsNew()) {
      this.getLogger().debug(`[FormContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    if (query) {
      return !!query.new;
    }
    return false;
  }

  render() {
    const { entity } = this.props;
    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        <Advanced.DetailHeader
          entity={ entity }
          to="/code-lists"
          rendered={ !this._getIsNew() && entity }>
          <span>{ manager.getNiceLabel(entity) } <small>{ this.i18n('edit.title') }</small></span>
        </Advanced.DetailHeader>
        {
          this._getIsNew()
          ?
          <CodeListDetail isNew match={ this.props.match } />
          :
          <Advanced.TabPanel position="left" parentId="code-lists" match={ this.props.match }>
            { this.getRoutes() }
          </Advanced.TabPanel>
        }
      </div>
    );
  }
}

CodeListRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
CodeListRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(CodeListRoutes);
