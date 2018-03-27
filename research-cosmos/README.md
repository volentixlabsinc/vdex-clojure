# Cosmos examples

## Usage

```bash
npm install lotion
```

## Examples

### 1 - start lotion
```bash
# start app
sh examples/examples/1_start_lotion/start_app.sh
sh examples/examples/1_start_lotion/query_app.sh

## results
# Query state
# {"count":0}

# Create dummy transaction
# {"result":{"check_tx":{"code":0,"data":"","log":"","gas":"0","fee":"0"},"deliver_tx":{"code":0,"data":"","log":"","tags":[]},"hash":"F85EBFB91B6829B6DCA678DF99D8F6472E5CE33B","height":638}}

# Query state
# {"count":1}
```