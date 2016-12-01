# SelectBox component

Component for search and select item any entity. Extended from AbstractFormComponent.
Component supported single select and multi select mode.

## Parameters

All parameters from AbstractFormComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| spanClassName | string | Default class name of main span | col-sm-offset-3 col-sm-8  |
| triggerForTooltip  | array   | Default triger for tooltip  | hover |
| placementForTooltip | string   | Tooltip placement | right |
| tooltip | string  | Default localization for tooltip | content.password.change.passwordChangeTooltip |
| max  | number   | Maximum for estimator  |  |
| initialStrength  | number   | Default value for estimator | 0 |
| icon | string   | Icon form with type FA. Icon is locate right by estimator | lock |
| isIcon | bool   | Show icon | true |
| isTooltip | bool   | Show tooltip | true |
| opacity | number   | Opacity of all component, sometimes is useful | 0.54 |
| value | string   | String for strength estimator | null |

## Usage
### Select
```html
<StrengthEstimator
  max="5"
  initialStrength="1"
  value="testPassword321!@#$" />

```
