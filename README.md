# Important Info

This repository is an extended version of [FCM XMPP Connection Server 2](https://github.com/carlosCharz/fcmxmppserverv2) (its fork) which adds support for scheduling notifications for clients which send the details of the notifications to be scheduled via a FCM upstream message. The corresponding android client is located at [FCM Test Android Client](https://github.com/yatharthranjan/FCMTest) and the cordova client is located at [Cordova client](https://github.com/yatharthranjan/cordova-notification-test) which uses this [fcm plugin](https://github.com/yatharthranjan/cordova-plugin-fcm) 
The following changes have been introduced -

1. The maven build has been converted to a gradle build and several improvements to the build process.
2. Added a command line parser for arguments and also looks in environment variables for these values.
3. Added notification scheduling function by parsing payloads from upstream messages containing the action `SCHEDULE`. The format of the data payload of upstream message should contain atleast
    ```javascript
    {
    "data":
        {
            "notificationTitle":"Schedule id 1",
            "notificationMessage":"hello",
            "action":"SCHEDULE",
            "time":"1531482349791",
            "subjectId":"test",
            "ttlSeconds":""
         },
         ...
    }
    ```
    If `ttlSeconds` is not specified the default is set to 28 days. Note that the `time` parameter is in milliseconds (from epoch time) while the `ttlSeconds` is in seconds as apparent from its name.
4. Currently it supports 3 types of schedulers - simple, in-memory and persistent. As the name suggests, simple is just a java thread based scheduler without any DB, in-memory uses an instance of in-memory database and persistent writes database files to disk. We use the HyperSQL DB or HSQL in short as it supports both in memory and persistent options.
5. To locally build the server and run it - 
    ```shell
    ./gradlew clean build
    ```
    Then you can just run the jar file generated or any of the distribution files with -h or --help option to display the usage -
    ```shell
    java -jar radar-xmppserver-all-0.1.0-SNAPSHOT.jar --help
    ```
    
    or install package into `/usr/local` using the distribution
    ```shell
    sudo tar -xzf build/distributions/radar-xmppserver-0.1.0-SNAPSHOT.tar.gz -C /usr/local --strip-components=1
    ```
    Now you can run with command
    ```shell
    radar-xmppserver --help
    ```
    
    Run again by specifying the options you find in the above help -
    ```shell
    radar-xmppserver [Options]
    ```
    
6. Notifications can be cancelled using the data below from an upstream message - 
    ```javascript
    {
    "data":
        {
            "action": "CANCEL",
            "cancelType": "all",
            "subjectId": "yatharth"
         }
         ...
    }

    ```
7. A Dockerfile is also included with the project for running the server in a docker container.
   Build the docker image by running the following in the root directory - 
   ```bash
   docker build -t "radarbase/radar-xmppserver:0.1.0" .
   ```
   Then run the container like to get all the available options -
   ```bash
    docker run radarbase/radar-xmppserver:0.1.0 --help
    ```
    Then run with the options - 
    ```bash
    docker run radarbase/radar-xmppserver:0.1.0 [Options]
    ```

8. Using it with docker-compose is also possible. Just add the following to the docker-compose.yml file under the services tag -

    ```yaml
    ...
      xmppserver:
        image: radarbase/radar-xmppserver:0.1.0
        restart: always
        environment:
          RADAR_XMPP_FCM_SENDER_KEY: <your-sender-key>
          RADAR_XMPP_FCM_SERVER_KEY: <your-fcm-server-key>
          RADAR_XMPP_SCHEDULER_TYPE: <scheduler-type>
          RADAR_XMPP_DB_PATH: <db-path(if using db scheduler)>
        volumes:
          - ./myhostdir/hsql/:/usr/hsql/
     ...
    ```
    OR
    
    ```yaml
    ...
      xmppserver:
        image: radarbase/radar-xmppserver:0.1.0
        restart: always
        command: -s <sender-id> -k <server-key> -ns <notification-scheduler-type> ...
        volumes:
          - ./myhostdir/hsql/:/usr/hsql/
     ...
    ```
9. As seen above the options can be either set as command line args or as environment variables. The docker image is also published on Docker hub [radarbase/radar-xmppserver](https://hub.docker.com/r/radarbase/fcmxmppserverv2/).
   Also make sure to mount the path of the db as a bind mount to host if using a persistent db otherwise all data will be lost if the container is removed.


# Old README from base

### XMPP Connection Server for FCM (Upgrade from Smack 3.x to 4.x) + Connection Draining Implementation
This is an upgrade of my last [FCM XMPP Connection Server](https://github.com/carlosCharz/fcmxmppserver) application. Now, this project uses the latest version at this time of the Smack library (4.2.4). _**I just added the connection draining implementation to this project. If you had some problems check my troubleshooting section!**_

The new version has introduced new terminology, deprecated some older methods and enriched the library in general. The problem started when there is a no working example out there using the new version to build a XMPP CCS for FCM. In summary, the API changes from the 3.x to the 4.x version are:

1. XMPPConnection is now an interface. Use either AbstractXMPPConnection or one of its subclasses (XMPPTCPConnection).
2. XMPPConnection.addPacketListener is deprecated: use either addAsyncPacketListener or addSyncPacketListener.
3. Packet became a deprecated interface. Use the new Stanza class.
4. The Packet Extension term is now Extension Element.
  
For more information you must read the following documentation: 
 
* [The upgrade guide](https://github.com/igniterealtime/Smack/wiki/Smack-4.1-Readme-and-Upgrade-Guide)
* [New Smack Terminology](https://github.com/igniterealtime/Smack/wiki/New-Smack-Terminology)
* [The Smack Javadoc](http://download.igniterealtime.org/smack/docs/latest/javadoc/)

_**ADDITIONAL USEFUL LINKS**_

* [XMPP Connection Server for FCM](https://github.com/carlosCharz/fcmxmppserver): This project is the original code base and it gives you a simple explanation of FCM XMPP Connection Server.
* [FCM Connection Draining solution explanation](https://youtu.be/6AQCnNWPksg): This video explains how I handle the FCM Connection Draining message.

### New Smack libraries

 * [Smack java 7](https://mvnrepository.com/artifact/org.igniterealtime.smack/smack-java7)
 * [Smack tcp](https://mvnrepository.com/artifact/org.igniterealtime.smack/smack-tcp)
 * [Smack extensions](https://mvnrepository.com/artifact/org.igniterealtime.smack/smack-extensions)

### How to start the server
Just because it is the same project as my prior solution, the way to start the server is exactly the same. You can read my [how to start the server](https://github.com/carlosCharz/fcmxmppserver).

### Troubleshooting
This is a simple java code. You can integrate with just spring or spring in a container (Tomcat for example). In any case you need to take into account these issues: 

1. _**If using a simple java application, keep the application alive listening messages.**_ This problem occurs when you use a simple java code as a daemon in linux (that's why I put the while true workaround).

2. _**If using a java server (with spring boot at least), close the XMPP connection when the server goes down.**_ This problem occurs when even if you shutdown the server the XMPP connection is still open and handling incoming messages. I do not know yet if this is a spring boot problem. The thing is like we are wrapping the XMPP CCS Server into a HTTP interface just to treat it as a normal server. My workaround was putting the disconnection in the spring preDestroy. @PreDestroy public void springPreDestroy() { //ccsConnection.disconnect(); }

3. _**Reconnection when connection draining.**_ The reconnection should be handled to connect again to the FCM using the same parameters with a backoff strategy. Smack can handle the automatic reconnection if you enable it but if the connection will be closed from the FCM side you should call your reconnection method when FCM sends connection draining control message or connection draining error message.

4. _**Handle properly the new connection creation.**_ If you handle differently the connection draining or use the reconnect. Always make sure that when you create a new connection the old one is completely closed gracefully to avoid phantom connections!

## About me
I am Carlos Becerra - MSc. Softwware & Systems. You can contact me via:

* [Google+](https://plus.google.com/+CarlosBecerraRodr%C3%ADguez)
* [Twitter](https://twitter.com/CarlosBecerraRo)

### Thanks
To tell the truth. I was really worried looking for the right solution. Finally, I made a list of useful links (apart from the above documentation links).

* [gcm server](http://www.marothiatechs.com/2015/08/building-your-own-android-chat_18.html)
* [stanza](http://www.programcreek.com/java-api-examples/index.php?api=org.jivesoftware.smack.packet.Stanza)
* [a problem](https://community.igniterealtime.org/thread/59532)
* [other gcm server](https://github.com/googlesamples/friendlyping/blob/master/server/Java/src/main/java/com/gcm/samples/friendlyping/GcmServer.java)

_**Any improvement or comment about the project is always welcome! As well as others shared their code publicly I want to share mine! Thanks!**_

### License
```javas
Copyright 2016 Carlos Becerra

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
