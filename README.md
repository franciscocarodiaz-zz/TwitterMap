# TwitterMap
Single view Android application that displays	tweets as pins on a map	in real time according to a search term.

- Main activity with main functions. At the end of the file there is all the old methods used for testing and first version.
- Data folder. All the activities related to the Content Provider.
- Service. This folder contains all the methods to call to the Twitter API. At the end is only used the TwitterMapService that is responsible to remove data from content provider after TIME_RELOAD seconds.
- Twitter. Contains all the Java classes related to Twitterç: user, searches, tweets, etc.
- Util. Contains all the auxiliar methods and classes used in the main activity.
  In Const java class there are to important int declared: TIME_TO_RELOAD, responsible to call the alarm to remove data presented in the map. And, TWITTER_SIZE_LIMIT responsible of the length of the array of tweets return by Twitter API.
  
