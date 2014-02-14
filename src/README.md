FrontServe v0.0.1
=================
> Helps keep frontend repositories available, all the time.

What is it?
-----------

FrontServe aims to sit inbetween your build machine, and the various frontend packagement systems that exist for Javascript. It is loosely inspired by Nexus for Maven. 

FrontServe will act as your registry for Bower and NPM. It will download the meta data, and packages, from the sources defined in the NPM and Bower registry and keep local copies.

Bower and NPM already store local copies but you can not share that cache with other team members or your build server. FrontServe aims to fill that gap. Once the meta data and package is downloaded by FrontServe, it will not be fetched again from NPM/Bower.

FrontServe is created in Groovy and compiled into a single exectubable JAR, with no dependency beyond a JRE.

How does it work?
-----------------

NPM and Bower behave very differently. NPM contains a centralized repository of packages, while Bower only has a centralized database of packages. Bower will fetch packages directly from git repositories while NPM will fetch packages from this centralized place.

When using NPM, FrontServe will fetch the json metadata from NPM and cache it, changing the URL locations of NPM packages to become FrontServe URLs instead. When a package itself is requested through FrontServe, it will be downloaded from NPM first and stored. Subsequent requests will always use the local cache.

When using Bower, FrontServe will fetch the json metadata from Bower and cache it, changing all the git:// urls to FrontServe http:// urls. FrontServe will cache the raw git data locally and subsequent requests will rely on the local data.

With all this information kept local to FrontServe, you can now use FrontServe whether or not Bower, NPM, git or your corporate network is down.

Is this a proxy to get through firewalls?
-----------------------------------------

Not really. FrontServe will talk to npm/bower and git servers directly. There is currently no support for proxies built into FrontServe but it is currently being worked on.

Installation
------------

1. Download zip flie
2. Configure etc/fpcache.properties
3. Run it: java -jar fpcache.jar -config etc/fpcache.properties

Building
--------
mvn clean install

Contributing
------------
Strongly encouraged

