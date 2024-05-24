package ro.pub.cs.systems.eim.practicaltest02v10;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class PracticalTest02MainActivityv10 extends AppCompatActivity {

    private EditText editTextPokemonName, editTextPort;
    private Button buttonFetch, buttonStartServer;
    private TextView textViewTypes, textViewAbilities;
    private ServerThread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPokemonName = findViewById(R.id.editTextPokemonName);
        editTextPort = findViewById(R.id.editTextPort);
        buttonFetch = findViewById(R.id.buttonFetch);
        buttonStartServer = findViewById(R.id.buttonStartServer);
        textViewTypes = findViewById(R.id.textViewTypes);
        textViewAbilities = findViewById(R.id.textViewAbilities);

        buttonStartServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String portStr = editTextPort.getText().toString();
                if (portStr.isEmpty()) {
                    Toast.makeText(PracticalTest02MainActivityv10.this, "Please enter a port number", Toast.LENGTH_SHORT).show();
                    return;
                }
                int port = Integer.parseInt(portStr);
                serverThread = new ServerThread(port);
                serverThread.start();
                Toast.makeText(PracticalTest02MainActivityv10.this, "Server started on port " + port, Toast.LENGTH_SHORT).show();
                Log.i("MainActivity", "Server started on port " + port);
            }
        });

        buttonFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pokemonName = editTextPokemonName.getText().toString();
                String portStr = editTextPort.getText().toString();
                if (pokemonName.isEmpty() || portStr.isEmpty()) {
                    Toast.makeText(PracticalTest02MainActivityv10.this, "Please enter both Pokemon name and port number", Toast.LENGTH_SHORT).show();
                    return;
                }
                int port = Integer.parseInt(portStr);
                new FetchPokemonTask().execute(pokemonName, String.valueOf(port));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serverThread != null) {
            serverThread.stopServer();
            Log.i("MainActivity", "Server stopped");
        }
    }

    private class FetchPokemonTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String pokemonName = params[0];
            int port = Integer.parseInt(params[1]);
            try {
                Socket socket = new Socket("localhost", port);
                OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
                out.write(pokemonName + "\n");
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();

                in.close();
                out.close();
                socket.close();

                return new JSONObject(response);
            } catch (Exception e) {
                Log.e("FetchPokemonTask", "Error fetching Pokemon data", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (result != null) {
                try {
                    String types = result.getString("types");
                    String abilities = result.getString("abilities");

                    textViewTypes.setText("Types: " + types);
                    textViewAbilities.setText("Abilities: " + abilities);

                    Log.i("FetchPokemonTask", "Types: " + types);
                    Log.i("FetchPokemonTask", "Abilities: " + abilities);

                } catch (Exception e) {
                    Toast.makeText(PracticalTest02MainActivityv10.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    Log.e("FetchPokemonTask", "Error parsing response", e);
                }
            } else {
                Toast.makeText(PracticalTest02MainActivityv10.this, "Error fetching data from server", Toast.LENGTH_SHORT).show();
                Log.e("FetchPokemonTask", "Error fetching data from server");
            }
        }
    }
}
