# TimeDuration component

Component producing time difference - duration
The rendered output may look like this 1d 01:22:33 if the duration exceeds on day.
Otherwise only portion without days is displayed - 01:22:33.

## Parameters

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| start  | string   | Initial timestamp in ISO-8601 format |  |
| stop  | string   | Terminal timestamp in ISO-8601 format|  |
| humanForm | bool | Verbal form of duration is the main and numeric as the hint | false

## Usage

```html
<TimeDuration
  start="2016-05-02T00:00:00"
  end="2016-05-02T00:00:00"
  humanForm
/>
```
