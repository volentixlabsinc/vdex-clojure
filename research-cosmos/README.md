# Cosmos

The goal of a blockchain is to represent a single state being concurrently edited. In order to avoid conflicts between concurrent edits, it represents the state as a ledger: a series of transformations (transactions) applied to an initial state. The blockchain must allow all connected nodes to agree about which transformations are valid, and their ordering within the ledger.

To accomplish this, a blockchain is composed of three protocols:

- `network protocol`,
- `consensus protocol`,
- `transaction protocol`

The `network protocol` is how nodes in the network tell each other about new transactions, blocks, and other nodes; usually a p2p gossip network.

The `consensus protocol` is the set of rules that nodes should follow to determine which particular ordered set of transformations should be in the ledger at a given moment. In Bitcoin, the chain with the highest difficulty seen by a node is treated as authoritatively correct.

The `transaction protocol` describes what makes transactions valid, and how they should mutate the blockchain's state.

## Usage

```bash
npm install lotion
```

## Usage

- install node 8.x

## Lotion.js

Lotion is a new way to create blockchain apps in JavaScript. It builds on top of Tendermint using the ABCI protocol so it can easily interoperate with other blockchains on the Cosmos Network using IBC

When you're writing a Lotion app, you're only responsible for writing the transaction protocol. Under the hood, Tendermint is handling the consensus and network protocols. When you start your lotion app, a Tendermint node is also started which will handle all of the communication with other nodes running your lotion app

### Lotion.js app architecture
![Architecture](docs/cosmos_app.png)

### 1 - start lotion.js app node

This is example how to launch lotion node and how we can call API from command line.

```bash
# install dependencies
npm install

cd examples/examples/1_start_lotion

# tab 1 - start app
node app.js

# tab 2 - query from command line
sh query_app.sh

## results
# Query state
# {"count":0}

# Create dummy transaction
# {"result":{"check_tx":{"code":0,"data":"","log":"","gas":"0","fee":"0"},"deliver_tx":{"code":0,"data":"","log":"","tags":[]},"hash":"F85EBFB91B6829B6DCA678DF99D8F6472E5CE33B","height":638}}

# Query state
# {"count":1}
```

### 2 - use example Lotion coin

Example custom coin module

```bash
# install Lotion coin
sudo npm install -g lotion      --unsafe-perm=true --allow-root
sudo npm install -g lotion-coin --unsafe-perm=true --allow-root

# tab 1
lcoin init
lcoin start

cd examples/2_lotion_coin
node app.js
```
It will generate:

- credentials.json
My key pair
- initial-state.json
Initial application state
- lotion-data folder
Folder with genesis file, validators and internal data

### 3 - Lotion wallet

Example wallet usage

```bash

cd examples/3_lotion_wallet

npm install

# tab 1 - start app
node app.js

# tab 2 - run wallet
# update GCI addres in wallet-usage.js
node wallet-usage.js
```

### https://github.com/llSourcell/sirajcoin

Some coin implementation with simple wallet using lotion.js and Electron 

## Interesting libraries

### Node

- `SHA` on pure JavaScript 
https://www.npmjs.com/package/sha.js
- native bindings to bitcoin-core/secp256k1
https://www.npmjs.com/package/secp256k1
- Fast elliptic-curve cryptography in a plain javascript implementation
https://github.com/indutny/elliptic
- Search for a key across multiple discovery networks and find peers who answer.
Currently searches across and advertises on the Bittorrent DHT, centralized DNS servers and Multicast DNS simultaneously.Uses the bittorrent-dht and dns-discovery modules.
https://www.npmjs.com/package/discovery-channel
- A tcp/utp server that auto announces itself using discovery-channel. Basically a server-only version of discovery-swarm
https://www.npmjs.com/package/discovery-server
- A network swarm that uses discovery-channel to find peers
https://github.com/mafintosh/discovery-swarm
- Proto Buffer serializtion
https://www.npmjs.com/package/protobufjs
- A JS RPC client for Tendermint nodes.
https://www.npmjs.com/package/tendermint

### Java

- https://github.com/jTendermint
- https://github.com/jTendermint/crypto
- https://github.com/jTendermint/MerkleTree
- https://github.com/jTendermint/jabci
 
## Run IBC(Inter-Blockchain Communication) example on Vagrant machine

