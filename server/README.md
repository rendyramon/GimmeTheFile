Backend
===
The easiest (and free) way to host the youtube-dl client on a server is using Heroku. Check [this guide](https://devcenter.heroku.com/articles/getting-started-with-python#introduction).

Your Procfile should look like this:
```
web: python gimmethefile.py
```

Your runtime.txt should look like this:
```
python-3.5.1 # Or whatever version you want.
```

Make a requirements.txt with (use virtualenv):
```
python freeze > requirements.txt
```

After you have everything set-up, remember to add BASE_URL to app.properties in the root directory.

## youtube-dl
[youtube-dl](http://rg3.github.io/youtube-dl/) updates frequently, it is recommended that you upgrade the python package weekly.