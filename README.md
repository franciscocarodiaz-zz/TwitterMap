# TwitterMap
Single view Android application that displays tweets as pins on a map in real time according to a search term.

- Main activity with main functions. At the end of the file there is all the old methods used for testing and first version.
- Data folder. All the activities related to the Content Provider.
- Service. This folder contains all the methods to call to the Twitter API. At the end is only used the TwitterMapService that is responsible to remove data from content provider after TIME_RELOAD seconds.
- Twitter. Contains all the Java classes related to Twitter: user, searches, tweets, etc.
- Util. Contains all the aux methods and classes used in the main activity.
  In Const java class there are to important int declared: TIME_TO_RELOAD, responsible to call the alarm to remove data presented in the map. And, TWITTER_SIZE_LIMIT responsible of the length of the array of tweets return by Twitter API.

What I have used for this project?
- Android Studio, Gradle, and debugging tools, User Interface and Layout managers.
- How to connect to the Cloud: Threading and ASyncTask, HTTP requests on web APIs and Android Permission System.
- How to create new Activities and how to navigate between them, Intents and Services (Broadcast Intents and Broadcast Receivers).
- How to use Content Providers and Loaders to Persist and Recover Data.
- Next challenges: Use Services and Notifications to Run in the Background.

Apk file added "TwitterMap-app-release.apk" to test app. 