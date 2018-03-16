/**
 * Domain types register
 *
 * import { SearchParameters } from './domain' can be used in react components (ui layer)
 *
 * @author Radek Tomiška
 */
import SearchParameters from './SearchParameters';
import FormInstance from './FormInstance';
import HelpContent from './HelpContent';

const TopDomainRoot = {
  SearchParameters,
  FormInstance,
  HelpContent
};

TopDomainRoot.version = '0.0.1';
module.exports = TopDomainRoot;
