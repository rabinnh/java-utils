# URLFetch

This small project uses the encapsulates fetching a URL using GET, POST, SSL or not, Basic and Digest authorization.
## Description

### Purpose

URLFetch is a simple Maven Java project that solves the main issues that I see posted to StackExchange. The results is a jar file that can be included in your project.

### What does it do?

* Fetch a URL (of course)
* Fetch a URL using SSL
* Fetch a URL using SSL and ignore an invalid (self-signed) certificate
* Fetch a URL using GET
* Fetch a URL using POST with variables
* Authenticate with Basic
* Authenticate with Digest

## Building

To build:

    mvn package

To clean:

    mvn clean

## Using in your project

Simply include the resulting urlfetch jar file in your project. Then import the class, instantiate ii and make the call:

The FetchTest.java file provides a good example.

Note that the return is a HashMap<String, Object> with three keys: "success" (boolean true or false). "httpcode" (if there was one), and "content" (the results).
"content" will contain a stacktrace if one occurred during the call.



