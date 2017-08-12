# CharForgeMgr

This is a program to manage initiative for character sheets using [CharForge](https://sites.google.com/view/feyrunelabs/charforge). 
The program is written in [Scala](https://www.scala-lang.org/) and can be compiled and run using [sbt](http://www.scala-sbt.org/).
The user will need to provide their own client token for google sheets when compiling and running from source.

The program does not come with monsters built in, although some are provided in the repository.
The program requires a monster's hit dice, initiative, xp worth, and name.

Currently, a means of browsing one's google drive for character sheets is not provided; the party file will have to be manually created.
In addition, there is no way of saving monsters and no encounter creation method yet, these are coming soon!
Until these features come, such things can be managed using the sbt console.
