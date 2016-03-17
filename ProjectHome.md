This is my dumping ground of stuff that doesn't exist anywhere else, that I find handy.

You will find
  * a bootstrapper (<a href='https://libjoe.googlecode.com/svn/trunk/downloads/libjoe-bootstrapper-standalone-0.17.jar'>standalone jar download</a>) to ease starting applications with multiple ordered system property file dependencies (environment specific, user specific, ...).
  * a nearly-complete implementation of a circular `ArrayList` – think `ArrayDeque` retrofitted with the `List` interface
  * a concurrent <a href='http://guava-libraries.googlecode.com/svn/trunk/javadoc/com/google/common/collect/Multimap.html'> <code>Multimap</code></a> implementation based on `ConcurrentHashMap`


This may eventually contain some more handy things, when I get around to it. This is a bit of a todo list for now.
  * some handy reflection utilities for testing and debugging, including getting the statics out of a class, resolving threadlocals. Handy in Eclipse, which doesn't have great support for that.
  * a nearly-optimally low-memory map implementation. Store everything in a minimal array at the expense of update speed
  * a multi-reader single-writer concurrent set, like `ConcurrentHashMap(1)` but without the double indirection through `Sets.newSetFromMap` and from the map to the single segment. Needs to be only weakly-consistent and not throw `ConcurrentModificationException`