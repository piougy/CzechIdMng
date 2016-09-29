import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import { SystemManager } from '../../redux';


class Systems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.systemManager = new SystemManager();
  }

  getManager() {
    return this.systemManager;
  }

  getContentKey() {
    return 'acc:content.systems';
  }

  componentDidMount() {
    this.selectNavigationItem('sys-systems');
  }

  render() {
    return (
      <div>
        TODO: viz configurations content.
      </div>
    );
  }
}

Systems.propTypes = {
};
Systems.defaultProps = {
};

function select(state) {
  return {
  };
}

export default connect(select)(Systems);
