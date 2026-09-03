package com.ajj.memorizer.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "categories",
    foreignKeys = @ForeignKey(
        entity = Category.class,
        parentColumns = "id",
        childColumns = "parentId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("parentId")}
)
public class Category {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String name;
    private Integer parentId; // Null if it's a root Subject
    
    private int lastStudyIndex;
    private float rating; // Overall progress/rating for this category

    public Category(String name, Integer parentId) {
        this.name = name;
        this.parentId = parentId;
        this.lastStudyIndex = 0;
        this.rating = 0.0f;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public int getLastStudyIndex() { return lastStudyIndex; }
    public void setLastStudyIndex(int lastStudyIndex) { this.lastStudyIndex = lastStudyIndex; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
}
