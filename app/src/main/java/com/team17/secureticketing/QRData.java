package com.team17.secureticketing;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class QRData {

    String userName,cipherText,key,ivParameterSpec;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIvParameterSpec() {
        return ivParameterSpec;
    }

    public void setIvParameterSpec(String ivParameterSpec) {
        this.ivParameterSpec = ivParameterSpec;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCipherText() {
        return cipherText;
    }
    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }


}
