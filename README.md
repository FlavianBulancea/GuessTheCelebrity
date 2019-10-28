# GuessTheCelebrity
Opening the app for the first time will take longer (~ 5 min).
Update button will reproccess required information from imdb.

Walking through

The app has a single activity, proccessing, saving and displaying data.
DownloadTask extending AsyncTask is the main proccess used to collect information from https://www.imdb.com/list/ls052283250/, 
using patterns.
Furthermore the application creates an interface in which we can choose celebrities according to an image with a simple counter.
Data is saved using SharedPreferences variables.
