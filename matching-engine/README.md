# matching-engine

Matching orders from orders books 

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

## Research -> Engine state

### Market

* `name` - each currencies pair is name of the market (combination of `base-currency` into `market-currency`) for instance `"BTC-LTC"`; it represents all exchangesfrom `base-currency` into `market-currency`
* `open-buy-orders-cnt`
* `open-sell-orders-cnt`

### Order books

* each currencies pair (market) has its own order book, for instance "BTC-LTC"
* each order book has two sides - `bids` (buying orders) and `asks` (selling orders)

### Order

* `account-id` - uuid
* `order-id` - uuid
* `public-key-ring`
* `sender-ip`
* `sender-ip-port`
* `market` - "BTC-SHLD" 
* `type` - "limit-buy", "limit-sell", "stop-loss", "market"
* `quantity` - how much I order/sell
* `quantity-remaining` - remaining quantity to sell/order
* `limit`
* `price` - price tha I am intrested in
* `price-per-unit`
* `commmision`
* `total`
* `nonce` - (now)
* `opened-at`
* `closed-at`
* `signature` ?????

### Currency

* `name`
* `name-long`
* `transaction-fee`
* `minimum-confirmation`

### Trade/transaction

* `order-type` - `sell` or `buy`
* `accont-id`
* `address`
* `order-id` - uuid value
* `market` - "BTC-SHLD" 
* `quantity` - amount of base currency
* `price` 
* `total`
* `created-at`

### Address

* `uuid`
* `address` - as string
* `network` - blockchain type, "Bitcoin", ...

## Legal

Copyright © 2018 FIXME
