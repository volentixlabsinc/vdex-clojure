##

- Basic example
https://codesandbox.io/s/1qmjyp7p5j

- Trader widger WIP 
https://github.com/0xProject/0x-monorepo/pull/472

- Add marketBuyOrders WIP
https://github.com/0xProject/0x-monorepo/pull/486/files


- register in https://infura.io/
- save infura token value
- install metamask 

- Ox

export FAUCET_ENVIRONMENT=development
export INFURA_API_KEY=$INFURA_API_KEY
export DISPENSER_ADDRESS=0x5409ed021d9299bf6814279a6a1411a7e866a631
export DISPENSER_PRIVATE_KEY=f2f48ee19680706196e2e339e5da3491186e0c4c5030670656b0e0164837257d

const provider = new Web3.providers.HttpProvider('http://localhost:8545');



```js
import * as Web3ProviderEngine from 'web3-provider-engine';
import * as RPCSubprovider from 'web3-provider-engine/subproviders/rpc';

import { InjectedWeb3Subprovider } from '@0xproject/subproviders';
import { Web3Wrapper } from '@0xproject/web3-wrapper';
import { ZeroEx } from '0x.js';

const KOVAN_NETWORK_ID = 3;
// Create a Web3 Provider Engine
const providerEngine = new Web3ProviderEngine();
// Compose our Providers, order matters
// Use the InjectedWeb3Subprovider to wrap the browser extension wallet
// All account based and signing requests will go through the InjectedWeb3Subprovider
providerEngine.addProvider(new InjectedWeb3Subprovider(window.web3.currentProvider));
// Use an RPC provider to route all other requests

// provider
// providerEngine.addProvider(new RPCSubprovider({ rpcUrl: 'http://localhost:8545' }));
providerEngine.addProvider(new RedundantRPCSubprovider(['http://localhost:8545', 'https://ropsten.infura.io/'));
providerEngine.start();

// Optional, use with 0x.js
const zeroEx = new ZeroEx(providerEngine, { networkId: KOVAN_NETWORK_ID });
// Get all of the accounts through the Web3Wrapper
const web3Wrapper = new Web3Wrapper(providerEngine);
const accounts = await web3Wrapper.getAvailableAddressesAsync();
console.log(accounts);


// my tokens
const tokens = await this.props.zeroEx.tokenRegistry.getTokensAsync()
console.log (tokens)


```

- trade example

```
const exchangeAddress           = artifacts.ExchangeArtifact.networks[  TESTRPC_NETWORK_ID].address;
const tokenTransferProxyAddress = artifacts.TokenTransferProxy.networks[TESTRPC_NETWORK_ID].address;
const zrxTokenAddress           = artifacts.ZRXArtifact.networks[       TESTRPC_NETWORK_ID].address;
const etherTokenAddress         = artifacts.EtherTokenArtifact.networks[TESTRPC_NETWORK_ID].address;

const forwarderArgs = [exchangeAddress, tokenTransferProxyAddress, etherTokenAddress, zrxTokenAddress];
const forwarder = await deployer.deployAndSaveAsync('Forwarder', forwarderArgs);

const forwarderInitializeGasEstimate = new BigNumber(90000);
await forwarder.initialize.sendTransactionAsync({ from: owner, gas: forwarderInitializeGasEstimate });


```




## orderWatcher
tool requires a backing Ethereum node setup to have a decent representation of the mempool

## Relayer
relayer wanting to prune their orderbook of any orders that have become unfillable

## Mempool
- trade settlement happens on-chain
- trades are first submitted to the Ethereum network mempool before being mined into blocks

## Parity 
- https://github.com/paritytech/parity
- Fast, light, robust Ethereum implementation. https://parity.io
- built using Rust
- comes with a built-in wallet

## Ethereum Whisper
## Ethereum Truffle


npm install -g ethereumjs-testrpc
# download testRPC snapshot
# unpack
testrpc \
--networkId 50 \
-p 8545 \
--db ./0x_testrpc_snapshot \
-m "concert load couple harbor equip island argue ramp clarify fence smart topic"
