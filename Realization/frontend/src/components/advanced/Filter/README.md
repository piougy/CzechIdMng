# Filter Component

Encapsulates all features for filter. Filter works nice together with advanced table.

## Child components:

| Component | Description |
| - | :- | :- | :- |
| FilterButtons | Renders buttons for use and clear filter |
| ToogleFilterButton | Renders button for filter collapsing |
| FilterTextField | Text input. Default operator LIKE |
| FilterEnumSelectBox | Enumeration select. Supports multiselect. Default operator EQ (multi values are appended with OR clausule) |
| FilterBooleanSelectBox | Boolean select |
| FilterDateTimePicker | DateTime select. Default operator EQ |

Components, which are descendant of AbstractFormComponent (e.g. FilterTextField),
new

## Usage
```html
...
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
...
<Advanced.Filter onSubmit={this.useFilter.bind(this)}>
  <Basic.AbstractForm ref="filterForm">
    <Basic.Row>
      <div className="col-lg-4">
        <Advanced.Filter.TextField
          ref="filterCreatedAtFrom"
          field="createdAt"
          relation="GE"
          placeholder={this.i18n('filter.createdAtFrom.placeholder')}
          label={this.i18n('filter.createdAtFrom.label')}/>
      </div>
      <div className="col-lg-4">
        <Advanced.Filter.TextField
          ref="filterCreatedAtTill"
          field="createdAt"
          relation="LE"
          placeholder={this.i18n('filter.createdAtTill.placeholder')}
          label={this.i18n('filter.createdAtTill.label')}/>
      </div>
      <div className="col-lg-4 text-right">
        <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
      </div>
    </Basic.Row>
    <Basic.Row>
      <div className="col-lg-4">
        <Advanced.Filter.TextField
          ref="email"
          placeholder={this.i18n('entity.Identity.email')}
          label={this.i18n('entity.EmailLog.email')}/>
      </div>
      <div className="col-lg-4">
        <Advanced.Filter.TextField
          ref="subject"
          placeholder={this.i18n('entity.EmailLog.subject')}
          label={this.i18n('entity.EmailLog.subject')}/>
      </div>
      <div className="col-lg-4">
      </div>
    </Basic.Row>
    <Basic.Row className="last">
      <div className="col-lg-4">
        <Advanced.Filter.BooleanSelectBox
          ref="success"
          label={this.i18n('filter.success.label')}
          placeholder={this.i18n('filter.success.placeholder')}/>
      </div>
      <div className="col-lg-4">
      </div>
      <div className="col-lg-4">
      </div>
    </Basic.Row>
  </Basic.AbstractForm>
</Advanced.Filter>
```
