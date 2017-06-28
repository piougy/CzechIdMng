import React from 'react';
//
import * as Basic from '../../components/basic';
import ComponentService from '../../services/ComponentService';

export default class IdentityCreate extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.componentService = new ComponentService();
  }

  getContentKey() {
    return 'content.identity.create';
  }

  componentWillMount() {
    this.setState({
      showLoading: true
    });
  }

  componentDidMount() {
    this.selectNavigationItem('identities');
  }

  render() {
    const Create = this.componentService.getComponent('identity-create');
    return (
      <Create params={this.props.params} />
    );
  }
}

IdentityCreate.propTypes = {
};

IdentityCreate.defaultProps = {
};
