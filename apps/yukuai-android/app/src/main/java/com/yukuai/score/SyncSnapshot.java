package com.yukuai.score;
import org.json.JSONArray;
import org.json.JSONObject;
public final class SyncSnapshot {
 public static boolean valid(JSONObject o){
  if(o==null||o.optInt("protocol")!=2||!o.optString("type").equals("state")||o.optString("session").isEmpty()||o.optLong("revision",-1)<0||o.optLong("controlEpoch",-1)<0)return false;
  if(!o.optString("controller").equals("watch")&&!o.optString("controller").equals("phone"))return false;
  String sport=o.optString("sport");return (sport.equals("badminton")||sport.equals("tabletennis"))&&integer(o,"red")&&integer(o,"blue")&&o.optJSONArray("records")!=null;
 }
 private static boolean integer(JSONObject o,String key){Object n=o.opt(key);return n instanceof Number&&((Number)n).doubleValue()==((Number)n).intValue()&&((Number)n).intValue()>=0&&((Number)n).intValue()<=999;}
 public static void apply(Match m,JSONObject o){if(!valid(o))throw new IllegalArgumentException("Invalid snapshot");m.sport=o.optString("sport");m.red=o.optInt("red");m.blue=o.optInt("blue");m.redName=o.optString("redName","红方");m.blueName=o.optString("blueName","蓝方");m.reversed=o.optBoolean("reversed");m.gameId=o.optString("session");m.undo=new JSONArray();JSONArray records=o.optJSONArray("records"),history=new JSONArray();for(int i=records.length()-1;i>=0;i--){JSONObject r=records.optJSONObject(i);if(r==null)continue;String[] scores=r.optString("score").split(":");if(scores.length!=2)continue;try{int red=Integer.parseInt(scores[0].trim()),blue=Integer.parseInt(scores[1].trim());JSONObject h=new JSONObject();h.put("id",r.optString("id",o.optString("session")+":"+r.optLong("timestamp")+":"+i));h.put("sport",r.optString("game").equals("乒乓球")?"tabletennis":"badminton");h.put("red",red);h.put("blue",blue);h.put("redName",r.optString("redName",m.redName));h.put("blueName",r.optString("blueName",m.blueName));h.put("winner",red>blue?"red":"blue");h.put("time",r.optLong("timestamp"));history.put(h);}catch(Exception ignored){}}m.history=history;}
}
