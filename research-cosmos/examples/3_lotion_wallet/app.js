let lotion = require('lotion')
let coins  = require('coins')

let app = lotion({ initialState: {} })

async function main() {
  app.use(coins({
    name: 'kittycoin',
    initialBalances: {
      // map addresses to balances
      '04oDVBPIYP8h5V1eC1PSc/JU6Vo': 10,
      'OGccsuLV2xuoDau1XRc6hc7uO24': 20
    }
  }))

  let { GCI } = await app.listen(3000)
  console.log(`App GCI ${GCI}`)
}
main()