import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import IdentityDetail from './IdentityDetail';

const identityManager = new IdentityManager();

class Profile extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      showLoading: false,
      showLoadingIdentityTrimmed: false,
    };
  }

  getContentKey() {
    return 'content.user.profile';
  }

  componentWillMount() {
    this.setState({
      showLoading: true
    });
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['user-profile', 'profile-personal']);
    this.context.store.dispatch(identityManager.fetchEntity(entityId));
  }

  render() {
    const { identity } = this.props;
    const { entityId } = this.props.params;
    return (
      <IdentityDetail identity={identity} entityId={entityId} />
    );
  }
}

Profile.propTypes = {
};

Profile.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.params;
  return {
    identity: identityManager.getEntity(state, entityId)
  };
}

export default connect(select)(Profile);
