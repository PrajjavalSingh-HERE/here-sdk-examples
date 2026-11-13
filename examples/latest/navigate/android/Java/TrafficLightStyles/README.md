The _Traffic Light Styles_ example app shows how the size of the traffic light icons on the map can be changed with a custom map style, without changing the HERE SDK. You can find how this is done in [TrafficLightStylesExample.java](app/src/main/java/com/here/trafficlightstyles/TrafficLightStylesExample.java).

![Traffic light icons rendered with different custom styles](images/traffic_light_styles.png)

- The app shows a static map at zoom level 18.5 with gestures disabled. Traffic light icons are shown from zoom level 18 onwards.
- "SDK default" loads `MapScheme.NORMAL_DAY`. The other buttons load a custom style from the `assets` folder: `traffic-lights-050.zip`, `traffic-lights-100.zip`, `traffic-lights-200.zip` and `traffic-lights-300.zip`.
- Each style inherits the built-in `normal.day` scheme via `"base": {"style": "oslo", "scheme": "normal.day"}` and overrides only the `POI.TrafficLight.Icon.Size` definition.
- The value of the loaded style is shown at the bottom of the screen.
- Note that `POI.TrafficLight.Icon.Size` is a definition of the HERE map style and not a public HERE SDK API. It may change with future HERE SDK versions.

This example uses **HERE SDK Units** to support functionality such as permission handling or buttons that are not essential to the code snippets shown in this app, as the focus is on demonstrating how to use the APIs provided by the HERE SDK. The HERE SDK Units are included as AARs in the app’s `libs` folder. For more details, see the "HERESDKUnits" app to customize or create your own unit libraries. Note that this app is intended exclusively for the HERE SDK (Navigate). You can find it in the `navigate` folder. However, it can be easily adapted for the HERE SDK (Explore) by removing any code that is not supported there. At present, most components are compatible and will compile without issues.

Build instructions:
-------------------

1) Copy the AAR file of the HERE SDK for Android to your app's `app/libs` folder.

Note: If your AAR version is different than the version shown in the _Developer Guide_, you may need to adapt the source code of the example app.

2) Open Android Studio and sync the project.

Please do not forget: To run the app, you need to add your HERE SDK credentials to the `MainActivity.java` file. More information can be found in the _Get Started_ section of the _Developer Guide_.
