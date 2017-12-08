# SocialVideoCache

Android App Demonstrating video streaming and caching on a single request to the server. When the video is cached completely
it can be used for offline viewing.


**[Download the apk](https://github.com/gulzar1996/SocialVideoCache/blob/master/app-debug.apk?raw=true)** 

 How it works ?
================  
 Video is downloaded using AsyncTask. Socket is used to generate a proxy url from mobile storage so that the media player/exoplayer can parse and display video simultaneously
 when the video is completely buffered it can be used later for offline viewing as the video will be available on the disk.
 
