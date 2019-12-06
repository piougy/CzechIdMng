import React from 'react';
import PropTypes from 'prop-types';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager, DataManager } from '../../redux';
import * as Advanced from '../../components/advanced';
import OrganizationPosition from './OrganizationPosition';

const identityManager = new IdentityManager();

/**
 * Identity routes / tabs
 *
 * @author Radek Tomiška
 * @author Petr Hanák
 */
class IdentityContent extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    // FIXME: look out - entity is loaded thx to OrganizationPosition => it's commented for now. Find a way to prevent multiple loading
    // this.context.store.dispatch(identityManager.fetchEntityIfNeeded(entityId));
    //
    if (entityId) {
      this.context.store.dispatch(identityManager.downloadProfileImage(entityId));
    }
  }

  componentDidUpdate() {
    // TODO: move to componentWillReceiveNextProps
    // const { entityId } = this.props.match.params;
    //
    // FIXME: look out - entity is loaded thx to OrganizationPosition => it's commented for now. Find a way to prevent multiple loading
    /*
    this.context.store.dispatch(identityManager.fetchEntityIfNeeded(entityId, null, (entity, error) => {
      this.handleError(error);
    }));*/
  }

  render() {
    const { identity, _imageUrl, match } = this.props;
    const { entityId } = match.params;
    //
    return (
      <div>
        <Helmet title={ this.i18n('navigation.menu.profile') } />

        <Basic.PageHeader>
          {
            _imageUrl
            ?
            <img src={ _imageUrl } alt="profile" className="img-circle img-thumbnail" style={{ height: 40, padding: 0 }} />
            :
            <Basic.Icon icon="component:identity" identity={ identity }/>
          }
          { ' ' }
          { identityManager.getNiceLabel(identity) }
          <small>
            {' '}
            { this.i18n('content.identity.profile.userDetail') }
          </small>
        </Basic.PageHeader>

        <OrganizationPosition identity={ entityId }/>

        <Advanced.TabPanel position="left" parentId="identity-profile" match={match}>
          {this.getRoutes()}
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
  selectedSidebarItem: null,
  _imageUrl: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  const selectedNavigationItems = state.config.get('selectedNavigationItems');
  const selectedSidebarItem = (selectedNavigationItems.length > 0) ? selectedNavigationItems[0] : null;
  const profileUiKey = identityManager.resolveProfileUiKey(entityId);
  const profile = DataManager.getData(state, profileUiKey);
  //
  return {
    identity: identityManager.getEntity(state, entityId),
    userContext: state.security.userContext,
    selectedSidebarItem,
    _imageUrl: profile ? profile.imageUrl : null,
  };
}

export default connect(select)(IdentityContent);
