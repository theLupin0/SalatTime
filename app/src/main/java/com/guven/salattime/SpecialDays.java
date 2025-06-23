package com.guven.salattime;

public class SpecialDays {
    private String Name;
    private String Date;

    public SpecialDays(String _name,String _date){
        this.Name = _name;
        this.Date = _date;
    }

    public String getName(){
        return Name;
    }

    public String getDate(){
        return Date;
    }
}
