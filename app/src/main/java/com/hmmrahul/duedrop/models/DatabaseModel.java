package com.hmmrahul.duedrop.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "weather")
public class DatabaseModel{

     @PrimaryKey
     public int weatherId;

     public String specificLocation;

     public String iconUrl;
     public Double temp;
     public Double min;
     public Double max;
     public String weatherDesc;

     public Integer humidity;
     public Integer pressure;
     public Double windSpeed;
     public int visibility;

     public String location;
     public String lastUpdated;

     public String sunset;
     public String sunrise;

     public DatabaseModel(String specificLocation,String iconUrl, Double temp, Double min, Double max, String weatherDesc, Integer humidity,
                          Integer pressure, Double windSpeed, int visibility, String location, String lastUpdated, String sunset, String sunrise) {
          this.specificLocation = specificLocation;
          this.iconUrl = iconUrl;
          this.temp = temp;
          this.min = min;
          this.max = max;
          this.weatherDesc = weatherDesc;
          this.humidity = humidity;
          this.pressure = pressure;
          this.windSpeed = windSpeed;
          this.visibility = visibility;
          this.location = location;
          this.lastUpdated = lastUpdated;
          this.sunset = sunset;
          this.sunrise = sunrise;
     }
}
