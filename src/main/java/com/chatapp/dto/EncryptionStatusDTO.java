package com.chatapp.dto;

public class EncryptionStatusDTO {

    private boolean hasKeys;

    public EncryptionStatusDTO() {
    }

    public EncryptionStatusDTO(boolean hasKeys) {
        this.hasKeys = hasKeys;
    }

    public boolean isHasKeys() {
        return hasKeys;
    }

    public void setHasKeys(boolean hasKeys) {
        this.hasKeys = hasKeys;
    }
}
