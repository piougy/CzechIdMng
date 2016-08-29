import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../../../components/basic';
import { IdentityManager } from 'core/redux';
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
    const { userID } = this.props.params;
    this.selectNavigationItems(['user-profile', 'profile-personal']);
    this.context.store.dispatch(identityManager.fetchEntity(userID));
  }

  componentDidUpdate() {
  }

  render() {
    const { identity } = this.props;
    const { userID } = this.props.params;
    return (
      <div>
        {
          <IdentityDetail identity={identity} userID={userID} />
        }
      </div>
    );
  }
}

Profile.propTypes = {
};

Profile.defaultProps = {
};

function select(state, component) {
  const { userID } = component.params;
  return {
    identity: identityManager.getEntity(state, userID)
  };
}

export default connect(select)(Profile);
