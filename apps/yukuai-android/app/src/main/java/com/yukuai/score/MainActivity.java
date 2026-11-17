package com.yukuai.score;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import org.json.JSONObject;
import com.xiaomi.xms.wearable.Wearable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int INK=Color.rgb(28,32,39), PAPER=Color.rgb(248,248,245), RED=Color.rgb(220,42,52), BLUE=Color.rgb(23,120,205);
    private Match match; private LinearLayout root, content, nav; private int page=0; private String deviceStatus="手环未连接"; private SyncBridge bridge; private boolean watchMirror=false, choosing=false; private String syncSession=""; private long syncRevision=-1; private String controller="watch"; private long controlEpoch=0; private boolean synced=false; private JSONObject pendingCommand, latestSnapshot; private String rejectedSession="";
    private final Handler handler=new Handler(); private Runnable pendingTap;
    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(INK);getWindow().setNavigationBarColor(INK);match=Match.restore(getPreferences(MODE_PRIVATE).getString("match",null)); if(getPackageName().equals("com.yukuai.sync")) bridge=new SyncBridge(this,new SyncBridge.Listener(){ public void status(String text){if(!text.equals(deviceStatus)){deviceStatus=text;synced=false;render();}} public void snapshot(JSONObject data){onSnapshot(data);} }); restoreSync();render();if(bridge!=null)bridge.connect();handler.postDelayed(retry,2000);}
    private final Runnable retry=new Runnable(){public void run(){if(bridge!=null&&pendingCommand!=null)bridge.send(pendingCommand);handler.postDelayed(this,2000);}};
    private void restoreSync(){try{JSONObject o=new JSONObject(getPreferences(MODE_PRIVATE).getString("sync","{}"));watchMirror=o.optBoolean("mirror");syncSession=o.optString("session");syncRevision=o.optLong("revision",-1);controller=o.optString("controller","watch");controlEpoch=o.optLong("epoch");pendingCommand=o.optJSONObject("pending");}catch(Exception ignored){}}
    private void saveSync(){try{JSONObject o=new JSONObject();o.put("mirror",watchMirror);o.put("session",syncSession);o.put("revision",syncRevision);o.put("controller",controller);o.put("epoch",controlEpoch);o.put("pending",pendingCommand);getPreferences(MODE_PRIVATE).edit().putString("sync",o.toString()).apply();}catch(Exception ignored){}}
    private void command(String action,JSONObject payload){if(!synced||bridge==null){Toast.makeText(this,"请先连接手环",Toast.LENGTH_SHORT).show();return;}if(pendingCommand!=null){Toast.makeText(this,"等待手环确认",Toast.LENGTH_SHORT).show();return;}try{JSONObject o=payload==null?new JSONObject():payload;o.put("type","command");o.put("protocol",2);o.put("id",java.util.UUID.randomUUID().toString());o.put("session",syncSession);o.put("baseRevision",syncRevision);o.put("controlEpoch",controlEpoch);o.put("action",action);pendingCommand=o;saveSync();bridge.send(o);render();}catch(Exception ignored){}}
    private void scoreChange(boolean red,int delta){if(watchMirror){if(!controller.equals("phone")){Toast.makeText(this,"由手环控制",Toast.LENGTH_SHORT).show();return;}try{JSONObject o=new JSONObject();o.put("side",red?"A":"B");o.put("delta",delta);command("score",o);}catch(Exception ignored){}}else{match.change(red,delta);save();render();}}
    private void resetMatch(String sport){if(watchMirror){try{JSONObject o=new JSONObject();o.put("sport",sport);command(sport.equals(match.sport)?"reset":"sport",o);}catch(Exception ignored){}}else match.reset(sport);}
    private void save(){getPreferences(MODE_PRIVATE).edit().putString("match",match.serialize()).apply();}
    private TextView text(String s,int size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setGravity(Gravity.CENTER);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private Button button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setTextSize(16);b.setTextColor(INK);b.setAllCaps(false);b.setMinHeight(dp(52));b.setOnClickListener(v->action.run());return b;}
    private void cancelPending(){if(pendingTap!=null){handler.removeCallbacks(pendingTap);pendingTap=null;}}
    private void render(){
        cancelPending(); root=column();root.setBackgroundColor(PAPER);setContentView(root);
        content=column();root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this);nav.setBackgroundColor(INK);String[] names={"记分","比赛","历史","连接"};
        for(int i=0;i<names.length;i++){final int n=i;TextView item=text(names[i],15,i==page?Color.WHITE:0xffaab2bc,i==page);item.setOnClickListener(v->{page=n;render();});nav.addView(item,new LinearLayout.LayoutParams(0,dp(62),1));}root.addView(nav);
        if(page==0)scorePage();else if(page==1)settingsPage();else if(page==2)historyPage();else connectionPage();
        if(page==0&&match.keepScreen)getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void scorePage(){
        LinearLayout top=scoreArea(!match.reversed);LinearLayout bottom=scoreArea(match.reversed);
        content.addView(top,new LinearLayout.LayoutParams(-1,0,1));content.addView(bottom,new LinearLayout.LayoutParams(-1,0,1));
    }
    private LinearLayout scoreArea(final boolean isRed){
        LinearLayout area=column();area.setGravity(Gravity.CENTER);area.setBackgroundColor(isRed?RED:BLUE);
        TextView name=text(isRed?match.redName:match.blueName,20,Color.WHITE,true);area.addView(name);
        TextView score=text(String.valueOf(isRed?match.red:match.blue),110,Color.WHITE,true);score.setContentDescription((isRed?match.redName:match.blueName)+" "+(isRed?match.red:match.blue)+"分");area.addView(score);
        String winner=match.winner(); if(winner.equals(isRed?"red":"blue")){TextView win=text("获胜",17,Color.WHITE,true);area.addView(win);}
        if(isRed==!match.reversed){TextView state=text(watchMirror ? (controller.equals("phone")?"手机计分":"手环计分")+" · "+(pendingCommand!=null?"等待确认":deviceStatus) : "手机计分 · "+deviceStatus,14,Color.WHITE,false);state.setPadding(dp(12),dp(8),dp(12),dp(8));state.setBackground(bg(0x33400000,16));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,dp(42));sp.topMargin=dp(18);area.addView(state,sp);}
        GestureDetector detector=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDown(MotionEvent e){return true;}
            @Override public boolean onSingleTapConfirmed(MotionEvent e){scoreChange(isRed,1);return true;}
            @Override public boolean onDoubleTap(MotionEvent e){scoreChange(isRed,-1);return true;}
        });area.setOnTouchListener((v,e)->detector.onTouchEvent(e)); area.setContentDescription((isRed?match.redName:match.blueName)+"计分区");return area;
    }
    private LinearLayout pageBody(String title){ScrollView sc=new ScrollView(this);content.addView(sc,new LinearLayout.LayoutParams(-1,-1));LinearLayout body=column();body.setPadding(dp(22),dp(24),dp(22),dp(28));sc.addView(body);TextView heading=text(title,28,INK,true);heading.setGravity(Gravity.START);heading.setPadding(0,0,0,dp(20));body.addView(heading);return body;}
    private void addButton(LinearLayout body,String s,Runnable r){Button b=button(s,r);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));p.bottomMargin=dp(10);body.addView(b,p);}
    private void confirm(String title,Runnable yes){new AlertDialog.Builder(this).setTitle(title).setNegativeButton("取消",(d,w)->{}).setPositiveButton("确认",(d,w)->{yes.run();save();render();}).show();}
    private void settingsPage(){LinearLayout b=pageBody("比赛"); if(watchMirror&&!controller.equals("phone")){addButton(b,"申请手机计分",()->command("requestControl",null));TextView t=text("由手环控制",20,INK,true);t.setPadding(0,dp(32),0,dp(32));b.addView(t);return;}addButton(b,"项目："+(match.sport.equals("badminton")?"羽毛球":"乒乓球"),()->new AlertDialog.Builder(this).setTitle("选择项目").setSingleChoiceItems(new String[]{"羽毛球","乒乓球"},match.sport.equals("badminton")?0:1,(d,w)->{d.dismiss();String next=w==0?"badminton":"tabletennis";if(next.equals(match.sport))return;confirm("切换项目并开始新局？",()->resetMatch(next));}).setNegativeButton("取消",null).show());addButton(b,"撤销上一步",()->{if(watchMirror)command("undo",null);else{match.undoLast();save();render();}});addButton(b,"重置本局",()->confirm("重置本局？",()->resetMatch(match.sport)));addButton(b,"交换上下位置",()->{if(watchMirror){try{JSONObject o=new JSONObject();o.put("reversed",!match.reversed);command("display",o);}catch(Exception ignored){}}else{match.reversed=!match.reversed;save();render();}});addButton(b,"修改双方名称",this::namesDialog);if(watchMirror)addButton(b,"交回手环计分",()->command("releaseControl",null));addButton(b,match.keepScreen?"记分时屏幕常亮：开":"记分时屏幕常亮：关",()->{match.keepScreen=!match.keepScreen;save();render();});}
    private void namesDialog(){LinearLayout l=column();l.setPadding(dp(24),0,dp(24),0);EditText r=new EditText(this);r.setSingleLine(true);r.setText(match.redName);r.setHint("红方名称");l.addView(r);EditText b=new EditText(this);b.setSingleLine(true);b.setText(match.blueName);b.setHint("蓝方名称");l.addView(b);new AlertDialog.Builder(this).setTitle("双方名称").setView(l).setNegativeButton("取消",null).setPositiveButton("保存",(d,w)->{String rn=r.getText().toString().trim(),bn=b.getText().toString().trim();if(rn.length()>12||bn.length()>12){Toast.makeText(this,"名称最多12字",Toast.LENGTH_SHORT).show();return;}if(watchMirror){try{JSONObject o=new JSONObject();o.put("redName",rn);o.put("blueName",bn);command("names",o);}catch(Exception ignored){}}else{match.redName=rn.isEmpty()?"红方":rn;match.blueName=bn.isEmpty()?"蓝方":bn;save();render();}}).show();}
    private void historyPage(){LinearLayout b=pageBody("历史");if(match.history.length()==0){TextView empty=text("暂无记录",18,0xff747a80,false);empty.setPadding(0,dp(70),0,0);b.addView(empty);return;}for(int i=match.history.length()-1;i>=0;i--){JSONObject h=match.history.optJSONObject(i);if(h==null)continue;String sport=h.optString("sport").equals("badminton")?"羽毛球":"乒乓球";String when=new SimpleDateFormat("MM-dd HH:mm",Locale.CHINA).format(new Date(h.optLong("time")));String win=h.optString("winner").equals("red")?h.optString("redName"):h.optString("blueName");TextView card=text(sport+"    "+when+"\n"+h.optString("redName")+"  "+h.optInt("red")+" : "+h.optInt("blue")+"  "+h.optString("blueName")+"\n"+win+"获胜",18,INK,true);card.setPadding(dp(12),dp(16),dp(12),dp(16));card.setBackground(bg(Color.WHITE,14));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(12);b.addView(card,p);}}
    private void connectionPage(){LinearLayout b=pageBody("连接");TextView status=text(deviceStatus,22,INK,true);status.setPadding(0,dp(30),0,dp(25));b.addView(status);addButton(b,"连接 / 重试",()->findBand());addButton(b,"断开连接",()->{if(bridge!=null)bridge.close();synced=false;deviceStatus="已断开";render();});}
    private void findBand(){ rejectedSession=""; if(bridge!=null){bridge.connect();return;} deviceStatus="同步待配置";render(); }
    private void onSnapshot(JSONObject data){
        if(!SyncSnapshot.valid(data)){deviceStatus="同步数据无效";synced=false;render();return;}
        String session=data.optString("session");long revision=data.optLong("revision");
        if(watchMirror&&syncSession.equals(session)&&revision<syncRevision)return;
        if(session.equals(rejectedSession))return;
        latestSnapshot=data;
        if(!watchMirror||!syncSession.equals(session)){
            if(choosing)return;choosing=true;
            new AlertDialog.Builder(this).setTitle("使用手环比分？").setMessage("手环："+data.optInt("red")+" : "+data.optInt("blue")+"\n手机当前："+match.red+" : "+match.blue).setNegativeButton("取消",(d,w)->{choosing=false;rejectedSession=session;deviceStatus="未同步";synced=false;render();}).setPositiveButton("使用手环比分",(d,w)->{choosing=false;getPreferences(MODE_PRIVATE).edit().putString("local_backup",match.serialize()).apply();watchMirror=true;syncSession=latestSnapshot.optString("session");syncRevision=-1;pendingCommand=null;applySnapshot(latestSnapshot);}).setOnCancelListener(d->{choosing=false;rejectedSession=session;}).show();return;
        }
        applySnapshot(data);
    }
    private void applySnapshot(JSONObject data){
        if(!SyncSnapshot.valid(data)||data.optLong("revision")<syncRevision)return;
        boolean unchanged=synced&&pendingCommand==null&&syncRevision==data.optLong("revision");
        syncSession=data.optString("session");syncRevision=data.optLong("revision");controller=data.optString("controller");controlEpoch=data.optLong("controlEpoch");
        SyncSnapshot.apply(match,data);
        if(pendingCommand!=null){org.json.JSONArray results=data.optJSONArray("commandResults");if(results!=null)for(int i=0;i<results.length();i++){JSONObject r=results.optJSONObject(i);if(r!=null&&r.optString("id").equals(pendingCommand.optString("id"))){String result=r.optString("status");pendingCommand=null;if(!result.equals("ok"))Toast.makeText(this,result.equals("stale")?"比分已更新，请重新操作":result.equals("cancelled")?"手环已取消接管":"操作未执行，请重试",Toast.LENGTH_SHORT).show();break;}}}
        deviceStatus="已同步";synced=true;save();saveSync();if(!unchanged)render();
        android.util.Log.i("YUKUAI_SYNC","state revision="+syncRevision+" red="+match.red+" blue="+match.blue+" controller="+controller);
    }
    @Override protected void onResume(){super.onResume();if(bridge!=null)bridge.hello();}
    @Override protected void onPause(){super.onPause();save();saveSync();cancelPending();}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(bridge!=null)bridge.close();super.onDestroy();}
}
