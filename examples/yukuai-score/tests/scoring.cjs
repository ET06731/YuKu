const fs = require('fs'), vm = require('vm'), assert = require('assert'), path = require('path')
const ux = fs.readFileSync(path.join(__dirname, '../src/pages/index/index.ux'), 'utf8')
let now = 0, nextId = 1, timers = new Map()
function setTimer(fn, ms) { const id = nextId++; timers.set(id, { fn, at: now + ms }); return id }
function clearTimer(id) { timers.delete(id) }
function advance(ms) { const end = now + ms; for (;;) { const due = [...timers].filter(([, t]) => t.at <= end).sort((a,b) => a[1].at-b[1].at)[0]; if (!due) break; now = due[1].at; timers.delete(due[0]); due[1].fn() } now = end }
const sandbox = {module: {exports: {}}, setTimeout: setTimer, clearTimeout: clearTimer}
vm.runInNewContext(ux.match(/<script>([\s\S]*?)<\/script>/)[1].replace('export default', 'module.exports ='), sandbox)
const base = sandbox.module.exports
function create(){return Object.assign({}, base, JSON.parse(JSON.stringify(base.private)))}
function score(p,a,b){while(p.scoreA<a || p.scoreB<b){const before=p.scoreA+p.scoreB;if(p.scoreA<a)p.changeScore('A',1);if(p.scoreB<b)p.changeScore('B',1);assert.notEqual(before,p.scoreA+p.scoreB)}}
let p=create()
assert.equal(p.gameType,'badminton'); assert.equal(p.pageIndex,1)
p.tapRed(); advance(300); assert.equal(p.scoreA,1)
p.tapRed(); advance(100); p.tapRed(); advance(300); assert.equal(p.scoreA,0)
p.tapBlue(); p.cancelPending(); advance(300); assert.equal(p.scoreB,0)
p.changeScore('A',-1); assert.equal(p.undoStack.length,2)
p.changeScore('B',1); p.undo(); assert.equal(p.scoreB,0)
p.archiveAndReset(); score(p,20,20); assert.equal(p.winner,''); p.changeScore('A',1); p.changeScore('A',1); assert.equal(p.winner,'A'); p.changeScore('B',1); assert.equal(p.scoreB,20); p.changeScore('A',-1); assert.equal(p.winner,''); p.undo(); assert.equal(p.winner,'A')
p.archiveAndReset(); assert.equal(p.records.length,1); assert.equal(p.records[0].score,'22 : 20'); p.archiveAndReset(); assert.equal(p.records.length,1)
score(p,29,29); p.changeScore('B',1); assert.equal(p.winner,'B')
p.chooseTableTennis(); assert.equal(p.confirming,true); p.cancelConfirm(); assert.equal(p.gameType,'badminton'); assert.equal(p.scoreB,30)
p.chooseTableTennis(); p.confirmAction(); assert.equal(p.gameType,'tabletennis'); assert.equal(p.records.length,2); assert.equal(p.scoreA,0)
score(p,10,10); p.changeScore('B',1); p.changeScore('B',1); assert.equal(p.winner,'B'); p.archiveAndReset(); assert.equal(p.records.length,3)
p.changeScore('A',1); p.startReset({touches:[{clientX:10,clientY:10}]}); advance(1000); assert.equal(p.resetLabel,'1'); p.stopReset(); advance(1500); assert.equal(p.scoreA,1)
p.startReset({touches:[{clientX:10,clientY:10}]}); advance(2000); assert.equal(p.scoreA,0); assert.equal(p.records.length,3); p.stopReset()
p.changeScore('B',1); p.startReset({touches:[{clientX:10,clientY:10}]}); p.moveReset({touches:[{clientX:50,clientY:10}]}); advance(2000); assert.equal(p.scoreB,1)
console.log('Scoring, gestures, reset, mode switching, undo and session history passed.')
