package com.yukuai.score;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.xiaomi.xms.wearable.Wearable;
import com.xiaomi.xms.wearable.auth.Permission;
import com.xiaomi.xms.wearable.message.OnMessageReceivedListener;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/** Foreground transport; only an application snapshot proves the peer is reachable. */
public final class SyncBridge {
 public interface Listener { void status(String text); void snapshot(JSONObject data); }
 private final Context context; private final Listener listener; private final Handler main=new Handler(Looper.getMainLooper());
 private String node; private boolean registered,active; private int generation; private long lastReceived,started;
 private OnMessageReceivedListener incoming;
 private void state(String s){listener.status(s);}
 public SyncBridge(Context context,Listener listener){this.context=context.getApplicationContext();this.listener=listener;}
 private final Runnable heartbeat=new Runnable(){public void run(){if(!active)return;long now=android.os.SystemClock.elapsedRealtime();if(registered){if(now-lastReceived>10000)state("手环未响应");hello();}else if(now-started>15000){open();}main.postDelayed(this,3000);}};
 public void connect(){close();active=true;open();main.postDelayed(heartbeat,3000);}
 private void open(){final int a=++generation;started=android.os.SystemClock.elapsedRealtime();state("正在查找");try{Wearable.getNodeApi(context).getConnectedNodes().addOnSuccessListener(nodes->main.post(()->{if(!valid(a))return;if(nodes.isEmpty()){state("未找到手环");return;}node=nodes.get(0).id;state("正在申请授权");Wearable.getAuthApi(context).requestPermission(node,Permission.DEVICE_MANAGER).addOnSuccessListener(perms->main.post(()->{if(!valid(a))return;state("正在连接");incoming=(id,bytes)->main.post(()->{if(!valid(a)||!id.equals(node))return;try{JSONObject obj=new JSONObject(new String(bytes,StandardCharsets.UTF_8));if(!obj.optString("type").equals("state"))return;if(obj.optInt("protocol")!=2){state("请更新手环应用");return;}lastReceived=android.os.SystemClock.elapsedRealtime();listener.snapshot(obj);}catch(Exception e){state("同步数据无效");}});Wearable.getMessageApi(context).addListener(node,incoming).addOnSuccessListener(done->main.post(()->{if(!valid(a))return;registered=true;lastReceived=0;state("等待手环同步");hello();})).addOnFailureListener(e->fail(a,"监听失败"));})).addOnFailureListener(e->fail(a,"授权失败"));})).addOnFailureListener(e->fail(a,"查找失败"));}catch(Exception e){fail(a,"连接失败");}}
 private boolean valid(int a){return active&&a==generation;}
 private void fail(int a,String text){main.post(()->{if(!valid(a))return;registered=false;state(text);});}
 public void hello(){JSONObject o=new JSONObject();try{o.put("type","hello");o.put("protocol",2);}catch(Exception ignored){}send(o);}
 public boolean send(JSONObject message){if(!active||!registered||node==null)return false;final int a=generation;try{Wearable.getMessageApi(context).sendMessage(node,message.toString().getBytes(StandardCharsets.UTF_8)).addOnFailureListener(e->fail(a,"已断开"));return true;}catch(Exception e){fail(a,"发送失败");return false;}}
 public void close(){active=false;generation++;main.removeCallbacks(heartbeat);if(node!=null)try{Wearable.getMessageApi(context).removeListener(node);}catch(Exception ignored){}registered=false;node=null;}
}
