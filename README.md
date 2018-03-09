[![License](https://img.shields.io/github/license/BasinMC/Ejector.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![GitHub Release](https://img.shields.io/github/release/BasinMC/Ejector.svg?style=flat-square)](https://github.com/BasinMC/Ejector/releases)
[![CircleCI](https://img.shields.io/circleci/project/github/BasinMC/Ejector.svg?style=flat-square)](https://circleci.com/gh/BasinMC/Ejector)
[![Codacy grade](https://img.shields.io/codacy/grade/eb5138887b364cc3ade6d51588170e6f.svg?style=flat-square)](https://app.codacy.com/app/Basin/Ejector/)

Ejector
=======

Discord & IRC Notification Bot

# Table of Contents

* [Usage](#usage)
* [Building](#building)
* [Contact](#contact)
* [Issues](#issues)
* [Contributing](#contributing)
* [License](#license)

Usage
-----

Create an `application.yml` in your runtime directory:

```yml
ejector:
  github:
    secret: 'yoursecret'
  discord:
    enabled: true
    token: 'yourtoken'
    channels:
      - guildId: 1234567890
        channelId: 1234567890
      - guildId: 1234567890
        channelId: 1234567890
        events:
          - organization
          - push
  irc:
    enabled: true
    name: MrPotato
    ident: potato
    servers:
      - hostname: irc.example.org
        port: 6697
        secure: true
        password: 'immaspud'
        channels:
          - name: '#Potato'
          - name: '#SecretSpud'
            events:
              - organization
              - push
      - hostname: irc.example.com
        name: 'NotASpud'
        ident: 'beep'
        channels:
          - name: '#GiantNuts'
```

Event names are equal to the names documented in the [GitHub WebHook Documentation](https://developer.github.com/webhooks/)

Discord credentials can be generated [here](https://discordapp.com/developers/applications/me/create)

Building
--------

1. Clone this repository via ```git clone https://github.com/BasinMC/Ejector.git``` or download a [zip](https://github.com/BasinMC/Ejector/archive/master.zip)
2. Build the library by running ```mvn clean install```
3. The resulting jars can be found in their respective ```target``` directories as well as your local maven repository

Contact
-------

* [IRC #Basin on EsperNet](http://webchat.esper.net/?channels=Basin)
* [Twitter](https://twitter.com/BasinMC)
* [GitHub](https://github.com/BasinMC/Ejector)

Issues
------

You encountered problems with the library or have a suggestion? Create an issue!

1. Make sure your issue has not been fixed in a newer version (check the list of [closed issues](https://github.com/BasinMC/Ejector/issues?q=is%3Aissue+is%3Aclosed)
1. Create [a new issue](https://github.com/BasinMC/Ejector/issues/new) from the [issues page](https://github.com/BasinMC/Ejector/issues)
1. Enter your issue's title (something that summarizes your issue) and create a detailed description containing:
   - What is the expected result?
   - What problem occurs?
   - How to reproduce the problem?
   - Crash Log (Please use a [Pastebin](https://gist.github.com) service)
1. Click "Submit" and wait for further instructions

Contributing
------------

Before you add any major changes to the library you may want to discuss them with us (see
[Contact](#contact)) as we may choose to reject your changes for various reasons. All contributions
are applied via [Pull-Requests](https://help.github.com/articles/creating-a-pull-request). Patches
will not be accepted. Also be aware that all of your contributions are made available under the
terms of the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt). Please read
the [Contribution Guidelines](CONTRIBUTING.md) for more information.

License
-------

This project is released under the terms of the
[Apache License](https://www.apache.org/licenses/LICENSE-2.0.txt), Version 2.0.

The following note shall be replicated by all contributors within their respective newly created
files (variables are to be replaced; E-Mail address or URL are optional):

```
Copyright <year> <first name> <surname <[email address/url]>
and other copyright owners as documented in the project's IP log.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
