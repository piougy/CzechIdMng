import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';


class Systems extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'acc:content.systems';
  }

  componentDidMount() {
    this.selectNavigationItem('sys-systems');
  }

  render() {
    return (
      <span>acc</span>
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
