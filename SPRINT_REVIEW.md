# SPRINTS (produced value)

## week 4

- libVTX abstraction implementation
Tokens abstraction https://github.com/Volentix/vdex/pull/21
- CircleCI setup
https://github.com/Volentix/vdex/pull/26

## week 3
- basic flow using Kafka as starting point for learning it and use it. We want to use it for communication between components.
https://github.com/Volentix/vdex/pull/14
- research and prototype for flow testing (simulation testing). Simple example how to test flow correctness.
https://github.com/Volentix/vdex/pull/13
- research about Cosmos
https://github.com/Volentix/vdex/pull/18
- research about 0x
https://github.com/Volentix/vdex/pull/19
- research about EOS. 

Cosmos study:
- there is a still heavy core sdk development. Developers are still searching for a good way how to make good modularity
- examples are still often broken or have bugs
- Inter blockchain communication example (IBC) is still in development
https://github.com/cosmos/cosmos-sdk/pull/707
https://github.com/cosmos/cosmos-sdk/pull/772
- I am not able to go through example from https://github.com/cosmos/cosmos-sdk/blob/master/docs/ibc.rst
- IBC tutorial needs to be updated. I haven't got any tips,answers from Cosmos development channel yet
- having broken examples are dissapointing
- there are some basic examples of https://github.com/keppel/lotion - JS framework that allows to build some js apps on top of Cosmos.
- they mention hybrid DEX as an good path https://github.com/cosmos/cosmos/blob/master/DEX.md
- nice: cosmos sdk is written in Go. Easier to learn, read code, launch, find developers in comparison to C projects.
- nice: https://github.com/tendermint/tendermint used to have consensus between nodes was tested using jepsen framework. 
I haven't seen any this kind of testing in other solutions yet. :smile:
- nice: https://github.com/tendermint/abci is an interface that defines the boundary between the replication engine (the blockchain), and the state machine (the application) Seems to be easy extendable (in Go)
and seems to be easy to extend (basic protocol for communication)
- nice: they have some module to work with Ethereum https://github.com/tendermint/ethermint
- nice: idea of peg-zones -> bridges for wrapping one token into another for instance ETH<->CETH
- whole idea is promising, needs to wait for stabilize the Cosmos SDK (edited)

0x study:
- nice: ready to use code from https://github.com/district0x built on top of 0x (ClojureScript)
- nice: ready to learn from decentralized job search/posting application (ClojureScript) build on top of 0x 
https://github.com/district0x/ethlance
- nice: (work in progress) they are building some trade widget example https://github.com/0xProject/0x-monorepo/pull/472
- nice: they want to be some kind of framework, general solution to build on top off
- nice: (work in progress) some interesting examples around trading topic
https://github.com/0xProject/0x-monorepo/pull/486
https://github.com/0xProject/0x-monorepo/pull/490
https://github.com/0xProject/0x-monorepo/pull/492
- nice: (work in progress) some interesting example of offline analysis
https://github.com/0xProject/0x-monorepo/pull/314/files
- nice: (work in progrss) interfaces for token standard abstraction
https://github.com/0xProject/0x-monorepo/pull/353
- very basic example available online in codesandbox https://codesandbox.io/s/1qmjyp7p5j
- quite nice: they try to use Typescript
- very good source to learn from/use existing code
- nice: very good example of relayer https://github.com/NoteGio/openrelay in Go. Well designed.
Good source to learn from. Code is split into around 20 microservices.
- very promising. Might be very good to start with.

EOS study:
- to early to use it
- it might be useble after couple of months
- not able to run examples
- hard to reason about the code

## week 2

- basic setup for Kafka with Docker. Starting point to learn and examine
https://github.com/Volentix/vdex/pull/1
- research and start examine LMAX disruptor 
https://github.com/Volentix/vdex/pull/2
- research and prototype for testing performance. Simple example with hitting one service.
https://github.com/Volentix/vdex/pull/10

## week 1

- created simple microservices structure, each microservices can be ran by ```lein run```. When you go to ```localhost:[port]``` you will see `Microservice` welcome text
https://github.com/Volentix/vdex/commit/b4a32bdc593284e571e9c5bb638cc7cd97e7b5fb
- save/load data using JAVA NIO and fressian serialization format. It will be used for caching important data between components start/stop
https://github.com/Volentix/vdex/commit/ec398f32b7c6bb24870dcb6180f34799b692798f
- dockerize project. We are able to run project in Docker :D
https://github.com/Volentix/vdex/pull/1
- get some knowledge about exchanges based on `bisq` https://github.com/bisq-network, bitshares https://github.com/bitshares
