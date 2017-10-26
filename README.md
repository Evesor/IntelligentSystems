# intelligent systems
group 7 - project 3

## requirements
* minimum java jdk 8u131

## quickstart
### windows machines
`./gradlew.bat runAppDev`

### posix machines
`./gradlew runAppDev`

then access `http://localhost:4567/` using your web browser (latest firefox, chrome and edge supported)

## configuration
### agent environment
simply copy the file 
`backend/control-server/config/system-definition.json.template` 
to
`backend/control-server/config/system-definition.json`

and edit appropriately.

the given template json should serve most of your purposes.
however if different behaviour is desired then some delving into the source code may be necessary.

### distributed systems configuration
support for running a distributed system is currently not available. check back later.

## frontend development server
running `./gradlew startDevServer` will spin up a server for frontend development.

