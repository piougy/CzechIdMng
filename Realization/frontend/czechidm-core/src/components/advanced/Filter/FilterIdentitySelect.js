import PropTypes from 'prop-types';
import IdentitySelect from '../IdentitySelect/IdentitySelect';

/**
 * Identity select used in filters.
 *
 * @author Radek Tomiška
 * @since 10.1.0
 */
export default class FilterIdentitySelect extends IdentitySelect {

}

FilterIdentitySelect.propTypes = {
  ...IdentitySelect.propTypes,
  /**
   * Field name - if is not set, then ref is used
   * @type {[type]}
   */
  field: PropTypes.string,
  /**
   * Filter relation
   * TODO: enumeration
   */
  relation: PropTypes.oneOf(['EQ', 'NEQ'])
};
const { labelSpan, componentSpan, disableable, ...otherDefaultProps } = IdentitySelect.defaultProps; // labelSpan etc. override
FilterIdentitySelect.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ',
  disableable: false
};