```bash
wget https://dl.google.com/go/go1.10.1.linux-amd64.tar.gz
sudo tar -xvf go1.10.1.linux-amd64.tar.gz
sudo mv go /usr/local

echo 'export PATH=$PATH:/usr/local/go/bin:/home/vagrant/go/bin' >> /home/vagrant/.bash_profile
echo 'export GOPATH=/home/vagrant/go' >> /home/vagrant/.bash_profile

echo 'export PATH=$PATH:/usr/local/go/bin:/home/vagrant/go/bin' >> /home/vagrant/.bashrc
echo 'export GOPATH=/home/vagrant/go' >> /home/vagrant/.bashrc

mkdir -p /home/vagrant/go/bin
mkdir -p /home/vagrant/go/src/github.com/cosmos
//ln -s /vagrant /home/vagrant/go/src/github.com/cosmos/cosmos-sdk
    
# verify installation
go version
go env


go get github.com/tendermint/tendermint/cmd/tendermint

# verify tendermint installation
tendermint --help
tendermint version

go get github.com/Masterminds/glide
cd $GOPATH/src/github.com/tendermint/tendermint
glide install
go install ./cmd/tendermint

make get_tools
make get_vendor_deps
make install

go get -u github.com/cosmos/cosmos-sdk
cd $GOPATH/src/github.com/cosmos/cosmos-sdk
git pull origin master
cd $GOPATH/src/github.com/cosmos/cosmos-sdk
make get_tools
make get_vendor_deps
# build basecoind and basecli (basecoin module)
make build

# fix that sdk binaries aren't installed but only built
# see https://github.com/cosmos/cosmos-sdk/pull/733/files
cp $GOPATH/src/github.com/cosmos/cosmos-sdk/build/basecoind $GOPATH/bin
cp $GOPATH/src/github.com/cosmos/cosmos-sdk/build/basecli $GOPATH/bin


mkdir ~/.ibcdemo
rm -rf ~/.ibcdemo/

export BCHOME1_CLIENT=~/.ibcdemo/chain1/client
export BCHOME1_SERVER=~/.ibcdemo/chain1/server
export CHAINID1="test-chain-1"
export PORT_PREFIX1=1234
export RPC_PORT1=${PORT_PREFIX1}7
alias basecli1="basecli --home $BCHOME1_CLIENT"
alias basecoin2="basecoin --home $BCHOME2_SERVER"

export BCHOME2_CLIENT=~/.ibcdemo/chain2/client
export BCHOME2_SERVER=~/.ibcdemo/chain2/server
export CHAINID2="test-chain-2"
export PORT_PREFIX2=2345
export RPC_PORT2=${PORT_PREFIX2}7
alias basecli2="basecli --home $BCHOME2_CLIENT"
alias basecoin1="basecoind --home $BCHOME1_SERVER"

# Setup Chain 1 ================================
basecli1 keys add money
# us throwing-this-key-away as an pass phrase
basecli1 keys add gotnone
# us throwing-this-key-away as an pass phrase
export MONEY=$(basecli1 keys get money | awk '{print $2}')
export GOTNONE=$(basecli1 keys get gotnone | awk '{print $2}')

basecoin1 init --chain-id $CHAINID1 $MONEY

sed -ie "s/4665/$PORT_PREFIX1/" $BCHOME1_SERVER/config.toml
basecoin1 start &> basecoin1.log &

# Now we can attach the client to the chain and verify the state. The 
# first account should have money, the second none:
basecli1 init --node=tcp://localhost:${RPC_PORT1} --genesis=${BCHOME1_SERVER}/genesis.json
basecli1 query account $MONEY
basecli1 query account $GOTNONE

# SEtup Chain 2 ================================
basecli2 keys add moremoney
basecli2 keys add broke
MOREMONEY=$(basecli2 keys get moremoney | awk '{print $2}')
BROKE=$(basecli2 keys get broke | awk '{print $2}')
    
basecoin2 init --chain-id $CHAINID2 $(basecli2 keys get moremoney | awk '{print $2}')

sed -ie "s/4665/$PORT_PREFIX2/" $BCHOME2_SERVER/config.toml
basecoin2 start &> basecoin2.log &

# The first account should have money, the second none:
basecli2 init --node=tcp://localhost:${RPC_PORT2} --genesis=${BCHOME2_SERVER}/genesis.json
basecli2 query account $MOREMONEY
basecli2 query account $BROKE

# Connect these chains ==============================

RELAY_KEY=$BCHOME1_SERVER/key.json
RELAY_ADDR=$(cat $RELAY_KEY | jq .address | tr -d \")

basecli1 tx send --amount=100000mycoin --sequence=1 --to=$RELAY_ADDR--name=money
basecli1 query account $RELAY_ADDR

basecli2 tx send --amount=100000mycoin --sequence=1 --to=$RELAY_ADDR --name=moremoney
basecli2 query account $RELAY_ADDR

# start the relay process.
basecoin relay init --chain1-id=$CHAINID1 --chain2-id=$CHAINID2 \
  --chain1-addr=tcp://localhost:${RPC_PORT1} --chain2-addr=tcp://localhost:${RPC_PORT2} \
  --genesis1=${BCHOME1_SERVER}/genesis.json --genesis2=${BCHOME2_SERVER}/genesis.json \
  --from=$RELAY_KEY

basecoin relay start --chain1-id=$CHAINID1 --chain2-id=$CHAINID2 \
  --chain1-addr=tcp://localhost:${RPC_PORT1} --chain2-addr=tcp://localhost:${RPC_PORT2} \
  --from=$RELAY_KEY &> relay.log &

# Sending cross-chain payments =================

# Here's an empty account on test-chain-2
basecli2 query account $BROKE

# Let's send some funds from test-chain-1
basecli1 tx send --amount=12345mycoin --sequence=2 --to=test-chain-2/$BROKE --name=money

# give it time to arrive...
sleep 2
# now you should see 12345 coins!
basecli2 query account $BROKE


# ??????????
tendermint init
tendermint node --proxy_app=dummy  # TCP or UNIX socket address of the ABCI application

```