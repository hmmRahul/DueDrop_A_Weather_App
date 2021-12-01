package com.hmmrahul.duedrop.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.hmmrahul.duedrop.dao.WeatherDao;
import com.hmmrahul.duedrop.models.DatabaseModel;

@Database(entities = DatabaseModel.class ,version = 1,exportSchema = false)
public abstract class WeatherDatabase extends RoomDatabase
{
    private static WeatherDatabase weatherDatabase;

    public static synchronized WeatherDatabase getWeatherDatabase(Context context)
    {
        if(weatherDatabase == null)
        {
            weatherDatabase = Room.databaseBuilder(context,WeatherDatabase.class,"weather_db").build();
        }
        return weatherDatabase;
    }

    public abstract WeatherDao weatherDao();
}
