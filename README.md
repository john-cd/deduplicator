# Deduplicator

__WORK IN PROGRESS__

My first scala project: a tool to identify duplicate files (files with the same content) 

- read command line parameters and a configuraton file
- walk one or more directory tree
- compute a hash for each file asynchronously
  - support for very large files
- save hashes to a data store
- identify duplicate files
- monitor directory / file changes and recompute hashes as needed    


## Learning Objectives

I chose to use / explore the following technologies
- Akka
- Scala Futures
- java.nio
- cake design pattern 

The codebase borrows from [https://github.com/nikolovivan/scala-design-patterns](https://github.com/nikolovivan/scala-design-patterns)
