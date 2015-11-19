package cjm.ctastoppicker;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Prediction implements Comparable<Prediction> {
    //from API
    Time requestTime;
    String predictionType;
    String stopName;
    String stopID;
    int vehicleID;
    int distanceToStop;
    String routeNumber;
    String direction;
    String destination;
    Time predictionTime;

    //calculated
    Integer timeRemaining;

    public Prediction(String requestTime, String predictionType, String stopName, String stopID,
                      String vehicleID, String distanceToStop, String routeNumber, String direction,
                      String destination, String predictionTime)
    {
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        try {
            this.requestTime = new Time(formatter.parse(requestTime.substring(9,14)).getTime());
        }
        catch (ParseException e) {
            System.out.println("time parsing exception");
        }
        this.predictionType = predictionType;
        this.stopName = stopName;
        this.stopID = stopID;
        this.vehicleID = Integer.parseInt(vehicleID);
        this.distanceToStop = Integer.parseInt(distanceToStop);
        this.routeNumber = routeNumber;
        this.direction = direction.replaceAll("(?i)bound", "");;
        this.destination = destination;
        try {
            this.predictionTime = new Time(formatter.parse(predictionTime.substring(9,14)).getTime());
        }
        catch (ParseException e) {
            System.out.println("time parsing exception");
        }

        long diff = this.predictionTime.getTime() - this.requestTime.getTime();
        timeRemaining = (int) diff/60000;
    }

    @Override
    public int compareTo(Prediction another) {
        return timeRemaining - another.timeRemaining;
    }
}
