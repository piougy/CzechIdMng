import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import { ConfidentialStorageValueManager } from '../../redux';
import ConfidentialStorageValueDetail from './ConfidentialStorageValueDetail';

const confidentialStorageValueManager = new ConfidentialStorageValueManager();

/**
 * Confidential storage value detail content
 *
 * @author Patrik Stloukal
 */
class ConfidentialStorageValueContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.confidentialStorage';
  }

  componentDidMount() {
    this.selectNavigationItem('confidential-storage');
    const { entityId } = this.props.params;

    this.getLogger().debug(`[TypeContent] loading entity detail [id:${entityId}]`);
    this.context.store.dispatch(confidentialStorageValueManager.fetchEntity(entityId));
  }

  render() {
    const { entity, showLoading } = this.props;
    return (
      <div>
          <Helmet title={this.i18n('edit.title')} />
        {
          !entity
          ||
          <Basic.PageHeader>
            <Basic.Icon value="fa:lock"/>
            {' '}
            {
              <span>{this.i18n('edit.header')}</span>
            }
          </Basic.PageHeader>
        }

        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !entity
            ||
            <ConfidentialStorageValueDetail entity={entity} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

ConfidentialStorageValueDetail.propTypes = {
  showLoading: PropTypes.bool
};
ConfidentialStorageValueDetail.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    entity: confidentialStorageValueManager.getEntity(state, entityId),
    showLoading: confidentialStorageValueManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(ConfidentialStorageValueContent);
