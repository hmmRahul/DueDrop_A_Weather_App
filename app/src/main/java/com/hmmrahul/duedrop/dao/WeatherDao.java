package com.hmmrahul.duedrop.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hmmrahul.duedrop.models.DatabaseModel;

@Dao
public interface WeatherDao {
    @Query("SELECT * FROM weather")
    DatabaseModel getWeatherFromDB();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addWeather(DatabaseModel databaseModel);

}
