# libvtx


## Developing

### Setup

When you first clone this repository, run:

```sh
lein duct setup
```

This will create files for local configuration, and prep your system
for the project.

### Environment

To begin developing, start with a REPL.

```sh
lein repl
```

Then load the development environment.

```clojure
user=> (dev)
:loaded
```

Run `go` to prep and initiate the system.

```clojure
dev=> (go)
:duct.server.http.jetty/starting-server {:port 3000}
:initiated
```

By default this creates a web server at <http://localhost:3000>.

When you make changes to your source files, use `reset` to reload any
modified files and reset the server.

```clojure
dev=> (reset)
:reloading (...)
:resumed
```

### Usage

#### Transaction

```clojure
(require '[libvtx.transaction :as transaction])
```

`send-transaction` creates a transaction. It takes 2 parameters - db spec and a map with transaction data and returns newly created transaction.

```clojure
(def test-transaction {:from-address "from-address" 
                       :to-address "to-address" 
                       :amount "10" 
                       :token-address "token-address"
                       :message "message"})

(transaction/send-transaction db-spec test-transaction)
```

Message key-value pair is not mandatory.

`receive-transactions` takes 3 parameters - db spec, address and token address (not mandatory) and returns list of transactions filtered by to-address and token address (not mandatory) ordered by creation timestamp.

```clojure
(transaction/receive-transaction db-spec "to-address" "token-address")
```

`mempool-transaction` takes 3 parameters - db spec, interval in which to check for new transactions and third parameter shouldn't be used directly, it's used by scheduler that runs this function periodically. The function transfers balance and removes transaction from mempool. It' supposed to run each 5 minutes (configurable) when you run libVTX service.
For testing purposes, pass `0` as interval and the function will process all unprocessed entries immediately.

```clojure
(transaction/mempool-transaction db-spec 0 nil)
```

`transaction-confirmations` takes 2 or 3 parameters - db spec, transaction id and block time which defaults to 120 seconds if not provided. The function simulates getting confirmations from gateways. For testing purposes, use block time `1` or some other small number so you don't have to wait too long before confirmations start to add up.

```clojure
(transaction/transaction-confirmations db-spec transaction-id 1) 
```

#### Balance

```clojure
(require '[libvtx.balance :as balance])
```

`read-or-create-balance` takes 3 parameters - db spec, address and token address. It checks the balance table for balance on given address and if it's not found, it creates a new empty balance. Returns the balance.

```clojure
(balance/read-or-create-balance db-spec address token-address)
```

#### Token

```clojure
(require '[libvtx.token :as token])
```

`create-token` takes 2 parameters - db spec and map with token data. The function cerates a new token and if token data contains :pairs-with key, new pair is created for each address in the pairs-with vector.

```clojure
(def token {:address "an-address"
            :name "BTC"
            :precision "18"
            :pairs-with ["another-address"]})

(token/create-token db-spec token) 
```

#### Deposit

```clojure
(require '[libvtx.deposit :as deposit])
```

`create-deposit-address` takes 3 parameters - db spec, token address and receiver address. It creates a new address (random string) and returns it.

```clojure
(deposit/create-deposit-address db-spec token-address receiver-address)
```

`check-deposit-status` takes 3 parameters - db spec, deposit address and token address. Returns `true` if balance on deposit address is greater than `0`, otherwise retuns `false` and creates new balance if it doesn't exist.

```clojure
(deposit/check-deposit-status db-spec deposit-address token-address)
```

`withdraw-tokens` takes 3 parameters - db spec, address and token address. It gets the deposit of provided address, then it gets balance for provided address and send a transaction where `:from-address` is provided address, `:to-address` is receiver address from deposit, `:amount` is from balance check on provided address and `:token-address` is provided token address. Returns newly created transaction.

```clojure
(deposit/withdraw-tokens db-spec address token-address)
```

### Testing

Testing is fastest through the REPL, as you avoid environment startup
time.

```clojure
dev=> (test)
...
```

But you can also run tests through Leiningen.

```sh
lein test
```

## Legal

Copyright © 2018 FIXME
