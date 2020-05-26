# comp90020

![Java CI with Maven](https://github.com/omjadas/comp90020/workflows/Java%20CI%20with%20Maven/badge.svg?branch=master)
[![CodeFactor](https://www.codefactor.io/repository/github/omjadas/comp90020/badge?s=e63190bc215f22d323425533d74470789495079a)](https://www.codefactor.io/repository/github/omjadas/comp90020)

Distributed banking application written in Java that utilizes Snapshot recovery
algorithms such as those proposed by Chandy-Lamport and Friedemann Mattern.

## Building

To build the program use Maven.

```bash
mvn package
```

An uber JAR will be created in the target directory.

## Usage

To start the program specify the port to listen on as an argument.

```bash
java -jar <path-to-jar> <port>
```

Once you have started the program you will be presented with a prompt where you
are able to enter commands to interact with the system.

```bash
open <account-id> # open an account with a given ID
deposit <account-id> <amount> # deposit into an account
withdraw <account-id> <amount> # withdraw from an account
transfer <source-id> <dest-id> <amount> # transfer from one account to another
connect <hostname> <port> # connect to a remote branch
balance <account-id> # print the balance for an account
list # print all known accounts
mattern # initiate Mattern's algorithm
chandy-lamport # initiate the Chandy Lamport algorithm
delay # wait for 10 seconds
exit # exit the program
```
