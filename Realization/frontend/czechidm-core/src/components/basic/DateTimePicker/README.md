# DateTimePicker component

Component for select date and time. Extended from AbstractFormComponent.

## Parameters

All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| mode  | oneOf(['date', 'time', 'datetime'])   |  Defined mode of component see @DateTimePicker. Use 'datetime' for DateTime columns, timezone is ignored for LocalDate columns. | 'datetime' |
| dateFormat | string   | Defined input date format (use moment.js)  | DD.MM.YYYY |
| timeFormat | string   | Defined input time format (use moment.js)  | HH:mm |
| componentSpan  | string | defined span for component | col-sm-5 |
| validate | func | Custom validate method, see example below. Selected date (moment) value is given as parameter. Validation error message should be returned. | |
| minDate  | moment | Minimum valid date (use moment as value) |  |
| maxDate  | moment | Maximum valid date (use moment as value) |  ||

## Usage

### Date time:
```html
<DateTimePicker
  ref="expireDate"
  label='Expire Date'/>
```

### Only date:
```html
<DateTimePicker
  ref="expireDate"
  mode="date"
  label='Expire Date'/>
```

### Only time:
```html
<DateTimePicker
  ref="expireDate"
  mode="time"
  label='Expire Date'/>
```

### Date time with specific format:
```html
<DateTimePicker
  ref="expireDate"
  dateFormat="DD/MM/YYYY"
  timeFormat="HH:mm"
  label='Expire Date'/>
```


### Custom validate method

```html
<Basic.DateTimePicker
  mode="date"
  ref="validFrom"
  label={ this.i18n('label.validFrom') }
  validate={
    (value) => {
      const maxDate = moment().add(13, 'months');
      if (!value.isAfter(maxDate)) {
        // ok
        return null;
      }
      // validation failed
      return {
        error: {
          details: [
            {
              type: 'date.max',
              context: {
                limit: maxDate.format(this.i18n('format.date'))
              }
            }
          ]
        }
      };
    }
  }/>
```
or
```html
<Basic.DateTimePicker
  mode="date"
  ref="validFrom"
  label={ this.i18n('label.validFrom') }
  validate={
    (value) => {
      const maxDate = moment().add(13, 'months');
      if (!value.isAfter(maxDate)) {
        // ok
        return null;
      }
      // validation failed
      return {
        error: {
          message: this.i18n('label.validFrom')
        }
      };
    }
  }/>
```

### Validate maximum date

```html
<Basic.DateTimePicker
  mode="date"
  ref="validFrom"
  label={ this.i18n('label.validFrom') }
  maxDate={ moment().add(13, 'months') }/>
```
