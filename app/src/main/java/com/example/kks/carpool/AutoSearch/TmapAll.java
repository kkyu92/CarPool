package com.example.kks.carpool.AutoSearch;

import java.util.List;

public abstract class TmapAll {

    private List<Features> features;
    private String type;

    public List<Features> getFeatures() {
        return features;
    }

    public void setFeatures(List<Features> features) {
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class Features {
        private Properties properties;
        private Geometry geometry;
        private String type;

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Properties {
        private String pointtype;
        private int turntype;
        private String nextroadname;
        private String description;
        private String name;
        private int pointindex;
        private int index;
        private int taxifare;
        private int totalfare;
        private int totaltime;
        private int totaldistance;

        public String getPointtype() {
            return pointtype;
        }

        public void setPointtype(String pointtype) {
            this.pointtype = pointtype;
        }

        public int getTurntype() {
            return turntype;
        }

        public void setTurntype(int turntype) {
            this.turntype = turntype;
        }

        public String getNextroadname() {
            return nextroadname;
        }

        public void setNextroadname(String nextroadname) {
            this.nextroadname = nextroadname;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPointindex() {
            return pointindex;
        }

        public void setPointindex(int pointindex) {
            this.pointindex = pointindex;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getTaxifare() {
            return taxifare;
        }

        public void setTaxifare(int taxifare) {
            this.taxifare = taxifare;
        }

        public int getTotalfare() {
            return totalfare;
        }

        public void setTotalfare(int totalfare) {
            this.totalfare = totalfare;
        }

        public int getTotaltime() {
            return totaltime;
        }

        public void setTotaltime(int totaltime) {
            this.totaltime = totaltime;
        }

        public int getTotaldistance() {
            return totaldistance;
        }

        public void setTotaldistance(int totaldistance) {
            this.totaldistance = totaldistance;
        }
    }

    public static class Geometry {
        private List<Double> coordinates;
        private String type;

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            this.coordinates = coordinates;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
