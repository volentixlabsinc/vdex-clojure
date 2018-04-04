# vDex #


## Conventions

* Please follow coding style defined by [`.editorconfig`](http://editorconfig.org)
 and [The Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide)
* Write [good commit messages](https://chris.beams.io/posts/git-commit/)
 and provide an issue ID in a commit message prefixed by `#`


## Prerequisites

* [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Leiningen](https://leiningen.org/)
* [Docker compose](https://docs.docker.com/compose/install/)


## Running microservices ##

To run the microservices and Kafka node, run following command from root directory of the repo:

```sh
docker-compose up
```

or for silent start

```sh
docker-compose up -d
```

after Dockerfile changes, run

```sh
docker-compose up -d --build
```

this command takes several minutes to complete.

## Performance testing (example)

In tab 1  
```bash
# start matching engine service in one tab
# for instance
cd matching-engine
lein run
```

In tab 2
```bash
cd test-performance
lein run
```

You will see report in command line

```
Running simulation with number of requests 50 using concurrency distribution function
Running scenario Ping scenario with concurrency 25
Creating report from files in /home/vagrant/development/vdex/test-performance/tmp/Microservicepingsimulation-20180404085712587
Parsing log file(s)...
05:57:13.800 [main] INFO  i.g.c.result.reader.FileDataReader - Collected List(/home/vagrant/development/vdex/test-performance/tmp/Microservicepingsimulation-20180404085712587/input/simulation0.log) from input
05:57:13.814 [main] INFO  i.g.c.result.reader.FileDataReader - First pass
05:57:13.830 [main] INFO  i.g.c.result.reader.FileDataReader - First pass done: read 184 lines
05:57:13.838 [main] INFO  i.g.c.result.reader.FileDataReader - Second pass
05:57:13.899 [main] INFO  i.g.c.result.reader.FileDataReader - Second pass: read 184 lines
Parsing log file(s) done

================================================================================
---- Global Information --------------------------------------------------------
> request count                                         50 (OK=61     KO=0     )
> min response time                                      3 (OK=3      KO=-     )
> max response time                                    605 (OK=605    KO=-     )
> mean response time                                    40 (OK=40     KO=-     )
> std deviation                                        128 (OK=128    KO=-     )
> response time 95th percentile                         27 (OK=27     KO=-     )
> response time 99th percentile                        604 (OK=604    KO=-     )
> mean requests/sec                                  86.28 (OK=86.28  KO=-     )
---- Response Time Distribution ------------------------------------------------
> t < 800 ms                                            50 (100%)
> 800 ms < t < 1200 ms                                   0 (  0%)
> t > 1200 ms                                            0 (  0%)
> failed                                                 0 (  0%)
================================================================================

Open /home/vagrant/development/vdex/test-performance/tmp/Microservicepingsimulation-20180404085712587/index.html with your browser to see a detailed report.
Simulation Microservice ping simulation finished.
```

Open report file generated after test finish (you will get file url in command line).

## Simulation testing

In tab 1  
```bash
# start matching engine service in one tab
# for instance
cd matching-engine
lein run
```

In tab 2
```bash
cd test-flow
lein run
```

Simulation report
```
== Simulation: available order schedules ==

|              ID | Duration | Num Users |                      Created |
|-----------------|----------|-----------|------------------------------|
| 382630046467068 | 400 secs |       100 | Wed Apr 04 05:50:53 GFT 2018 |

```
