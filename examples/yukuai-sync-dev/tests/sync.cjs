const fs=require('fs'),vm=require('vm'),assert=require('assert'),path=require('path');
const source=fs.readFileSync(path.join(__dirname,'../src/pages/index/index.ux'),'utf8').match(/<script>([\s\S]*?)<\/script>/)[1].replace(/^import .*$/gm,'').replace('export default','module.exports =');
function create(saved){const sent=[],writes=[];let failNext=false;const conn={send(x){sent.push(x.data)}};const sandbox={module:{exports:{}},console,setTimeout,clearTimeout,brightness:{setKeepScreenOn(o){o.success()}},storage:{get(o){saved?o.success(saved):o.fail()},set(o){if(failNext){o.fail();return}writes.push(o.value);o.success()}},interconnect:{instance(){return conn}}};vm.runInNewContext(source,sandbox);const base=sandbox.module.exports,p=Object.assign({},base,JSON.parse(JSON.stringify(base.private)));p.onInit();return {p,sent,writes,failSave(){failNext=true},allowSave(){failNext=false}};}
function cmd(p,id,action,extra={}){return Object.assign({type:'command',protocol:2,id,session:p.syncSession,baseRevision:p.syncRevision,controlEpoch:p.controlEpoch,action},extra)}
let {p,writes}=create();p.scoreA=7;p.queueSave(true);p.receiveSync(cmd(p,'take','requestControl'));assert(p.confirming);p.receiveSync(cmd(p,'take','requestControl'));assert.equal(p.commandResults.length,0);p.confirmAction();assert.equal(p.controller,'phone');assert.equal(p.scoreA,7);assert.equal(p.controlEpoch,1);
const add=cmd(p,'one','score',{side:'A',delta:1});p.receiveSync(add);assert.equal(p.scoreA,8);p.receiveSync(add);assert.equal(p.scoreA,8);assert.equal(JSON.parse(writes.at(-1)).commandResults.at(-1).id,'one');
p.receiveSync({...add,id:'late'});assert.equal(p.scoreA,8);assert.equal(p.commandResults.at(-1).status,'stale');
p.tapRed();assert.equal(p.scoreA,8);p.receiveSync(cmd(p,'release','releaseControl'));assert.equal(p.controller,'watch');p.receiveSync(cmd(p,'deny','score',{side:'A',delta:1}));assert.equal(p.scoreA,8);
p.receiveSync(cmd(p,'cancel','requestControl'));p.cancelConfirm();assert.equal(p.controller,'watch');assert.equal(p.commandResults.at(-1).status,'cancelled');
p.receiveSync(cmd(p,'take2','requestControl'));p.confirmAction();p.receiveSync(cmd(p,'bad','score',{side:'A',delta:12}));assert.equal(p.scoreA,8);assert.equal(p.commandResults.at(-1).status,'invalid');
const restored=create(writes.at(-1)).p;assert.equal(restored.controller,'phone');assert.equal(restored.scoreA,8);restored.receiveSync(add);assert.equal(restored.scoreA,8);
console.log('Sync passed: handover, cancellation, duplicate/stale/invalid commands, read-only watch, persisted acknowledgement and recovery');

p.requestWatchControl();p.confirmAction();assert.equal(p.controller,'watch');assert.equal(p.scoreA,8);

const disk=create();disk.p.controller='phone';disk.failSave();const op=cmd(disk.p,'disk','score',{side:'B',delta:1});const before=disk.sent.length;disk.p.receiveSync(op);assert.equal(disk.p.scoreB,1);assert.equal(disk.sent.length,before);disk.p.receiveSync(op);assert.equal(disk.p.scoreB,1);disk.allowSave();disk.p.receiveSync({type:'hello',protocol:2});assert.equal(disk.sent.at(-1).blue,1);assert.equal(JSON.parse(disk.writes.at(-1)).blue,1);
for(let i=0;i<200;i++){const m=cmd(disk.p,'stress'+i,'score',{side:'A',delta:i%2?-1:1});disk.p.receiveSync(m);disk.p.receiveSync(m);}assert.equal(disk.p.scoreA,0);assert.equal(disk.p.commandResults.length,64);
console.log('Storage retry and 200 duplicated commands passed');

const takeover=create();takeover.p.controller='phone';takeover.p.scoreA=8;takeover.p.scoreB=9;takeover.p.queueSave(true);const late=cmd(takeover.p,'pre-reset','score',{side:'A',delta:1});takeover.p.confirming=true;takeover.p.requestedAction='reclaimReset';takeover.p.confirmAction();let finalState=JSON.parse(takeover.writes.at(-1));assert.equal(finalState.controller,'watch');assert.equal(finalState.controlEpoch,1);assert.equal(finalState.red,0);assert.equal(finalState.blue,0);takeover.p.receiveSync(late);assert.equal(takeover.p.scoreA,0);assert.equal(takeover.p.commandResults.at(-1).status,'stale');assert.equal(create(takeover.writes.at(-1)).p.controller,'watch');
console.log('Takeover/reset persistence and delayed phone command rejection passed');
