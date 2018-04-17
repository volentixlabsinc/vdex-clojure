import { DecodedLogEvent, ZeroEx } from '0x.js';
import { BigNumber } from '@0xproject/utils';
import * as Web3 from 'web3';

// Number of decimals to use (for ETH and ZRX)
const DECIMALS = 18;

// TestRPC on default port 8545
const TESTRPC_NETWORK_ID = 50;
const provider = new Web3.providers.HttpProvider('http://localhost:8545');
const configs = { networkId: TESTRPC_NETWORK_ID };

const zeroEx = new ZeroEx(provider, configs);


const showBalances = async (makerAddr: string,
                            takerAddr: string,
                            ZRX_ADDRESS: string,
                            WETH_ADDRESS: string) => {
    const balance_makerZRX  = await zeroEx.token.getBalanceAsync(ZRX_ADDRESS,  makerAddr);
    const balance_takerZRX  = await zeroEx.token.getBalanceAsync(ZRX_ADDRESS,  takerAddr);

    const balance_makerWETH = await zeroEx.token.getBalanceAsync(WETH_ADDRESS, makerAddr);
    const balance_takerWETH = await zeroEx.token.getBalanceAsync(WETH_ADDRESS, takerAddr);

    const balance_makerETH = await (zeroEx as any)._web3Wrapper.getBalanceInWeiAsync(makerAddr);
    const balance_takerETH = await (zeroEx as any)._web3Wrapper.getBalanceInWeiAsync(takerAddr);

    console.log('Maker ZRX: '  + ZeroEx.toUnitAmount(balance_makerZRX,  DECIMALS).toString());
    console.log('Taker ZRX: '  + ZeroEx.toUnitAmount(balance_takerZRX,  DECIMALS).toString());

    console.log('Maker WETH: ' + ZeroEx.toUnitAmount(balance_makerWETH, DECIMALS).toString());
    console.log('Taker WETH: ' + ZeroEx.toUnitAmount(balance_takerWETH, DECIMALS).toString());

    console.log('Maker ETH: ' + ZeroEx.toUnitAmount(balance_makerETH, DECIMALS).toString());
    console.log('Taker ETH: ' + ZeroEx.toUnitAmount(balance_takerETH, DECIMALS).toString());

    console.log("\n");
}

// internally 0x makes unlimited allowance as
// UNLIMITED_ALLOWANCE_IN_BASE_UNITS: new BigNumber(2).pow(256).minus(1)
// Sets the 0x proxy contract's allowance to a unlimited number of a tokens' baseUnits on behalf
// * of an owner address
const makeAllowances = async (makerAddr: string,
                              takerAddr: string,
                              ZRX_ADDRESS: string,
                              WETH_ADDRESS: string) => {
    // Unlimited allowances to 0x proxy contract for maker
    const setMakerAllowTxHash = await zeroEx.token.setUnlimitedProxyAllowanceAsync(ZRX_ADDRESS, makerAddr);
    await zeroEx.awaitTransactionMinedAsync(setMakerAllowTxHash);

    // Unlimited allowances to 0x proxy contract for taker
    const setTakerAllowTxHash = await zeroEx.token.setUnlimitedProxyAllowanceAsync(WETH_ADDRESS, takerAddr);
    await zeroEx.awaitTransactionMinedAsync(setTakerAllowTxHash);
    // console.log('Taker and maker allowance mined...');
}

const depositWETH = async (makerAddr: string,
                           takerAddr: string,
                           ZRX_ADDRESS: string,
                           WETH_ADDRESS: string) => {
    // Deposit WETH
    const ethAmount = new BigNumber(1);
    const ethToConvert = ZeroEx.toBaseUnitAmount(ethAmount, DECIMALS); // Number of ETH to convert to WETH

    const convertEthTxHash = await zeroEx.etherToken.depositAsync(WETH_ADDRESS, ethToConvert, takerAddr);
    await zeroEx.awaitTransactionMinedAsync(convertEthTxHash);
    // console.log(`${ethAmount} ETH -> WETH conversion mined...`);
}

