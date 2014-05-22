JTPG
====

Jepsen testing various Erlang process-group implementations.

Reasoning
---------

The Erlang standard library implements a distributed process grouping
module. This repo intends to discover the semantics involved when the
individual nodes which make up a cluster are cut-off from each other.

Tooling
-------

`jtpg` uses Jepsen's tooling to instrument an easy to implement and
repeatable set of tests.

Method
------

* Five Erlang nodes are used.
* Each node always attempt to reconnect to its neighbours.
* Jepsen generates a series of integers which are used as a name for a
  simple pid.
* The cluster is put through varying splits including:
  * 3/5 split.
  * bridge split.
  * Total split.
* The writes are paused and the cluster is allowed time to heal.
* A random node is contacted to retrieve all known pids.
* If this list of pids (and the number of pids under that name) is the
  same as the ACK'd writes, then pg2 has successfully repaired
  distributed state following a netsplit.
