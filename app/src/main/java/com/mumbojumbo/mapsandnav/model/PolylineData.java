package com.mumbojumbo.mapsandnav.model;

import com.google.android.gms.maps.model.Polyline;
import com.google.maps.model.DirectionsLeg;

public class PolylineData {
    private Polyline mPolyline;
    private DirectionsLeg mLeg;

    public PolylineData(Polyline mPolyLine, DirectionsLeg mLeg) {
        this.mPolyline = mPolyLine;
        this.mLeg = mLeg;
    }

    public Polyline getPolyline() {
        return mPolyline;
    }

    public void setPolyline(Polyline mPolyLine) {
        this.mPolyline = mPolyLine;
    }

    public DirectionsLeg getLeg() {
        return mLeg;
    }

    public void setLeg(DirectionsLeg mLeg) {
        this.mLeg = mLeg;
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("PolyLineData{\n Polyline: "+ mPolyline+" ,\n leg: "+mLeg+" }" );
        return sb.toString();
    }
}
