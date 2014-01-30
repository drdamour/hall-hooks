Master [![Build Status](https://travis-ci.org/drdamour/hall-hooks.png?branch=master)](https://travis-ci.org/drdamour/hall-hooks)

Hall.com Web Hook Integrations
==============================

A web service for relaying various web hooks to hall.  You can use the deployed application at http://hall-hooks.herokuapp.com/ to test.

Travis-CI Integration
==============================
Go to https://hall.com/docs/integrations/generic/ and get the room token that you wish to notify.  Supposed it's 1234roomtoken.  Then add the following configuration to your .travis.yml file:

```
notifications:
  webhooks:
    urls:
      - https://hall-hooks.herokuapp.com/travis-ci/travis-ci/buildnotification/1234roomtoken
    on_start: true
```

You can use the travis CLI tool to encrypt the url if you don't want to publish your room token (Hall hooks won't remember it)

```
travis encrypt https://hall-hooks.herokuapp.com/travis-ci/travis-ci/buildnotification/1234roomtoken
```

and add the output to your .travis.yml like this:
```
notifications:
  webhooks:
    urls:
      - secure: ecryptedgobblygookFdEd857iwZpN2PAFMsNBAZQdcU9bu
    on_start: true
```
