package ro.pub.cs.systems.eim.practicaltest02v10;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class ServerThread extends Thread {

    private ServerSocket serverSocket;
    private int port;
    private boolean isRunning;

    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
            this.isRunning = true;
        } catch (Exception e) {
            Log.e("ServerThread", "Could not create server socket on port: " + port, e);
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Log.i("ServerThread", "Waiting for client connection...");
                Socket clientSocket = serverSocket.accept();
                Log.i("ServerThread", "Client connected!");

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String pokemonName = in.readLine();
                Log.i("ServerThread", "Received request for Pokemon: " + pokemonName);
                if (pokemonName != null && !pokemonName.isEmpty()) {
                    String response = fetchPokemonInfo(pokemonName);
                    out.println(response);
                    Log.i("ServerThread", "Sent response: " + response);
                } else {
                    out.println("Invalid Pokemon name");
                    Log.i("ServerThread", "Invalid Pokemon name received");
                }

                clientSocket.close();
                Log.i("ServerThread", "Client connection closed");
            } catch (Exception e) {
                Log.e("ServerThread", "Error accepting client connection", e);
            }
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                Log.i("ServerThread", "Server socket closed");
            }
        } catch (Exception e) {
            Log.e("ServerThread", "Error closing server socket", e);
        }
    }

    private String fetchPokemonInfo(String pokemonName) {
        try {
            String apiUrl = "https://pokeapi.co/api/v2/pokemon/" + pokemonName.toLowerCase();
            URL url = new URL(apiUrl);
            Log.i("ServerThread", "Opening connection to URL: " + apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            Log.i("ServerThread", "Response code: " + responseCode);
            if (responseCode != 200) {
                Log.e("ServerThread", "Pokemon not found: " + pokemonName);
                return "Pokemon not found";
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray typesArray = jsonResponse.getJSONArray("types");
            JSONArray abilitiesArray = jsonResponse.getJSONArray("abilities");

            StringBuilder types = new StringBuilder();
            for (int i = 0; i < typesArray.length(); i++) {
                types.append(typesArray.getJSONObject(i).getJSONObject("type").getString("name"));
                if (i < typesArray.length() - 1) {
                    types.append(", ");
                }
            }

            StringBuilder abilities = new StringBuilder();
            for (int i = 0; i < abilitiesArray.length(); i++) {
                abilities.append(abilitiesArray.getJSONObject(i).getJSONObject("ability").getString("name"));
                if (i < abilitiesArray.length() - 1) {
                    abilities.append(", ");
                }
            }

            JSONObject result = new JSONObject();
            result.put("types", types.toString());
            result.put("abilities", abilities.toString());

            Log.i("ServerThread", "Fetched info: " + result.toString());
            return result.toString();

        } catch (Exception e) {
            Log.e("ServerThread", "Error fetching Pokemon info", e);
            return "Server error: " + e.getMessage();
        }
    }
}
