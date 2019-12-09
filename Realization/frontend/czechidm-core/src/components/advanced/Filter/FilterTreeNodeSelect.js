import PropTypes from 'prop-types';
import TreeNodeSelect from '../TreeNodeSelect/TreeNodeSelect';

/**
 * @todo-upgrade-10 - Remove this class - use TreeNodeSelect instead
 * Tree node select used in filters
 *
 * @author Radek Tomiška
 */
export default class FilterTreeNodeSelect extends TreeNodeSelect {

}

FilterTreeNodeSelect.propTypes = {
  ...TreeNodeSelect.propTypes,
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
const { labelSpan, componentSpan, disableable, ...otherDefaultProps } = TreeNodeSelect.defaultProps; // labelSpan etc. override
FilterTreeNodeSelect.defaultProps = {
  ...otherDefaultProps,
  relation: 'EQ',
  uiKey: 'filter-tree-node-tree',
  disableable: false
};
