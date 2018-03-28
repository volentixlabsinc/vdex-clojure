let lotion = require('lotion')
let coins  = require('coins')

// PUT HEAR !!!! your app global chain indentifier
// need run app first, it will display it in console
let APP_GCI = null //"9d022500c3a590f7a639204074f8f846f5c739c1463a92698f05574ab9c6186b"

async function main() {

  let client = await lotion.connect(APP_GCI)

  let wallet = coins.wallet(client)

  // wallet methods:
  let address = wallet.getAddress()
  
  let balance = await wallet.getBalance()
  console.log(balance) // 20

  let result = await wallet.send('04oDVBPIYP8h5V1eC1PSc/JU6Vo', 5)
  console.log(result) // { height: 42 }
}
main()