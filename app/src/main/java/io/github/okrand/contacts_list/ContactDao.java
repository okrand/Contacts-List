package io.github.okrand.contacts_list;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Krando67 on 2/26/18.
 */

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contact WHERE name LIKE :name LIMIT 1")
    Contact findByName(String name);

    @Query("SELECT * FROM contact")
    List<Contact> getAll();

    @Query("SELECT * FROM contact ORDER BY ID DESC LIMIT 1")
    Contact getLast();

    @Query("SELECT COUNT(name) FROM contact")
    Integer countContacts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Contact... Contacts);

    @Delete
    void delete(Contact... Contacts);
}

