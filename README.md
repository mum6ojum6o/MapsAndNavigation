# MapsAndNavigation
An Android Application to utilize the prowess of Maps and Navigation

<b> Initial Configuration </b><br/>
This app uses the Google maps API and teh Directions API.<br/>
In order to build/run the app on your system, ensure you have a non-restricted API Key.<br/>
For info regarding obtaining your api key, refer the link below:-<br/>
https://developers.google.com/maps/documentation/android-sdk/get-api-key<br/>

Once you've created an API key, copy it and store it in a text file called "api.porperties".<br/>
The api.properties file should contain the following text:-<br/>
GOOGLE_MAPS_API_KEY=<<||Your GOOGLE MAPS API KEY||>><br/>

Place the api.properties file in the "app" folder of the project.<br/>


<h2>Steps to Develop the App</h2>
1. The app contains only One activity to host the Fragment.<br/>
2. The Fragment aka MAPFragment does all the heavy lifting.<br/>
3. Initialize the Map<br/>
4. If the Fragment is not being restored, then fetch the last known loaction of the device and update the location on the Map.<br/>
5. On entering a Search String and Pressing the "GO" key, the address and Location of the searched key is fetched.<br/>
  5.1 The Map camera is also moved to display the searched location.<br/>
  5.2 A Floating point button "Direction" is made Visible.<br/>
  5.3 Upon Clicking the Directions FAB, possible routes to the searched location are rendered on the map.<br/>
  5.4 the user can click on any of the routes to determine the possible duration of the selected route. <br/>
  By Default, the route with the lowest duration will be highlited (Blue color).<br/>
6. Once the app is rendering all possible routs to the destination, another FAB is made VISIBLE.
The purpose of this FAB is to launch an Implicit Intent to start Navigation via Google Maps.
