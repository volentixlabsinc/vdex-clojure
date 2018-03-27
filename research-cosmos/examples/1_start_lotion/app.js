let app = require('lotion')({
  initialState: { count: 0 }
})

app.use((state, tx) => {
  state.count++
})

app.listen(3000)