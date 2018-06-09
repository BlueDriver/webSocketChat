package controller;

import java.io.IOException;
import java.util.Vector;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import net.sf.json.JSONObject;

@ServerEndpoint("/websocket")
public class MyWebsocket {
	// private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Vector<Session> room = new Vector<Session>();
	private static int online = 0;

	@OnOpen
	public void open(Session session) {
		System.out.println( Integer.valueOf(session.getId(), 16));
		online++;
		JSONObject j = new JSONObject();
		j.put("who", -1);
		j.put("msg", "新朋友上车了！");
		j.put("online", online);
		j.put("isSelf", false);
		for (Session se : room) {			
			// 发送消息给远程用户
			se.getAsyncRemote().sendText(j.toString());
		}
		room.add(session);
		//System.out.println("size: " + room.size());
		JSONObject json = new JSONObject();
		// json.put("date", df.format(new Date()));
		json.put("who", 0);
		json.put("msg", "欢迎新朋友上车！");
		json.put("online", online);
		json.put("isSelf", false);
		// 发送消息给远程用户
		session.getAsyncRemote().sendText(json.toString());

		// System.out.println(session.getId());
		// int n = Integer.parseInt(session.getId());
		// System.out.println(n);
		// System.out.println("*** WebSocket opened from sessionId " +
		// session.getId());
	}

	@OnMessage
	public void inMessage(String message, Session session) {
		System.out.println(message);
		//System.out.println( Integer.valueOf(session.getId(), 16) % 8 + 1);
		//System.out.println(message);
		// System.out.println(session.getId());
		JSONObject json = new JSONObject();
		// json.put("date", df.format(new Date()));
		
		int n = Integer.valueOf(session.getId(), 16) % 8 + 1;

		if(message.indexOf("，。，") >= 0){
			json.put("who", 0);
			json.put("msg", message.replace("，。，", ""));
		}else if(message.indexOf("，，。") >= 0){
			json.put("who", 110);
			json.put("msg", message.replace("，，。", ""));
		}else{
			json.put("who", n);
			json.put("msg", message);
		}	
		json.put("online", online);
		
		for (Session se : room) {
			// 设置消息是否为自己的			
			json.put("isSelf", se.equals(session));
			// 发送消息给远程用户
			se.getAsyncRemote().sendText(json.toString());
		}
		// System.out.println("*** WebSocket Received from sessionId " +
		// session.getId() + ": " + message);
	}

	@OnClose
	public void end(Session session) {
		// System.out.println("*** WebSocket closed from sessionId " +
		// session.getId());
		room.remove(session);
		if(online > 0)
			online--;
		JSONObject j = new JSONObject();
		j.put("who", -1);
		j.put("msg", "不好，有位朋友下车了。。。");
		j.put("online", online);
		j.put("isSelf", false);
		for (Session se : room) {			
			// 发送消息给远程用户
			se.getAsyncRemote().sendText(j.toString());
		}	
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	@OnError
	public void onError(Session session, Throwable t) {		
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		room.remove(session);
		if (t.toString().indexOf("IOException") < 0) {
			t.printStackTrace();
		}

	}

}
