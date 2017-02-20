package sam.richwandell.com.myapplication.upnp;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import sam.richwandell.com.myapplication.db.FloorPlan;
import sam.richwandell.com.myapplication.eventlisteners.FloorPlanReceiver;


public class UPnP {
    private Context context;
    private UPnPListener listener;

    private static HashMap<String, HeaderParser.UPnPDevice> deviceCache = new HashMap<>();

    private static DatagramSocket socket = null;

    public UPnP(Context context){
        this.context = context;
    }

    public UPnP(Context context, UPnPListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void registerListener(UPnPListener listener){
        this.listener = listener;
    }

    public void discover(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                if(wifi != null) {
                    WifiManager.MulticastLock lock = wifi.createMulticastLock("multicast_lock");
                    lock.acquire();
                    try {
                        InetAddress group = InetAddress.getByName("239.255.255.250");
                        int port = 1900;
                        String query =
                                "M-SEARCH * HTTP/1.1\r\n" +
                                        "HOST: 239.255.255.250:1900\r\n"+
                                        "MAN: \"ssdp:discover\"\r\n"+
                                        "MX: 1\r\n"+
                                        "ST: ssdp:all\r\n"+
                                        "\r\n";

                        if(socket == null){
                            socket = new DatagramSocket(port);
                            socket.setReuseAddress(true);
                        }

                        if(socket.isClosed()){
                            socket = new DatagramSocket(port);
                            socket.setReuseAddress(true);
                        }

                        if(!socket.isBound()) {
                            socket = new DatagramSocket(port);
                            socket.setReuseAddress(true);
                        }

                        DatagramPacket dgram = new DatagramPacket(query.getBytes(), query.length(),
                                group, port);
                        socket.send(dgram);

                        long time = System.currentTimeMillis();
                        long curTime = System.currentTimeMillis();

                        while (curTime - time < 1000) {
                            byte[] buff = new byte[1200];
                            DatagramPacket p = new DatagramPacket(buff, buff.length);
                            socket.receive(p);
                            String[] s = new String(p.getData()).trim().split("\r\n");
                            HeaderParser hp = new HeaderParser(s);
                            curTime = System.currentTimeMillis();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        if(socket != null) {
                            socket.close();
                        }
                    }
                    lock.release();
                }
            }
        }).start();
    }

    public class HeaderParser {
        HashMap<String, String> headers = new HashMap<>();
        String status;



        public HeaderParser(String[] headers){
            status = headers[0];
            for(String h: headers){
                String[] parsed = h.split(":", 2);
                if(parsed.length > 1) {
                    this.headers.put(parsed[0], parsed[1]);
                }
            }
            if(this.headers.containsKey("LOCATION")){
                String key = this.headers.get("LOCATION");
                if(!deviceCache.containsKey(key)){
                    UPnPDevice dev = new UPnPDevice();
                    deviceCache.put(key, dev);
                    dev.loadDescription();
                }
            }
        }

        public String get(String key) {
            return this.headers.get(key);
        }

        public UPnPDevice getDevice(){
            return new UPnPDevice();
        }

        public class UPnPDevice{

            private String deviceDescription;

            private HashMap<String, String> attrs = new HashMap<>();

            @Override
            public String toString(){
                if(deviceDescription != null) {
                    return deviceDescription;
                }else{
                    return "";
                }
            }

            public String get(String attr){
                if(attrs.containsKey(attr)) {
                    return attrs.get(attr);
                }else{
                    return null;
                }
            }

            private void parseXML(String xmlString){
                deviceDescription = xmlString;
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = null;
                try {
                    dBuilder = dbFactory.newDocumentBuilder();
                    InputSource inputSource = new InputSource(new StringReader(xmlString));
                    Document doc = dBuilder.parse(inputSource);
                    NodeList devices = doc.getElementsByTagName("device");
                    for(int x = 0; x < devices.getLength(); x++){
                        Node device = devices.item(x);
                        NodeList children = device.getChildNodes();
                        for(int y = 0; y < children.getLength(); y++){
                            Node child = children.item(y);
                            if(child.hasChildNodes()){
                                String childName = child.getNodeName();
                                String childData = child.getTextContent();
                                attrs.put(childName, childData);
                            }
                        }
                    }
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    e.printStackTrace();
                }
            }

            public void loadDescription(){
                String location = headers.get("LOCATION");
                if(location == null){
                    location = headers.get("location");
                }
                RequestQueue queue = Volley.newRequestQueue(context);
                String url = location;
                final UPnPDevice that = this;
                if(url != null) {
                    // Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(
                        Request.Method.GET,
                        url,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                parseXML(response);
                                listener.onDiscover(that);
                            }

                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                listener.onDiscover(that);
                            }

                        }
                    );

                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
                }
            }

            public void loadFloorPlans(final FloorPlanReceiver fpr) {
                String endPoint = get("endPoint");
                if (endPoint != null) {
                    RequestQueue queue = Volley.newRequestQueue(context);
                    String url = endPoint + "floorplans";
                    Log.d("rdebug", url);
                    JsonArrayRequest req = new JsonArrayRequest(url,

                            new Response.Listener<JSONArray>() {

                                @Override
                                public void onResponse(JSONArray response) {
                                    Log.d("rdebug", response.toString());
                                    FloorPlan[] fps = new FloorPlan[response.length()];
                                    for(int i = 0; i < response.length(); i++){
                                        try {
                                            fps[i] = new FloorPlan((JSONObject)response.get(i));
                                            Log.d("rdebug", Integer.toString(i) + ": " + fps[i].toString());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    fpr.onFloorPlanReceived(fps);
                                }

                            },

                            new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("rdebug", error.toString());
                                }

                            }
                    );
                    queue.add(req);
                }
            }
        }
    }
}