# SchedulerTaskInfo Component

Information about scheduler task. Mainly used for internal

## Parameters

All parameters from AbstractComponent are supported. Added parameters:

| Parameter | Type | Description | Default  |
| --- | :--- | :--- | :--- |
| entity | object  |  Selected entity - externally loaded.  Has higher priority, when is given, then loading is not needed. |  |
| entityIdentifier | string  |  Selected entity's identifier - entity will be loaded automatically.  |  |
| face | oneOf(['link'])  |  Decorator: <ul><li>TODO:  `text`: entity's nice label only</li><li>`link`: entity's nice label with link to detail</li><li>TODO: `full`: full info card</li></ul>  |  full |


## Usage

```html
<Advanced.SchedulerTaskInfo entity={{ taskType: 'eu.bcvsolutions.role.ExcludeTask' }}/>
```

or

```html
<Advanced.SchedulerTaskInfo entityIdentifier="uuid" />
```