const putOrder = async (makerAddr: string,
                        takerAddr: string,
                        ZRX_ADDRESS: string,
                        WETH_ADDRESS: string,
                        EXCHANGE_ADDRESS: string) => {
    // Generate order
    const order = {
        maker:        makerAddr,
        taker:        ZeroEx.NULL_ADDRESS,
        feeRecipient: ZeroEx.NULL_ADDRESS,

        makerTokenAddress: ZRX_ADDRESS,
        takerTokenAddress: WETH_ADDRESS,
        makerTokenAmount: ZeroEx.toBaseUnitAmount(new BigNumber(0.2), DECIMALS), // Base 18 decimals
        takerTokenAmount: ZeroEx.toBaseUnitAmount(new BigNumber(0.3), DECIMALS), // Base 18 decimals

        exchangeContractAddress: EXCHANGE_ADDRESS,
        salt: ZeroEx.generatePseudoRandomSalt(),
        makerFee: new BigNumber(0),
        takerFee: new BigNumber(0),
        expirationUnixTimestampSec: new BigNumber(Date.now() + 3600000), // Valid for up to an hour
    };

    // Create orderHash
    const orderHash = ZeroEx.getOrderHashHex(order);

    // Signing orderHash -> ecSignature
    const shouldAddPersonalMessagePrefix = false;
    const ecSignature = await zeroEx.signOrderHashAsync(orderHash, makerAddr, shouldAddPersonalMessagePrefix);

    // Appending signature to order
    const signedOrder = { ...order, ecSignature, };

    // Verify that order is fillable
    await zeroEx.exchange.validateOrderFillableOrThrowAsync(signedOrder);

    return signedOrder;
}

const fillOrder = async (takerAddr: string, signedOrder: any) => {

    // Try to fill order
    const shouldThrowOnInsufficientBalanceOrAllowance = true;
    const fillTakerTokenAmount = ZeroEx.toBaseUnitAmount(new BigNumber(0.1), DECIMALS);

    // Filling order
    const txHash = await zeroEx.exchange.fillOrderAsync(
        signedOrder,
        fillTakerTokenAmount,
        shouldThrowOnInsufficientBalanceOrAllowance,
        takerAddr,
    );

    // Transaction receipt
    const txReceipt = await zeroEx.awaitTransactionMinedAsync(txHash);
    // console.log('FillOrder transaction receipt: ', txReceipt);
}

const mainAsync = async () => {
    // Smart contracts addressess
    const WETH_ADDRESS     = zeroEx.etherToken.getContractAddressIfExists() as string;
    const ZRX_ADDRESS      = zeroEx.exchange.getZRXTokenAddress();
    const EXCHANGE_ADDRESS = zeroEx.exchange.getContractAddress();

    // All test accounts available
    const accounts = await zeroEx.getAvailableAddressesAsync();
    console.log('=== all test accounts ===');
    console.log(accounts);
    console.log('\n');

    // Set our addresses
    const [makerAddress, takerAddress] = accounts;
    console.log('Maker ' + makerAddress);
    console.log('Taker ' + takerAddress);

    console.log('\nBalances at start');
    await showBalances(makerAddress, takerAddress, ZRX_ADDRESS, WETH_ADDRESS);

    // required to set only once
    await makeAllowances(makerAddress, takerAddress, ZRX_ADDRESS, WETH_ADDRESS);
    console.log('Balances after set allowance');
    await showBalances(makerAddress, takerAddress, ZRX_ADDRESS, WETH_ADDRESS);

    // required to set only once
    await depositWETH(makerAddress, takerAddress, ZRX_ADDRESS, WETH_ADDRESS);
    console.log('Balances after deposit WETH');
    await showBalances(makerAddress, takerAddress, ZRX_ADDRESS, WETH_ADDRESS);

    const signedOrder = await putOrder(makerAddress, takerAddress, ZRX_ADDRESS, WETH_ADDRESS, EXCHANGE_ADDRESS);

    await fillOrder(takerAddress, signedOrder);
    console.log('Balances after fill order');
    await showBalances(makerAddress, takerAddress, ZRX_ADDRESS, WETH_ADDRESS);
};

mainAsync().catch(console.error);
