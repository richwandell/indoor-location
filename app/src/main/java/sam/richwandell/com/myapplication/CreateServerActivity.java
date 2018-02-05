package sam.richwandell.com.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static sam.richwandell.com.myapplication.RV.TAG;

public class CreateServerActivity extends Activity{

    public static String NEW_SERVER_NAME = "new_server_name";
    public static String NEW_SERVER_IP = "new_server_ip";
    public static String NEW_SERVER_PORT = "new_server_port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String ip = intent.getStringExtra(NEW_SERVER_IP);
        String port = intent.getStringExtra(NEW_SERVER_PORT);
        setContentView(R.layout.create_server_activity);

        TextView ipField = (TextView) findViewById(R.id.server_ip_field);
        ipField.setText(ip);

        TextView portField = (TextView) findViewById(R.id.server_port_field);
        portField.setText(port);
    }

    public void saveServer(View v) {
        Log.d(TAG, "CreateServerActivity.saveServer");

        EditText editText = (EditText) findViewById(R.id.server_ip_field);
        String ip = String.valueOf(editText.getText());

        editText = (EditText)findViewById(R.id.server_port_field);
        String port = String.valueOf(editText.getText());

        Log.d(TAG, " ip: " + ip + " port: " + port);

        Intent data = new Intent();
        data.putExtra(NEW_SERVER_IP, ip);
        data.putExtra(NEW_SERVER_PORT, port);

        setResult(RESULT_OK, data);
        finish();
    }
}


