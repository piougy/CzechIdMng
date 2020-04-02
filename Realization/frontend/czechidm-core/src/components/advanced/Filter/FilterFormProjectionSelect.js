import PropTypes from 'prop-types';
import FormProjectionSelect from '../FormProjectionSelect/FormProjectionSelect';

/**
 * Form projection select used in filters.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
export default class FilterFormProjectionSelect extends FormProjectionSelect {

}

FilterFormProjectionSelect.propTypes = {
  ...FormProjectionSelect.propTypes,
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
const { labelSpan, componentSpan, disableable, ...otherDefaultProps } = FormProjectionSelect.defaultProps; // labelSpan etc. override
FilterFormProjectionSelect.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ',
  disableable: false
};
