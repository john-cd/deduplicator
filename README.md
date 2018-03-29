# Deduplicator

My first scala project: a tool to identify duplicate files (files with the same content) 

- read command line parameters and a configuration file
- walk one or more directory tree
- compute a hash for each file asynchronously
- support for large files
- save hashes to a data store
- identify duplicate files

## Learning Objectives

I chose to use / explore the following technologies
- Scala Futures
- java.nio
- cake design pattern 

The codebase borrows from [https://github.com/nikolovivan/scala-design-patterns](https://github.com/nikolovivan/scala-design-patterns)
