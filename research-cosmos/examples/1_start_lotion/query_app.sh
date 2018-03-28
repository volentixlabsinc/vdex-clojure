#!/bin/bash
echo "Query state"
curl http://localhost:3000/state
echo "\n\nCreate dummy transaction"
curl http://localhost:3000/txs -d '{ "nonce": 0 }'
echo "\n\nQuery state"
curl http://localhost:3000/state

echo "\n\nQuery info"
#curl http://localhost:3000/info

## results
# Query state
# {"count":0}

# Create dummy transaction
# {"result":{"check_tx":{"code":0,"data":"","log":"","gas":"0","fee":"0"},"deliver_tx":{"code":0,"data":"","log":"","tags":[]},"hash":"F85EBFB91B6829B6DCA678DF99D8F6472E5CE33B","height":638}}

# Query state
# {"count":1}