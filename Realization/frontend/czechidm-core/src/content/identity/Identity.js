import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import * as Advanced from '../../components/advanced';
import OrganizationPosition from './OrganizationPosition';

const identityManager = new IdentityManager();

class IdentityContent extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount() {
    this._selectNavigationItem();
    const { entityId } = this.props.params;
    //
    this.context.store.dispatch(identityManager.fetchEntityIfNeeded(entityId));
    //
    if (!this.state.imageUrl && entityId) {
      identityManager.download(entityId, this.receiveImage.bind(this));
    }
  }

  receiveImage(blob) {
    const objectURL = URL.createObjectURL(blob);
    this.setState({imageUrl: objectURL});
  }

  componentDidUpdate() {
    this._selectNavigationItem();
    // TODO: move to componentWillReceiveNextProps
    const { entityId } = this.props.params;
    //
    this.context.store.dispatch(identityManager.fetchEntityIfNeeded(entityId, null, (entity, error) => {
      this.handleError(error);
    }));
  }

  _selectNavigationItem() {
    const { entityId } = this.props.params;
    const { userContext } = this.props;
    if (entityId === userContext.username) {
      this.selectNavigationItems(['identity-profile', null]);
    } else {
      this.selectNavigationItems(['identities', null]);
    }
  }

  render() {
    const { identity } = this.props;
    const { entityId } = this.props.params;
    const imageUrl = this.state.imageUrl;

    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.profile')} />

        <Basic.PageHeader>
          <img
          src={imageUrl}
          className="img-circle"
          style={{height: '40px'}} />
          {' '}
          {identityManager.getNiceLabel(identity)} <small> {this.i18n('content.identity.profile.userDetail')}</small>
        </Basic.PageHeader>

        <OrganizationPosition identity={entityId}/>

        <Advanced.TabPanel position="left" parentId="identity-profile" params={this.props.params} style={{ display: 'none'}}>
          {this.props.children}
        </Advanced.TabPanel>
      </div>
    );
  }
}

IdentityContent.propTypes = {
  identity: PropTypes.object,
  userContext: PropTypes.object,
  selectedSidebarItem: PropTypes.string
};
IdentityContent.defaultProps = {
  identity: null,
  userContext: null,
  selectedSidebarItem: null
};

function select(state, component) {
  const { entityId } = component.params;
  const selectedNavigationItems = state.config.get('selectedNavigationItems');
  const selectedSidebarItem = (selectedNavigationItems.length > 1) ? selectedNavigationItems[1] : null;
  return {
    identity: identityManager.getEntity(state, entityId),
    userContext: state.security.userContext,
    selectedSidebarItem
  };
}

export default connect(select)(IdentityContent);
