const fs = require('fs'), vm = require('vm'), assert = require('assert'), path = require('path')
const ux = fs.readFileSync(path.join(__dirname, '../src/pages/index/index.ux'), 'utf8')
const source = ux.match(/<script>([\s\S]*?)<\/script>/)[1].replace(/^import .*$/gm, '').replace('export default', 'module.exports =')
const sandbox = { module: { exports: {} }, console, setTimeout, clearTimeout, brightness: { setKeepScreenOn(o) { o.success() } } }
vm.runInNewContext(source, sandbox)
const base = sandbox.module.exports
function create() {
  const p = Object.assign({}, base, JSON.parse(JSON.stringify(base.private)))
  p.exits = 0; p.$app = { exit() { p.exits++ } }
  return p
}
const touch = (x,y) => ({ touches: [{ clientX:x, clientY:y }] })
let p = create()
assert.equal(p.onBackPress(), true)
assert.equal(p.pageIndex, 0); assert.equal(p.exits, 0)
p.onBackPress(); assert.equal(p.exits, 1)
p.handleSettingsSwipe({ direction:'right' }); assert.equal(p.exits, 1)
p = create(); p.pageIndex = 0
p.startExitGesture(touch(5,80)); p.moveExitGesture(touch(95,85))
p.endExitGesture({ touches:[], changedTouches:[] }); assert.equal(p.exits, 1)
p = create(); p.pageIndex = 0
p.startExitGesture(touch(100,80)); p.moveExitGesture(touch(10,80)); p.pageChanged({ index:1 })
p.endExitGesture({ touches:[], changedTouches:[] }); assert.equal(p.exits, 0); assert.equal(p.pageIndex, 1)
p = create(); p.pageIndex = 0
p.startExitGesture(touch(20,20)); p.moveExitGesture(touch(25,120)); p.endExitGesture({})
p = create(); p.pageIndex = 0; p.confirming = true
p.onBackPress(); assert.equal(p.confirming, false); assert.equal(p.exits, 0)
p.$app.exit = () => { throw new Error('temporary runtime error') }
p.handleSettingsSwipe({direction:'right'}); assert.equal(p.exitRequested, false)
p.$app.exit = () => { p.exits++ }; p.handleSettingsSwipe({direction:'right'}); assert.equal(p.exits, 1)
console.log('Navigation passed: system back, right swipe without end coordinates, left paging, modal dismissal and retry.')
