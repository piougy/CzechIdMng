import React, { PropTypes } from 'react';
import * as Basic from '../../basic'

/**
 * Detail button for advaced table row 
 */
class DetailButton extends Basic.AbstractContextComponent {

  constructor(props) {
    super(props);
  }

  render() {
    const { rendered, title, onClick } = this.props;
    if (!rendered) {
      return null;
    }
    if (!onClick) {
      return (
        <span>Please, define onClick method on detail button</span>
      );
    }
    // default detail title
    const _title = title || this.i18n('button.detail') ;

    return (
      <Basic.Button
        type="button"
        level="default"
        title={_title}
        titlePlacement="bottom"
        onClick={onClick}
        className="btn-xs">
        <Basic.Icon type="fa" icon="search"/>
      </Basic.Button>
    );
  }
}

DetailButton.propTypes = {
  rendered: PropTypes.bool
};
DetailButton.defaultProps = {
  rendered: true
};

export default DetailButton;
