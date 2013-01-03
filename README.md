XBot Framework
==============

XBot framework is a full-featured platform for running automated scripts on
Wikipedia. More than just a helper class for the [MediaWiki api](http://en.wikipedia.org/w/api.php),
the platform allows many different operations to run in parallel, sharing
computing resources and information. 

A bot processes consists of a class that extends the abstract class BotThread,
located in the com.yetanotherx.xbot.bots package. As a result, every bot is
a separate thread, so care must be taken to ensure proper thread safety. Each
bot implements a doRun() and doShutdown() method, which get run once at startup
and shutdown, respectively. In addition to each bot running in its own thread,
bots can assign jobs to the thread as well, which are individual threads that
are specifically assigned to the bot. They get three parameters: the parent bot
thread, the time to sleep after each run, and whether or not the job should 
loop indefinitely. Each job class must extend the BotJob<? extends BotThread> 
class (an example would be "class SomeJob extends BotJob<SomeThread>" for a job 
that is assigned to SomeThread). These jobs get added to the thread with the 
BotThread.addJob() method. This adds a job to the thread and starts it. 

The bot uses a modified version of [MER-C's Wiki.java](http://code.google.com/p/wiki-java/). 
It is modified to use XBot's specific logger, as well as provide some updated 
features. The XBotWiki class extends this class, and provides functionality for
write throttling, configuration parameters, runpage checking, and monitoring.
It is accessible using the XBot.getWiki() method. 

The bot also contains an interactive console, provided using the jline library.
It allows for commands to be given to the bot, that allow for monitoring the
state of the platform, managing which bots are running, stopping/starting the
process, as well as other useful features. It also contains support for ANSI
color codes, using the ChatColor class. Console output gets sent to both the
standard output and a file, if the --enable-logging=true parameter is set.

XBot is open source and is available under the GNU General Public License v3.

Compiling
---------

The bot is written for Java 6 and can be built using [Maven](http://maven.apache.org). 

Dependencies are automatically handled by Maven.

Contributing
------------

I will happily accept contributions, especially through pull requests on GitHub. 
Submissions must be licensed under the GNU Lesser General Public License v3.
