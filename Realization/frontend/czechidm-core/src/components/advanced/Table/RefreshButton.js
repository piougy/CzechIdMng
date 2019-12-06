import React from 'react';
import PropTypes from 'prop-types';
import * as Basic from '../../basic';

/**
 * Refresh button for table. Can be used even without advanced table (some css are hard coded to fit table toolbar - props can be added if needed).
 *
 * @author Radek Tomiška
 */
export default class RefreshButton extends Basic.AbstractContextComponent {

  render() {
    const { rendered, showLoading, waiting, title, onClick, readOnly } = this.props;
    if (!rendered) {
      return null;
    }
    // default detail title
    const _title = title || this.i18n('button.refresh');
    //
    return (
      <Basic.Button
        className="btn-xs"
        title={ _title }
        onClick={ onClick }
        disabled={readOnly}
        titlePlacement="bottom"
        showLoading={ showLoading }
        style={{ marginLeft: 3}}>
        <Basic.Icon value="fa:refresh" color={waiting ? '#2d6ca2' : undefined} showLoading={ showLoading || waiting }/>
        {
          onClick
          ||
          <span>Please, define onClick method on detail button</span>
        }
      </Basic.Button>
    );
  }
}

RefreshButton.propTypes = {
  ...Basic.AbstractContextComponent.propTypes,
  /**
   * onClick callback
   */
  onClick: PropTypes.func.isRequired,
  /**
   * Buttons tooltip, otherwise default 'button.refresh' will be used
   */
  title: PropTypes.oneOfType([PropTypes.string, PropTypes.element])
};
RefreshButton.defaultProps = {
  ...Basic.AbstractContextComponent.defaultProps
};
