package io.github.okrand.contacts_list;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Krando67 on 2/26/18.
 */

@Dao
public interface RelationshipDao {
    @Query("SELECT * FROM relation WHERE name LIKE :name OR relationTo LIKE :name")
    List<Relationship> findRelations(String name);

    @Query("SELECT * FROM relation WHERE name NOT LIKE :name OR relationTo NOT LIKE :name")
    List<Relationship> getOthers(String name);

    @Query("DELETE FROM relation WHERE name LIKE :name OR relationTo LIKE :name")
    void deleteRelation(String name);

    @Insert
    void insertAll(Relationship... Relationship);

    @Delete
    void delete(Relationship... Relationship);
}
