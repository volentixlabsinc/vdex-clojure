let app = require('lotion')({
  initialState: { count: 0 }
})

// change our node state
app.use((state, tx) => {
  state.count++
})

app.listen(3000)