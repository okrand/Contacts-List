package io.github.okrand.contacts_list;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Krando67 on 2/24/18.
 */
@Entity(tableName = "contact")
public class Contact {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "number")
    private String number;

    @Ignore
    Bitmap picture;

    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getNumber(){
        return number;
    }

    public void setId(int newId) { id = newId; }
    public void setName(String newName){
        name = newName;
    }
    public void setNumber(String newNum){
        number = newNum;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Contact) {
            Contact c2 = (Contact) o;
            if (c2.getName().equals(getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}


