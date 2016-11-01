package com.powempresas.rest;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.AsyncTask;
import android.os.Bundle;


import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    TextView inputLocal;
    TextView status;
    TextView conexao;
    TextView valoresFiltro;
    TextView resposta;
    String lat;
    Double latitude = -25.538549;
    Double longitude = -49.196069;
    Button getDados;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputLocal = (TextView) findViewById(R.id.inputLocal);
        status = (TextView) findViewById(R.id.status);
        conexao = (TextView) findViewById(R.id.conexao);
        resposta = (TextView) findViewById(R.id.resposta);
        getDados = (Button) findViewById(R.id.getDados);
        getDados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStatus("Clicado...");
                setDados();

            }
        });
        setStatus(String.valueOf("Iniciado"));
        setLocal();
    }

    protected void onStart() {
        super.onStart();
    }


    protected void onStop() {
        super.onStop();
    }


    public void setLocal() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                setStatus(String.valueOf("Changed"));
                if (latitude != location.getLatitude() && longitude != location.getLongitude()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    conexao.setText(String.valueOf("Utilizando " + location.getProvider()));
                    status.setText(String.valueOf("LocationChanged"));
                    inputLocal.setText(String.valueOf(latitude + ", " + longitude));
                    setDados();
                } else {
                    setStatus(String.valueOf("Sem Alteração."));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                setStatus(String.valueOf("StatusChanged"));
            }

            @Override
            public void onProviderEnabled(String provider) {
                status.setText(String.valueOf("Ativo"));
            }

            @Override
            public void onProviderDisabled(String provider) {
                status.setText(String.valueOf("Inativo"));
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println(String.valueOf("Sem permissão"));
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    public void setDados() {
        JSONObject json = new JSONObject();
        Gson gson = new Gson();
        try {
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("imoveis_tipos_link", "apartamento");

            String qtdeAsync = new ImoveisQtde().execute(json.toString()).get();
            Integer qtde = gson.fromJson(qtdeAsync, new TypeToken<Integer>(){}.getType());
            System.out.println(qtde);

            String itensAsync = new ImoveisItens().execute(json.toString()).get();
            JSONArray itensJsonArray = new JSONArray(itensAsync);
            int itensQtde = itensJsonArray.length();
            for ( int i = 0 ; i < itensQtde; i++ ) {
                JSONObject item = itensJsonArray.getJSONObject(i);
                System.out.println(item.getString("nome"));
                JSONObject images = item.getJSONObject("images");
                System.out.println(images);
            }
            //int qtdeA = itensJson.length();

            //Map<String, Object> itensMap = gson.fromJson(itensAsync, new TypeToken<Map<String, Object>>(){}.getType());
            //resposta.setText(itens.toString());
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void setStatus(String valor) {
        status.setText(valor);
    }




    private class ImoveisQtde extends AsyncTask<String, Double, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlValue = "http://192.168.2.20:3000/imoveis/qtde";
            HttpURLConnection urlConn = null;
            try {
                URL url = new URL(urlValue);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.setRequestProperty("charset", "utf-8");
                urlConn.setDoOutput(true);
                String p = params[0];

                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8"));
                writer.write(p);

                writer.close();

                urlConn.connect();
                int statusConn = urlConn.getResponseCode();
                setStatus(String.valueOf(statusConn));
                if (statusConn == HttpURLConnection.HTTP_OK || statusConn == HttpURLConnection.HTTP_NOT_FOUND) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    System.out.print("Nãao conectou");
                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

    private class ImoveisItens extends AsyncTask<String, Double, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlValue = "http://192.168.2.20:3000/imoveis/itens";
            HttpURLConnection urlConn = null;
            try {
                URL url = new URL(urlValue);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.setRequestProperty("charset", "utf-8");
                urlConn.setDoOutput(true);
                String p = params[0];

                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8"));
                writer.write(p);

                writer.close();

                urlConn.connect();
                int statusConn = urlConn.getResponseCode();
                setStatus(String.valueOf(statusConn));
                if (statusConn == HttpURLConnection.HTTP_OK || statusConn == HttpURLConnection.HTTP_NOT_FOUND) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    System.out.print("Nãao conectou");
                }


            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

}

