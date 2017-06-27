import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { IdentityManager } from '../../redux';
import ComponentService from '../../services/ComponentService';

const identityManager = new IdentityManager();

class Profile extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      showLoading: false,
      showLoadingIdentityTrimmed: false,
    };
    //
    this.componentService = new ComponentService();
  }

  getContentKey() {
    return 'content.identity.profile';
  }

  componentWillMount() {
    this.setState({
      showLoading: true
    });
  }

  componentDidMount() {
    const { entityId } = this.props.params;
    this.selectNavigationItems(['identity-profile', 'profile-personal']);
    this.context.store.dispatch(identityManager.fetchEntity(entityId, null, (entity, error) => {
      this.handleError(error);
    }));
  }

  render() {
    const { identity } = this.props;
    const { entityId } = this.props.params;
    const IdentityDetail = this.componentService.getComponent('identity-detail');
    //
    return (
      <div className="tab-pane-text-body">
        <IdentityDetail identity={identity} entityId={entityId} params={this.props.params} />
      </div>
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
