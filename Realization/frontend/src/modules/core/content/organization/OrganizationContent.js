import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from 'app/components/basic';
import { OrganizationManager } from 'core/redux';
import OrganizationDetail from './OrganizationDetail';

const organizationManager = new OrganizationManager();

/**
 * Organization detail content
 */
class OrganizationContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.organizations';
  }

  componentDidMount() {
    this.selectNavigationItem('organization');
    const { entityId } = this.props.params;
    const { query } = this.props.location;
    const isNew = (query) ? query.new : null;

    if (isNew) {
      this.context.store.dispatch(organizationManager.receiveEntity(entityId, { }));
    } else {
      this.getLogger().debug(`[OrganizationContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(organizationManager.fetchEntity(entityId));
    }
  }

  render() {
    const { organization, showLoading } = this.props;
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.PageHeader>
          <Basic.Icon value="fa:building"/>
          {' '}
          {this.i18n('header')}
        </Basic.PageHeader>

        <Basic.Panel>
          <Basic.Loading isStatic showLoading={showLoading} />
          {
            !organization
            ||
            <OrganizationDetail organization={organization} />
          }
        </Basic.Panel>

      </div>
    );
  }
}

OrganizationContent.propTypes = {
  organization: PropTypes.object,
  showLoading: PropTypes.bool
};
OrganizationContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  //
  return {
    organization: organizationManager.getEntity(state, entityId),
    showLoading: organizationManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(OrganizationContent);
