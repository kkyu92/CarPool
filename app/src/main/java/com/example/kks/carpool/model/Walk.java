package com.example.kks.carpool.model;

import java.util.List;

public class Walk {

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
        private String facilityname;
        private String facilitytype;
        private String intersectionname;
        private String nearpoiy;
        private String nearpoix;
        private String nearpoiname;
        private String direction;
        private String description;
        private String name;
        private int pointindex;
        private int index;
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

        public String getFacilityname() {
            return facilityname;
        }

        public void setFacilityname(String facilityname) {
            this.facilityname = facilityname;
        }

        public String getFacilitytype() {
            return facilitytype;
        }

        public void setFacilitytype(String facilitytype) {
            this.facilitytype = facilitytype;
        }

        public String getIntersectionname() {
            return intersectionname;
        }

        public void setIntersectionname(String intersectionname) {
            this.intersectionname = intersectionname;
        }

        public String getNearpoiy() {
            return nearpoiy;
        }

        public void setNearpoiy(String nearpoiy) {
            this.nearpoiy = nearpoiy;
        }

        public String getNearpoix() {
            return nearpoix;
        }

        public void setNearpoix(String nearpoix) {
            this.nearpoix = nearpoix;
        }

        public String getNearpoiname() {
            return nearpoiname;
        }

        public void setNearpoiname(String nearpoiname) {
            this.nearpoiname = nearpoiname;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
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
