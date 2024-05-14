package com.ucsalpdm.gps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ucsalpdm.gps.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private LocationManager locationManager;
    private boolean isTracking = false;

    public static final int PERMISSION_REQUEST_LOCATION = 1001;
    private List<LatLng> trilhaPoints = new ArrayList<>();
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtém uma referência ao SupportMapFragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        // Inicia o processo de inicialização do mapa
        mapFragment.getMapAsync((OnMapReadyCallback) this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        Button btnRegistrarTrilha = findViewById(R.id.btnRegistrarTrilha);
        Button btnGerenciarTrilha = findViewById(R.id.btnGerenciarTrilha);
        Button btnCompartilharTrilha = findViewById(R.id.btnCompartilharTrilha);
        Button btnConfiguracao = findViewById(R.id.btnConfiguracao);
        Button btnSobre = findViewById(R.id.btnSobre);


        btnRegistrarTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTracking) {
                    startTracking();
                } else {
                    stopTracking();
                }
            }
        });
        btnGerenciarTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crie uma string para exibir as trilhas salvas
                StringBuilder trilhasSalvas = new StringBuilder();
                for (int i = 0; i < trilhaPoints.size(); i++) {
                    LatLng trilhaPoint = trilhaPoints.get(i);
                    trilhasSalvas.append("Trilha ").append(i + 1).append(": Latitude ").append(trilhaPoint.latitude)
                            .append(", Longitude ").append(trilhaPoint.longitude).append("\n");
                }

                // Crie um AlertDialog para exibir as trilhas salvas
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Trilhas Salvas")
                        .setMessage(trilhasSalvas.toString())
                        .setPositiveButton("Fechar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss(); // Fecha o AlertDialog
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        btnCompartilharTrilha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifica se há trilhas para compartilhar
                if (trilhaPoints.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Não há trilhas para compartilhar", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Crie uma string para representar as trilhas
                StringBuilder trilhasString = new StringBuilder();
                for (int i = 0; i < trilhaPoints.size(); i++) {
                    LatLng trilhaPoint = trilhaPoints.get(i);
                    trilhasString.append("Trilha ").append(i + 1).append(": Latitude ").append(trilhaPoint.latitude)
                            .append(", Longitude ").append(trilhaPoint.longitude).append("\n");
                }

                // Crie um intent para compartilhar a trilha
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Trilhas Salvas");
                intent.putExtra(Intent.EXTRA_TEXT, trilhasString.toString());

                // Inicia a atividade de compartilhamento
                startActivity(Intent.createChooser(intent, "Compartilhar Trilhas"));
            }
        });


        btnConfiguracao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ConfiguracoesActivity.class);

                startActivity(intent);
            }
        });

        btnSobre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreditosActivity.class);

                startActivity(intent);
            }
        });
    }

    private void startTracking() {
        mMap.clear();
        isTracking = true;

        // Inicia o rastreamento da localização
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_LOCATION);
            return;
        }
        Toast.makeText(MainActivity.this, "Registrando trilha!", Toast.LENGTH_SHORT).show();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    private void stopTracking () {
        isTracking = false;

        // Desenha a linha da trilha no mapa
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(trilhaPoints)
                .color(Color.BLUE)
                .width(5);
        mMap.addPolyline(polylineOptions);

        // Parar o rastreamento da localização
        locationManager.removeUpdates(locationListener);
        Toast.makeText(MainActivity.this, "Fim da trilha!", Toast.LENGTH_SHORT).show();
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            // Armazena a localização na lista de trilha
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            trilhaPoints.add(latLng);

            // Adiciona um marcador no mapa para a localização atual
            mMap.addMarker(new MarkerOptions().position(latLng).title("Trilha Point"));

            // Aqui você pode obter as coordenadas geográficas e calcular a velocidade
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            float speedInMetersPerSecond = location.getSpeed();
            float speedInKilometersPerHour = speedInMetersPerSecond * 3.6f;



            // Exemplo de exibição das informações no log
            Log.d("Trilha", "Latitude: " + latitude + ", Longitude: " + longitude + ", Velocidade (m/s): " + speedInMetersPerSecond + ", Velocidade (km/h): " + speedInKilometersPerHour);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão de localização concedida, iniciar o rastreamento da localização
                startTracking();
            } else {
                // Permissão de localização negada, exibir uma mensagem ou tomar outra ação apropriada
                Toast.makeText(MainActivity.this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Recuperar as preferências salvas
        SharedPreferences sharedPreferences = getSharedPreferences("configuracoes", MODE_PRIVATE);
        String tipoMapa = sharedPreferences.getString("tipo_mapa", "Vetorial");
        String orientacaoMapa = sharedPreferences.getString("orientacao", "Course Up");

        // Define o tipo de mapa de acordo com a preferência do usuário
        if (tipoMapa.equals("Vetorial")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (tipoMapa.equals("Satélite")) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

        // Define a orientação do mapa de acordo com a preferência do usuário
        if (orientacaoMapa.equals("North Up")) {
            mMap.getUiSettings().setMapToolbarEnabled(true); // Habilita a barra de ferramentas do mapa
            mMap.getUiSettings().setMyLocationButtonEnabled(true); // Habilita o botão de localização
            mMap.getUiSettings().setCompassEnabled(true); // Habilita a bússola
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom(mMap.getCameraPosition().zoom)
                    .bearing(0)  // Norte do mapa alinhado com o topo do dispositivo
                    .tilt(0)
                    .build()));
        } else if (orientacaoMapa.equals("Course Up")) {
            mMap.getUiSettings().setMapToolbarEnabled(true); // Habilita a barra de ferramentas do mapa
            mMap.getUiSettings().setMyLocationButtonEnabled(true); // Habilita o botão de localização
            mMap.getUiSettings().setCompassEnabled(true); // Habilita a bússola
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(mMap.getCameraPosition().target)
                    .zoom(mMap.getCameraPosition().zoom)
                    .bearing(90)  // Topo do mapa alinhado com a direção do deslocamento
                    .tilt(0)
                    .build()));
        }

        // Configurar botões de zoom
        FloatingActionButton zoomInButton = findViewById(R.id.zoom_in_button);
        FloatingActionButton zoomOutButton = findViewById(R.id.zoom_out_button);

        // Definir listener para o botão de zoom in
        zoomInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        // Definir listener para o botão de zoom out
        zoomOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
        // Adicione qualquer outra configuração do mapa aqui, se necessário
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Atualizar as configurações do mapa quando houver alterações nas preferências
        if (key.equals("tipo_mapa") || key.equals("orientacao")) {
            // Recuperar as novas preferências
            String tipoMapa = sharedPreferences.getString("tipo_mapa", "Vetorial");
            String orientacaoMapa = sharedPreferences.getString("orientacao", "Course Up");

            // Atualizar o mapa de acordo com as novas preferências
            if (key.equals("tipo_mapa")) {
                if (tipoMapa.equals("Vetorial")) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (tipoMapa.equals("Satélite")) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
            } else if (key.equals("orientacao")) {
                if (orientacaoMapa.equals("North Up")) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(mMap.getCameraPosition().target)
                            .zoom(mMap.getCameraPosition().zoom)
                            .bearing(0)  // Norte do mapa alinhado com o topo do dispositivo
                            .tilt(0)
                            .build()));
                } else if (orientacaoMapa.equals("Course Up")) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(mMap.getCameraPosition().target)
                            .zoom(mMap.getCameraPosition().zoom)
                            .bearing(90)  // Topo do mapa alinhado com a direção do deslocamento
                            .tilt(0)
                            .build()));
                }
            }
        }
    }
}




