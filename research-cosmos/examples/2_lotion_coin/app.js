let lotion = require('lotion')
let coin   = require('lotion-coin')

async function main() {
  // initialize lotion-coin client
  let client = coin.client('http://localhost:3000')
  
  // generate key pair
  let privKey = client.generatePrivateKey()
  let pubKey  = client.generatePublicKey(privKey)
  
  // generate address based on public key
  let address = client.generateAddress(pubKey)
  
  // start our app node
  let opts = {
    port: 3000,
    initialState: {
      balances: {
        [address.toString('hex')]: 1000
      },
      nonces: {}
    }
  }
  let genesisKey = await lotion(opts)(coin.handler)
  console.log(`My node is listening on port ${opts.port}`)

  // send some coins to my account
  setTimeout(() => {
    client.send(privKey, {
      amount: 900,
      address:
        '1234123412341234123412341234123412341234123412341234123412341234'
    })
  }, 4000)
}

main()