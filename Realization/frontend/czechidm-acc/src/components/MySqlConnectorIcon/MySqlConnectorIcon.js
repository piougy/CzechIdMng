import AdConnectorIcon from '../AdConnectorIcon/AdConnectorIcon';

/**
 * Icon for MSSQL connector.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @since 10.7.0
 */
export default class MySqlConnectorIcon extends AdConnectorIcon {

  getIcon() {
    return 'fa:database';
  }

  getLevel() {
    return 'primary';
  }
}
