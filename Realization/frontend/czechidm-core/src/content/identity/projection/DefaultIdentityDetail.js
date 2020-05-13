import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';

/**
 * Default form for identity - default identity detail configurable by identity projections.
 *
 * @author Radek Tomiška
 * @since 10.3.0
 */
export default class DefaultIdentityDetail extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    const { location } = this.props;
    const isNew = !!Utils.Ui.getUrlParameter(location, 'new');
    //
    // redirect to default new / edit identity details
    if (isNew) {
      this.context.history.replace(`/identity/new?id=${ encodeURIComponent(entityId) }`);
    } else {
      this.context.history.replace(`/identity/${ encodeURIComponent(entityId) }/profile`);
    }
  }

  render() {
    return null;
  }
}
