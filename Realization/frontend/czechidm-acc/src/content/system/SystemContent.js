import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import { Basic, Utils } from 'czechidm-core';
import { SystemManager } from '../../redux';
import SystemDetail from './SystemDetail';

const manager = new SystemManager();

/**
 * Target system detail content
 */
class SystemContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.system.detail';
  }

  componentDidMount() {
    this.selectNavigationItem('sys-systems');
    const { entityId } = this.props.params;
    const { query } = this.props.location;
    const isNew = (query) ? query.new : null;

    if (isNew) {
      this.context.store.dispatch(manager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[SystemContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} />

        <Basic.PageHeader showLoading={showLoading}>
          <Basic.Icon value="link"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.header', { name: manager.getNiceLabel(entity) })} }/>
        </Basic.PageHeader>

        <Basic.Panel>
          {
            showLoading
            ?
            <Basic.Loading isStatic showLoading />
            :
            <SystemDetail entity={entity} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

SystemContent.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
SystemContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(SystemContent);
