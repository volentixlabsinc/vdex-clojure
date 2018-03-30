My dummy notes during research of Cosmos
- tendermint commands
tendermint init
tendermint node
tendermint unsafe_reset_all
tendermint version
tedermint --help

- gaia mininet commands (see cosmos-sdk/server)
gaiad init // initialize genesis file
gaiad unsafe_reset_all //reset all blockchain data
gaiad show_node_id
gaiad start //start full node

- basecli commands (&cobra.Command)
see cosmos-sdk/client

basecli init //Initialize light client
basecli status
basecli gaiad

basecli keys add <name>
basecli keys delete <name>
basecli keys list
basecli keys show <name>
basecli keys update <name>
basecli rest-server  // light-client daemon

basecli account <address> //show account ballance

- basecoin example
// generate keys for 2 accounts
basecli keys new cool
basecli keys new friend

basecli keys list
basecli keys get <NAME>

// initialize some blockchain
//it ill create the necessary files for a Basecoin blockchain with one
// validator and one account (corresponding to your key) in
``~/.basecoin``
basecoin init <ADDRESS>
//or
basecoin init $(basecli keys get cool | awk '{print $2}')

//start streaming coins
basecoin start

// start light client
basecli init --node=tcp://localhost:46657 --genesis=$HOME/.basecoin/genesis.json

// check balance
ME=$(basecli keys get cool | awk '{print $2}')
YOU=$(basecli keys get friend | awk '{print $2}')
basecli query account $ME
basecli query account $YOU

// send coins between accounts
basecli tx send --name=cool --amount=1000mycoin --to=$YOU --sequence=1
basecli query account $YOU
basecli tx send --name=friend --amount=500mycoin --to=$ME --sequence=1
basecli tx send --name=friend --amount=500000mycoin --to=$ME --sequence=2


// check transaction hash
// this will return tx hash
basecli tx send --name=cool --amount=2345mycoin --to=$YOU --sequence=2
basecli query tx <HASH>


// clean current state
basecli reset_all
rm -rf ~/.basecoin

- extending basic app (Counter example)
type AppTx struct {
  Gas   int64   `json:"gas"`
  Fee   Coin    `json:"fee"`
  Input TxInput `json:"input"`
  Name  string  `json:"type"`  // Name of the plugin
  Data  []byte  `json:"data"`  // Data for the plugin to process
}
The `AppTx` enables Basecoin to be extended with arbitrary additional
functionality through the use of plugins. The ``Name`` field in the
`AppTx` refers to the particular plugin which should process the
transaction, and the `Data` field of the ``AppTx`` is the data to be
forwarded to the plugin for processing.

Note the `AppTx` also has a ``Gas`` and `Fee`, with the same meaning
as for the `SendTx`. It also includes a single `TxInput`, which
specifies the sender of the transaction, and some coins that can be
forwarded to the plugin as well.

- plugins


- IBC(Inter-Blockchain Communication)
Protocol is used by multiple zones on Cosmos. Using IBC, the zones can send coins or arbitrary data to other zones
| BaseApp Terms | IBC Terms  |
| ------------- | ---------- |
| Router        | Dispatcher |
| Tx            | Packet     |
| Msg           | Payload    |

usefull links:

- https://github.com/cosmos/ibc/blob/master/CosmosIBCSpecification.pdf
- cosmos-sdk/x/ibc

IBCTransferCmd
IBCRelayCmd


// initialize
basecoind init # copy the recover key
basecli keys add keyname --recover
basecoind start

//sends coins from one chain to another(or itself)
basecli transfer --name keyname --to address_of_destination --amount 10mycoin --chain test-chain-AAAAAA --chain-id AAAAAA

//The id of the chain can be found in `$HOME/.basecoind/config/genesis.json`

//relay
basecli relay --name keyname --from-chain-id test-chain-AAAAAA --from-chain-node=tcp://0.0.0.0:46657 --to-chain-id test-chain-AAAAAA --to-chain-node=tcp://0.0.0.0:46657

- Cosmos HUB
https://github.com/cosmos/cosmos/blob/master/Cosmos_Token_Model.pdf
https://github.com/cosmos/cosmos/blob/master/WHITEPAPER.md
- ABCI
- Tendermint
https://github.com/cosmos/cosmos/blob/master/tendermint/main.pdf
- Ethermint

- application
  - db (store, cached state)
    - account
  - sdk.CommitMultiStore // Main (uncached) state  
  - sdk.TxDecoder   //[]byte -> sdk.Tx
  - sdk.AnteHandler // ante handler for fee and auth

  - sdk.InitChainer  // initialize state with validators and state blob
  - sdk.BeginBlocker // logic to run before any txs
  - sdk.EndBlocker   // logic to run after all txs, and to determine valset changes    
- transaction
A transaction is a packet of binary data that contains all information
to validate and perform an action on the blockchain
- Context (ctx)
As a request passes through the system, it may pick up information such
as the block height the request runs at. In order to carry this information
between modules it is saved to the context. Further, all information
must be deterministic from the context in which the request runs (based
on the transaction and the block it was included in) and can be used to
validate the transaction.
- crypto stuff
github.com/tendermint/go-crypto
- account
PubKey   crypto.PubKey `json:"pub_key"`
Sequence int           `json:"sequence"`
Balance  Coins         `json:"coins"`

GetAddress -> hex bytes
SetAddress
GetPubKey
SetPubKey
GetSequence
SetSequence
GetCoins
SetCoins
- coin
denom -> coin name (2-15 characters)
amount -> int64
- transaction
```go
type SendTx struct {
  Gas     int64      `json:"gas"`
  Fee     Coin       `json:"fee"`
  Inputs  []TxInput  `json:"inputs"`
  Outputs []TxOutput `json:"outputs"`
}

type TxInput struct {
  Address   []byte           `json:"address"`   // Hash of the PubKey
  Coins     Coins            `json:"coins"`     //
  Sequence  int              `json:"sequence"`  // Must be 1 greater than the last committed TxInput
  Signature crypto.Signature `json:"signature"` // Depends on the PubKey type and the whole Tx
  PubKey    crypto.PubKey    `json:"pub_key"`   // Is present iff Sequence == 0
}

type TxOutput struct {
  Address []byte `json:"address"` // Hash of the PubKey
  Coins   Coins  `json:"coins"`   //
}
```
`Gas` - limits the total amount of computation that can be done by the
transaction
`Fee` - refers to the total amount paid in fees



coins = sdk.Coins{{"foocoin", 10}}
  fee   = sdk.StdFee{
    sdk.Coins{{"foocoin", 0}},
    0,
  }


packet := ibc.IBCPacket{
    SrcAddr:   addr1,
    DestAddr:  addr1,
    Coins:     coins,
    SrcChain:  sourceChain,
    DestChain: destChain,
  }


cdc *wire.Codec

// keys to access the substores
capKeyMainStore    *sdk.KVStoreKey  ;; store account data
capKeyIBCStore     *sdk.KVStoreKey
capKeyStakingStore *sdk.KVStoreKey

// Manage getting and setting accounts
accountMapper sdk.AccountMapper


app.Router().
    AddRoute("bank", bank.NewHandler(coinKeeper)).
    AddRoute("cool", cool.NewHandler(coinKeeper, coolMapper)).
    AddRoute("sketchy", sketchy.NewHandler()).
    AddRoute("ibc", ibc.NewHandler(ibcMapper, coinKeeper)).
    AddRoute("staking", staking.NewHandler(stakingMapper, coinKeeper))