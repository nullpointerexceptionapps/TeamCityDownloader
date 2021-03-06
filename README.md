TeamCityDownloader
==================
Android app to browse TeamCity server build artifacts &amp; download them  

Play Link: [Play Store](https://play.google.com/store/apps/details?id=com.raidzero.teamcitydownloader)

Current Features
-----
* Light and dark material theme
* Cache web responses 
* Automatically open downloaded files (install APK's)
* Notify of new builds in favorite configurations
* Download multiple files at once
* TeamCity guest authentication or username & password
* Mark build config as favorite to keep it in navigation drawer
* Only download files the device can open
* Supports multiple build branches
* Display build properties including custom parameters
* Open some things in web browser

Supported Artifact File Formats
-----
* Anything you have an app to open, if no app is found on the device, an error will display and the 
file will not download.

Documentation
-----
To get started, use the navigation drawer to get to settings and set up your server parameters.

Once a server is set up, you can go back to the main screen and tab "Select Projects".
Check the desired projects and tap "Save".

Once you have set up some projects, you can browse them. Long press on a build to view its information.
If you want to set a favorite, browse to a build configuration and tap the star icon. This will "star"
the build and place a shortcut into the navigation drawer. To return to this build in the future, you
can access it directly from the navigation drawer. To un-"star", just tap the star icon and will return
to its empty state, removing the drawer shortcut.

To install an android app from a build, just tap on the APK file you wish to install.

Multiple downloads enabled: you can download many files at once. If you tap the notification you
will be taken to the Downloads screen where you can get the files. Long press to cancel download or
delete a completed download.

Multiple downloads disabled: It will download one file at a time and automatically prompt you to
install. If you download a file type other than APK, it will automatically open the file, using any
app you choose.
