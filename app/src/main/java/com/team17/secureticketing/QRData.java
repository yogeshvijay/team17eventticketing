package com.team17.secureticketing;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class QRData {

    String ticketHolder, encryptedData, initialVector, encryptedKey;

    public String getTicketHolder() {
        return ticketHolder;
    }

    public void setTicketHolder(String ticketHolder) {
        this.ticketHolder = ticketHolder;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getInitialVector() {
        return initialVector;
    }

    public void setInitialVector(String initialVector) {
        this.initialVector = initialVector;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    @Override
    public String toString() {
        return "QRData{" +
                "ticketHolder='" + ticketHolder + '\'' +
                ", encryptedData='" + encryptedData + '\'' +
                ", initialVector='" + initialVector + '\'' +
                ", encryptedKey='" + encryptedKey + '\'' +
                '}';
    }
}
