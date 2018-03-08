package io.github.okrand.contacts_list;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Krando67 on 2/26/18.
 */
@Database(entities = {Relationship.class}, version = 1)
public abstract class RelDatabase extends RoomDatabase {
    public abstract RelationshipDao relationDao();
}

