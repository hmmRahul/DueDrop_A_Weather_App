package com.hmmrahul.duedrop.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.hmmrahul.duedrop.R;
import com.hmmrahul.duedrop.database.WeatherDatabase;
import com.hmmrahul.duedrop.databinding.ActivityMainBinding;
import com.hmmrahul.duedrop.models.AppKeyStore;
import com.hmmrahul.duedrop.models.Coord;
import com.hmmrahul.duedrop.models.DatabaseModel;
import com.hmmrahul.duedrop.models.Main;
import com.hmmrahul.duedrop.models.MainModelClass;
import com.hmmrahul.duedrop.models.Sys;
import com.hmmrahul.duedrop.models.Weather;
import com.hmmrahul.duedrop.models.Wind;
import com.hmmrahul.duedrop.network.ApiClient;
import com.hmmrahul.duedrop.network.ApiService;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;
    String currentLocationTobeSearched = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        loadLastWeatherUpdated();

        activityMainBinding.swipeLayout.setOnRefreshListener(() -> {
            apiCall();
            activityMainBinding.swipeLayout.setRefreshing(false);
        });

        activityMainBinding.searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                activityMainBinding.progressBar.setVisibility(View.VISIBLE);
                InputMethodManager in = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(activityMainBinding.searchBox.getWindowToken(), 0);
                activityMainBinding.searchBox.setFocusable(false);
                activityMainBinding.searchBox.setFocusableInTouchMode(true);

                apiCall();
                return true;

            }
            return false;
        });

    }

    private void loadLastWeatherUpdated() {
        Thread thread = new Thread(() -> {
            DatabaseModel databaseModel = WeatherDatabase.getWeatherDatabase(getApplicationContext())
                    .weatherDao().getWeatherFromDB();
            if (databaseModel != null) {
                activityMainBinding.gridLayout.setVisibility(View.VISIBLE);
                currentLocationTobeSearched = databaseModel.specificLocation;
                Log.d("hhh", "here2 - " + currentLocationTobeSearched);
                activityMainBinding.temperature.setText(databaseModel.temp + " °C");
                activityMainBinding.maxmin.setText("Max " + databaseModel.max + "°" + " ⸱ " + "Min " + databaseModel.min + "°");
                activityMainBinding.weatherDescription.setText(databaseModel.weatherDesc);

                //loading the image from the executor thread, which Picasso doesn't allow thus we need to execute it on ui thread
                MainActivity.this.runOnUiThread(() -> Picasso.get().load(databaseModel.iconUrl).into(activityMainBinding.weatherIcon));

                activityMainBinding.humidityCardValue.setText(databaseModel.humidity + "%");
                activityMainBinding.pressureCardValue.setText(String.valueOf(databaseModel.pressure));
                activityMainBinding.windCardValue.setText(String.valueOf(databaseModel.windSpeed));
                activityMainBinding.visibilityCardValue.setText(String.valueOf(databaseModel.visibility));

                activityMainBinding.sunriseText.setText("▲ Sunrise " + databaseModel.sunrise);
                activityMainBinding.sunsetText.setText("▼ Sunset " + databaseModel.sunset);
                activityMainBinding.lastUpdated.setText("↻ Last Updated - " + databaseModel.lastUpdated);

                activityMainBinding.location.setText(databaseModel.location);
            } else {
                if (!isConnected(this)) {
                    Log.d("yes", "here2 ");
                    MainActivity.this.runOnUiThread(() -> showCustomDialog());
                    activityMainBinding.noConnectionText.setVisibility(View.VISIBLE);
                    activityMainBinding.noConnectionText.setText("Check your Internet Connection ⚠️");
                }
                else
                {
                    activityMainBinding.noConnectionText.setVisibility(View.VISIBLE);
                    activityMainBinding.noConnectionText.setText("Search your First Weather Report ⬆");
                }
            }
        });
        thread.start();
    }

    private void showCustomDialog() {
        AlertDialog builder = new AlertDialog.Builder(MainActivity.this).setTitle("Network Error")
                .setMessage("Please Connect to the Internet").setCancelable(false).setPositiveButton("Okay", (dialog, which) -> {
                    dialog.dismiss();
                    activityMainBinding.progressBar.setVisibility(View.INVISIBLE);

                }).create();
        builder.show();

    }

    private boolean isConnected(MainActivity mainActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {
            return true;
        } else {
            return false;
        }
    }

    public void apiCall() {
        if (!isConnected(this)) {
            showCustomDialog();
        }
        else {
            activityMainBinding.noConnectionText.setVisibility(View.INVISIBLE);
            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
            if (activityMainBinding.searchBox.getText().toString().trim().length() != 0) {
                currentLocationTobeSearched = activityMainBinding.searchBox.getText().toString().trim();
            }
            if(currentLocationTobeSearched == null)
            {
                activityMainBinding.noConnectionText.setVisibility(View.VISIBLE);
                activityMainBinding.noConnectionText.setText("Erorr : Enter a City to Search");
                Toast.makeText(MainActivity.this,"Enter a City",Toast.LENGTH_SHORT).show();
                return;
            }
            Call<MainModelClass> mainModelClassCall = apiService.getDailyInfo(currentLocationTobeSearched, "metric", AppKeyStore.getAppid());

            mainModelClassCall.enqueue(new Callback<MainModelClass>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onResponse(Call<MainModelClass> call, Response<MainModelClass> response) {
                    if (!(response.isSuccessful())) {
                        Toast.makeText(MainActivity.this, "Please Enter a Valid City", Toast.LENGTH_SHORT).show();
                        activityMainBinding.progressBar.setVisibility(View.INVISIBLE);
                        activityMainBinding.searchBox.getText().clear();
                    } else {
                        MainModelClass mainModelClass = response.body();
                        ArrayList<Weather> weathers = mainModelClass.getWeather();
                        Main main = mainModelClass.getMain();
                        Wind wind = mainModelClass.getWind();
                        Sys sys = mainModelClass.getSys();

                        String weatherDesc = weathers.get(0).getMain();
                        activityMainBinding.weatherDescription.setText(weatherDesc);

                        Double temp = main.getTemp();
                        Double min = main.getTempMin();
                        Double max = main.getTempMax();
                        Integer pressure = main.getPressure();
                        Integer humidity = main.getHumidity();
                        Double windSpeed = wind.getSpeed();

                        long sunrise = sys.getSunrise();
                        long sunset = sys.getSunset();

                        int visibility = mainModelClass.getVisibility() / 1000;

                        activityMainBinding.progressBar.setVisibility(View.INVISIBLE);
                        activityMainBinding.temperature.setText(temp + " °C");
                        activityMainBinding.maxmin.setText("Max " + max + "°" + " ⸱ " + "Min " + min + "°");
                        String iconUrl = "http://openweathermap.org/img/w/" + weathers.get(0).getIcon() + ".png";
                        activityMainBinding.weatherIcon.setVisibility(View.VISIBLE);
                        Picasso.get().load(iconUrl).placeholder(R.drawable.prog).into(activityMainBinding.weatherIcon);
                        activityMainBinding.gridLayout.setVisibility(View.VISIBLE);
                        activityMainBinding.humidityCardValue.setText(humidity + "%");
                        activityMainBinding.pressureCardValue.setText(String.valueOf(pressure));
                        activityMainBinding.windCardValue.setText(String.valueOf(windSpeed));
                        activityMainBinding.visibilityCardValue.setText(String.valueOf(visibility));

                        long dt = mainModelClass.getDt();

                        String sunriseTime = formatTime(Instant.ofEpochSecond(sunrise));
                        String sunsetTime = formatTime(Instant.ofEpochSecond(sunset));
                        String lastUpdated = formatTime(Instant.ofEpochSecond(dt));

                        activityMainBinding.sunriseText.setText("▲ Sunrise " + sunriseTime);
                        activityMainBinding.sunsetText.setText("▼ Sunset " + sunsetTime);
                        activityMainBinding.lastUpdated.setText("↻ Last Updated - " + lastUpdated);

                        Coord coord = mainModelClass.getCoord();
                        Double latitude = coord.getLat();
                        Double longitude = coord.getLon();

                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        String locationforDB = null;
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            String city = addresses.get(0).getLocality();
                            String state = addresses.get(0).getAdminArea();
                            String zip = addresses.get(0).getPostalCode();

                            activityMainBinding.location.setText("Location - " + city + ", " + state + ", " + zip);
                            locationforDB = "Location - " + city + ", " + state + ", " + zip;
                            Log.d("loc", "Here " + locationforDB);
                        } catch (IOException e) {
                            e.printStackTrace();
                            locationforDB = currentLocationTobeSearched;
                            activityMainBinding.location.setText("Location - " + currentLocationTobeSearched);
                            Log.d("loc", "Here " + e.getMessage().toString() + locationforDB);
                        }
                        DatabaseModel databaseModel = new DatabaseModel(currentLocationTobeSearched, iconUrl, temp, min, max, weatherDesc, humidity, pressure, windSpeed, visibility,
                                locationforDB, lastUpdated, sunsetTime, sunriseTime);

                        InsertAsyncTask insertAsyncTask = new InsertAsyncTask();
                        insertAsyncTask.execute(databaseModel);

                    }
                }

                @Override
                public void onFailure(Call<MainModelClass> call, Throwable t) {
                    activityMainBinding.progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("h:mm a", Locale.ENGLISH)
            .withZone(ZoneId.of("Asia/Kolkata"));

    static String formatTime(Instant time) {
        return formatter.format(time);
    }

    //For background Execution
    class InsertAsyncTask extends AsyncTask<DatabaseModel, Void, Void> {
        @Override
        protected Void doInBackground(DatabaseModel... databaseModels) {
            WeatherDatabase.getWeatherDatabase(MainActivity.this).weatherDao()
                    .addWeather(databaseModels[0]);
            return null;
        }
    }
}