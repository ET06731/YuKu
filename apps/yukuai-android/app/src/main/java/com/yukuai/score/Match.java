package com.yukuai.score;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.UUID;

/** Local match rules. Communication must be validated before sharing state with a band. */
public final class Match {
    public String sport="badminton", redName="红方", blueName="蓝方";
    public int red=0, blue=0; public boolean reversed=false, keepScreen=true;
    public String gameId=UUID.randomUUID().toString();
    public JSONArray undo=new JSONArray(), history=new JSONArray();
    public String winner() {
        int target=sport.equals("badminton")?21:11;
        if (sport.equals("badminton")) { if(red>=30 && red>blue)return "red"; if(blue>=30 && blue>red)return "blue"; }
        if(red>=target && red-blue>=2)return "red";
        if(blue>=target && blue-red>=2)return "blue";
        return "";
    }
    public void change(boolean isRed,int delta) {
        if(delta>0 && !winner().isEmpty())return;
        int old=isRed?red:blue; if(old+delta<0)return;
        JSONObject snapshot=new JSONObject(); try {snapshot.put("red",red);snapshot.put("blue",blue);} catch(Exception e){throw new IllegalStateException(e);}
        undo.put(snapshot); if(isRed)red+=delta;else blue+=delta;
    }
    public void undoLast(){if(undo.length()==0)return;JSONObject x=undo.optJSONObject(undo.length()-1); undo.remove(undo.length()-1);if(x!=null){red=x.optInt("red");blue=x.optInt("blue");}}
    public void reset(String newSport) {
        String winner=winner();
        if(!winner.isEmpty())try {JSONObject h=new JSONObject();h.put("id",gameId);h.put("sport",sport);h.put("red",red);h.put("blue",blue);h.put("redName",redName);h.put("blueName",blueName);h.put("winner",winner);h.put("time",System.currentTimeMillis());history.put(h);}catch(Exception e){throw new IllegalStateException(e);}
        red=0;blue=0;undo=new JSONArray();gameId=UUID.randomUUID().toString();sport=newSport;
    }
    public String serialize(){try{JSONObject o=new JSONObject();o.put("sport",sport);o.put("redName",redName);o.put("blueName",blueName);o.put("red",red);o.put("blue",blue);o.put("reversed",reversed);o.put("keepScreen",keepScreen);o.put("gameId",gameId);o.put("undo",undo);o.put("history",history);return o.toString();}catch(Exception e){throw new IllegalStateException(e);}}
    public static Match restore(String json){Match m=new Match();if(json==null)return m;try{JSONObject o=new JSONObject(json);m.sport=o.optString("sport","badminton");m.redName=o.optString("redName","红方");m.blueName=o.optString("blueName","蓝方");m.red=Math.max(0,o.optInt("red"));m.blue=Math.max(0,o.optInt("blue"));m.reversed=o.optBoolean("reversed");m.keepScreen=o.optBoolean("keepScreen",true);m.gameId=o.optString("gameId",m.gameId);m.undo=o.optJSONArray("undo");if(m.undo==null)m.undo=new JSONArray();m.history=o.optJSONArray("history");if(m.history==null)m.history=new JSONArray();}catch(Exception ignored){}return m;}
}
