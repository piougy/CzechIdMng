import AdConnectorIcon from '../AdConnectorIcon/AdConnectorIcon';

/**
 * Icon for scripted SQL connector.
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @since 10.7.0
 */
export default class ScriptedSqlConnectorIcon extends AdConnectorIcon {

  getIcon() {
    return 'fa:code';
  }

  getLevel() {
    return 'primary';
  }
}
