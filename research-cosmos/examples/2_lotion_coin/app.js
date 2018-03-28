let lotion  = require('lotion')
let coin    = require('lotion-coin')
// let { handler, client } = require('lotion-coin')

async function main() {
  // initialize lotion-coin client
  let client = coin.client('http://localhost:3000')
  
  // generate key pair
  let privKey = client.generatePrivateKey()
  let pubKey  = client.generatePublicKey(privKey)
  
  // generate address based on public key
  let address    = client.generateAddress(pubKey)
  let addres_hex = address.toString('hex')
  
  console.log(`My address hex ${addres_hex}`)

  // start our app node
  let opts = {
    port: 3000,
    initialState: {
      balances: {
        [addres_hex]: 1000
      },
      nonces: {}
    }
  }

  let app = lotion(opts)
  await app.use((state, tx) => { coin.handler(state, tx) })
  console.log(`My app node is listening on port ${opts.port}`)

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