import React, { PropTypes } from 'react';
import * as Basic from '../../basic';

/**
 * Quick dashboard button supper class.
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
export default class AbstractIdentityDashboardButton extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false
    };
  }

  /**
   * Identity identifier - can be resolved from params (url) or from user context (global dashboard).
   *
   * @return {string} id or username (prefered username - more readable)
   */
  getIdentityIdentifier() {
    const { entityId, userContext } = this.props;
    //
    if (entityId) {
      return entityId;
    }
    if (userContext) {
      return userContext.username;
    }
    return null;
  }

  /**
   * Button icon.
   * Override button label or icon at least.
   *
   * @return {string}
   */
  getIcon() {
    return null;
  }

  /**
   * Button is rendered.
   *
   * @return {Boolean}
   */
  isRendered() {
    return true;
  }

  /**
   * Button text.
   * Override label or icon at least.
   *
   * @return {string}
   */
  getLabel() {
    return null;
  }

  /**
   * Button tooltip.
   *
   * @return {string}
   */
  getTitle() {
    return null;
  }

  /**
   * OnClick button function
   */
  onClick() {
    return;
  }

  /**
   * Render button content (~children).
   */
  renderContent() {
    return this.getLabel();
  }

  /**
   * Action runs
   * @return {Boolean}
   */
  isShowLoading() {
    return this.state.showLoading;
  }

  /**
   * Button level (~color). See Button.level property for available options.
   *
   * @return {string}
   */
  getLevel() {
    return 'default';
  }

  render() {
    return (
      <Basic.Button
        icon={ this.getIcon() }
        level={ this.getLevel() }
        className="btn-large"
        onClick={ this.onClick.bind(this) }
        style={{ height: 50, marginRight: 3, minWidth: 150 }}
        title={ this.getTitle() }
        titlePlacement="bottom"
        rendered={ this.isRendered() === true ? true : false }
        showLoading={ this.isShowLoading() }
        showLoadingIcon>
        { this.renderContent() }
      </Basic.Button>
    );
  }
}

AbstractIdentityDashboardButton.propTypes = {
  /**
   * Selected identity (in redux state)
   *
   * @type {IdmIdentityDto}
   */
  identity: PropTypes.object.isRequired,
  /**
   * Loaded perrmissions for selected identity                                 [description]
   */
  permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ]),
  /**
   * Security context
   */
  userContext: PropTypes.object.isRequired
};
