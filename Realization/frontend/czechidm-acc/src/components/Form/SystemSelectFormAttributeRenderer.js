import { Advanced } from 'czechidm-core';
import { SystemManager } from '../../redux/';

const manager = new SystemManager();

/**
 * Identity SelectBox form value component
 *
 * @author Radek Tomiška
 */
export default class SystemSelectFormAttributeRenderer extends Advanced.AbstractSelectBoxFormAttributeRenderer {

  getManager() {
    return manager;
  }
}
