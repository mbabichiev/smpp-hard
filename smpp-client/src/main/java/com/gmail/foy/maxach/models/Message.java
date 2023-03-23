package com.gmail.foy.maxach.models;

public class Message {

    private String id;
    private Long publish_date;
    private String fromAddr;
    private String fromTON;
    private String fromNPI;
    private String toAddr;
    private String toTON;
    private String toNPI;
    private String message;
    private String dcs;
    private int deliveryStatus;


    public Message(String id, Long publish_date, String fromAddr, String fromTON, String fromNPI,
                   String toAddr, String toTON, String toNPI, String message, String dcs, int deliveryStatus) {
        this.id = id;
        this.publish_date = publish_date;
        this.fromAddr = fromAddr;
        this.fromTON = fromTON;
        this.fromNPI = fromNPI;
        this.toAddr = toAddr;
        this.toTON = toTON;
        this.toNPI = toNPI;
        this.message = message;
        this.dcs = dcs;
        this.deliveryStatus = deliveryStatus;
    }


    public String getId() {
        return id;
    }

    public Long getPublishDate() {
        return publish_date;
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public String getFromTON() {
        return fromTON;
    }

    public String getFromNPI() {
        return fromNPI;
    }

    public String getToAddr() {
        return toAddr;
    }

    public String getToTON() {
        return toTON;
    }

    public String getToNPI() {
        return toNPI;
    }

    public String getMessage() {
        return message;
    }

    public String getDcs() {
        return dcs;
    }

    public int getDeliveryStatus() {
        return deliveryStatus;
    }

}
