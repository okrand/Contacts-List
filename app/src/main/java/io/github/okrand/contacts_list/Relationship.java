package io.github.okrand.contacts_list;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Krando67 on 2/26/18.
 */

@Entity(tableName = "relation")
public class Relationship {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "relationTo")
    private String name2;

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }
    public String getName2(){
        return name2;
    }

    public void setId(int newId) { id = newId; }
    
    public void setName(String newName){
        name = newName;
    }

    public void setName2(String newName2){
        name2 = newName2;
    }

    public void setRel(Relationship r, String n1, String n2){
        r.name = n1;
        r.name2 = n2;
    }
}