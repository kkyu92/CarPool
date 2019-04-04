package com.example.kks.carpool.AutoSearch;

import java.util.ArrayList;

public class TmapRoute {
    private String type;
    private ArrayList<Features> features;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Features> getFeatures() {
        return features;
    }

    public void setFeatures(ArrayList<Features> features) {
        this.features = features;
    }

    public class Features {
        private String type;
        private Properties properties;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

    }

    public class Properties {
        private String totalDistance;
        private String totalTime;
        private String totalFare;
        private String taxiFare;

        public String getTotalDistance() {
            return totalDistance;
        }

        public void setTotalDistance(String totalDistance) {
            this.totalDistance = totalDistance;
        }

        public String getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(String totalTime) {
            this.totalTime = totalTime;
        }

        public String getTotalFare() {
            return totalFare;
        }

        public void setTotalFare(String totalFare) {
            this.totalFare = totalFare;
        }

        public String getTaxiFare() {
            return taxiFare;
        }

        public void setTaxiFare(String taxiFare) {
            this.taxiFare = taxiFare;
        }
    }
}
