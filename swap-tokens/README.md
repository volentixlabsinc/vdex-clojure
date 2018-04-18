# swap_tokens

Initial prototype for working with 0x using cljs.  

## Developing

### Setup

When you first clone this repository, run:

```sh
lein duct setup
```

This will create files for local configuration, and prep your system
for the project.

### Environment

#### REPL

Download dependencies required for Node.js modules

```sh
lein deps
```
To begin developing, start with a REPL.

```sh
lein repl
```

#### Dev backend

Then load the development environment.

```clojure
user=> (dev)
:loaded
```

Run `go` to prep and initiate the system.

```clojure
dev=> (go)
:duct.server.http.jetty/starting-server {:port 5000}
:initiated
```

By default this creates a web server at <http://localhost:5000>.

When you make changes to your clj (Clojure) source files, use `reset` to reload any
modified files and reset the server.

```clojure
dev=> (reset)
:reloading (...)
:resumed
```

#### UI

Start CLJS UI. It will start figwheel server that will continously compile cljs files and refresh browser UI

```clojure
dev=> (ui)
```

You can also stop the UI

```clojure
dev=> (ui-stop)
```

#### CLJS REPL

If you want sent CLJS commands one by one directy from editor to change UI state you can start CLJS REPL

```clojure
dev=> (cljs-repl)
```

### Testing

#### Backend tests

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
