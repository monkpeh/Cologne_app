package com.example.colognerecommendation.dto;

import java.io.Serializable;

public class FragranceDto implements Serializable {

    private int id;
    private String brand;
    private String name;
    private String scentFamily;
    private int projection;
    private int longevity;
    private int seasonHot;
    private int seasonCold;
    private boolean officeSafe;
    private String description;
    private String imageUrl;

    public FragranceDto() {}

    public int getId()             { return id; }
    public String getBrand()       { return brand; }
    public String getName()        { return name; }
    public String getScentFamily() { return scentFamily; }
    public int getProjection()     { return projection; }
    public int getLongevity()      { return longevity; }
    public int getSeasonHot()      { return seasonHot; }
    public int getSeasonCold()     { return seasonCold; }
    public boolean isOfficeSafe()  { return officeSafe; }
    public String getDescription() { return description; }
    public String getImageUrl()    { return imageUrl; }

    public void setId(int id)                    { this.id = id; }
    public void setBrand(String brand)            { this.brand = brand; }
    public void setName(String name)              { this.name = name; }
    public void setScentFamily(String sf)         { this.scentFamily = sf; }
    public void setProjection(int projection)     { this.projection = projection; }
    public void setLongevity(int longevity)       { this.longevity = longevity; }
    public void setSeasonHot(int seasonHot)       { this.seasonHot = seasonHot; }
    public void setSeasonCold(int seasonCold)     { this.seasonCold = seasonCold; }
    public void setOfficeSafe(boolean officeSafe) { this.officeSafe = officeSafe; }
    public void setDescription(String description){ this.description = description; }
    public void setImageUrl(String imageUrl)      { this.imageUrl = imageUrl; }
}
