package com.yukuai.probe;
import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import com.xiaomi.xms.wearable.Wearable;
import com.xiaomi.xms.wearable.auth.Permission;
import com.xiaomi.xms.wearable.message.OnMessageReceivedListener;
import java.nio.charset.StandardCharsets;

public class ProbeActivity extends Activity {
 TextView status; String node; OnMessageReceivedListener listener;
 void show(String msg){runOnUiThread(()->{status.setText(msg);android.util.Log.i("YUKUAI_PROBE",msg);});}
 @Override public void onCreate(Bundle b){super.onCreate(b); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(32,64,32,32);status=new TextView(this);status.setTextSize(20);status.setText("等待测试");root.addView(status);Button find=new Button(this);find.setText("查找并请求授权");root.addView(find);Button send=new Button(this);send.setText("发送测试消息");root.addView(send);setContentView(root);
 listener=(id,data)->show("收到手环消息："+new String(data,StandardCharsets.UTF_8));
 find.setOnClickListener(v->{show("查找手环");try{Wearable.getNodeApi(this).getConnectedNodes().addOnSuccessListener(nodes->{if(nodes.isEmpty()){show("未找到已连接手环");return;}node=nodes.get(0).id;show("已发现设备，正在请求授权");Wearable.getAuthApi(this).requestPermission(node,Permission.DEVICE_MANAGER).addOnSuccessListener(perms->{show("授权成功，注册消息监听");Wearable.getMessageApi(this).addListener(node,listener).addOnSuccessListener(done->show("监听已注册，等待手环消息")).addOnFailureListener(e->show("监听失败："+e.getMessage()));}).addOnFailureListener(e->show("授权失败："+e.getMessage()));}).addOnFailureListener(e->show("查找失败："+e.getMessage()));}catch(Exception e){show("启动失败："+e);}});
 send.setOnClickListener(v->{if(node==null){show("请先查找手环");return;}Wearable.getMessageApi(this).sendMessage(node,"{\"type\":\"probe\",\"value\":\"phone\"}".getBytes(StandardCharsets.UTF_8)).addOnSuccessListener(done->show("手机消息已提交，等待手环回复")).addOnFailureListener(e->show("发送失败："+e.getMessage()));}); }
 @Override protected void onDestroy(){if(node!=null)Wearable.getMessageApi(this).removeListener(node);super.onDestroy();}
}
