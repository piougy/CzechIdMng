import React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import Tooltip from '../Tooltip/Tooltip';
import Label from '../Label/Label';
import LocalizationService from '../../../services/LocalizationService';
import AbstractContextComponent from '../AbstractContextComponent/AbstractContextComponent';
/**
 * Time duration formatter
 *
 * @author Ondrej Husnik
 */

class TimeDuration extends AbstractContextComponent {

  /**
   * Method fomatting time duration between start and end
   *
   * @param {*} start
   * @param {*} end
   * @returns formatted duration
   */
  _formatDuration(start, end) {
    const dayInMs = 24 * 3600 * 1000;
    const _end = end ? end : moment().utc().valueOf();
    const timeDuration = moment.duration(moment(_end).diff(moment(start)));
    const humanized = timeDuration
      .locale(LocalizationService.getCurrentLanguage())
      .humanize();

    const dayResidueMs = timeDuration.asMilliseconds() % dayInMs;
    const daysDuration = Math.floor(timeDuration.asMilliseconds() / dayInMs);
    const hhmmssDuration = moment.utc(dayResidueMs).format(this.i18n('format.times'));
    const timeInfo = daysDuration > 0 ? `${daysDuration}d ${hhmmssDuration}` : `${hhmmssDuration}`;

    return { timeInfo, humanized };
  }

  render() {
    const { rendered, start, end, level, style, humanForm } = this.props;
    if (!rendered || !start) {
      return null;
    }
    const renderAsTooltip = !level;
    const formattedValues = this._formatDuration(start, end);
    let mainVal = formattedValues.timeInfo;
    let hintVal = formattedValues.humanized;
    if (humanForm) {
      mainVal = formattedValues.humanized;
      hintVal = formattedValues.timeInfo;
    }

    return (
      <span>
        <Tooltip
          style={style}
          rendered={renderAsTooltip}
          value={hintVal}>
          <span>
            {mainVal}
          </span>
        </Tooltip>

        <Label
          rendered={!renderAsTooltip}
          style={style}
          level={level}
          title={hintVal}
          text={mainVal}
        />
      </span>
    );
  }
}

TimeDuration.PropTypes = {
  /**
   * Contains <stared> and <ended> values of
   * date value in iso-8601 format
   */
  start: PropTypes.string,
  end: PropTypes.string,
  /* Human form is a verbal form
   * of approximate time e.g. 18 minutes, a day
   */
  humanForm: PropTypes.bool
};

TimeDuration.defaultProps = {
  rendered: true,
  humanForm: false
};
export default TimeDuration;
