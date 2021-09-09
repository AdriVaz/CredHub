# CredHub

CredHub is a password manager Android app written in Java.

** WARNING ** This is just a learning project that is NOT SECURE by any means. This app should NOT BE USED to store any real credentials

## Structure

The repo is split in two folders: the main `CredHub` Android Studio project app, and the `WebRepo` that is used to simulate a cloud server storage.

The WebRepository was developed by COSEC (COmputer SECurity) Department of Carlos III University. CredHub was implemented as an university project, and the web repository was provided.

## Web Repository

The web repository command can be run with the command:
```
java -jar SDM_WebRepo.jar https+auth
```

The `https+auth` part can be changed (see SDM_WebRepo.jar help for more), but this option is the one that works with the Android app

The repository already contain some example credentials, stored in the `.cred` files in the `WebRepo/Server` directory


## Access Credentials

The credentials used to access the app (in the main screen) are:
```
User: sdm
Password: repo4droid
```
