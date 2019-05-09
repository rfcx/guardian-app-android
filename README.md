# rfcx-ranger-android


#### Requirements
- Android Studio 3.0
- Gradle 3.0.0

#### Configuration group by Firebase remote config.
You can group by application ID.<br/>
### For the sample <br/>
create "groupC"

#### Create productFlavor
open "build.gradle (Module : app)"

```
productFlavors { 
        groupC{
            applicationIdSuffix".groupc"
        }
    }
```
Here, the application ID is "android.rfcx.org.ranger.groupc"

#### Add app on Firebase
visit https://console.firebase.google.com/u/2/project/rfcx-ranger/overview

Press ADD ANOTHER APP

![screen shot 2560-11-21 at 12 36 09 am](https://user-images.githubusercontent.com/13133464/33032768-d3625a9a-ce54-11e7-9d8a-553a6b3c1828.png)

Choose Add Firebase to your android app. Fill application ID and name. Then press Register app<br/>

![screen shot 2560-11-21 at 12 46 54 am](https://user-images.githubusercontent.com/13133464/33033040-a8a7d540-ce55-11e7-9b56-aa0caf50884a.png)

Download "google-services.json" and put it to directory "app/src/groupc/".

```
app/
      src/
            main/
            groupc/
                google-services.json
```
#### Add condition on remote config.
visit https://console.firebase.google.com/u/2/project/rfcx-ranger/config <br/>
On "CONDITION" tab, press "NEW CONDITION". <br/>
Fill condition name and apply app with application ID. <br />
Press "CREATE CONDITION". <br/>

![screen shot 2560-11-21 at 1 02 57 am](https://user-images.githubusercontent.com/13133464/33033793-447214b6-ce58-11e7-803f-34fcc8d46dc0.png)

#### Custom config for Ranger-GroupC.
Now you can custom parameter for "Ranger-GroupC". <br/>
On "PARAMETERS" tab. <br/>
- Select parameter need to custom.
- Press Add value for condition.
- Choose condition (here is "Ranger-GroupC").
- Put your value.
- Press Update.

#### Publish config from remote config.
To publish the config press "PUBLISH CHANGES". <br />

![screen shot 2560-11-21 at 1 25 43 am](https://user-images.githubusercontent.com/13133464/33034607-f211af26-ce5a-11e7-849d-33850a80d870.png)

#### Firebase Remote Config parameters
- enable_notification_event_alert  <br/>
  Show/not show alert notification : value true/false.
- enable_notification_message <br/>
  Show/not show message notification : value true/false.
- notification_polling_frequency_duration <br/>
  Time to interval pulling notification : value time in second (minumum 60).
- show_event_list <br/>
  Show/not show alert (event) on list. : value true/false.
- user_group
  The group name : value : String grup name.
  
#### Run App.
Select build variants in Android Studio and Run >> Run app.<br/>
![screen shot 2560-11-21 at 1 56 12 am](https://user-images.githubusercontent.com/13133464/33035904-582c17f2-ce5f-11e7-978a-0a593273a6e0.png)
  
