package io.huskit.common.number;

import lombok.experimental.NonFinal;

public interface Hexadecimal {

    static Hexadecimal fromHexChars() {
        return new HexFromChars();
    }

    Hexadecimal withHexChar(char hexChar);

    int intValue();
}

class HexFromChars implements Hexadecimal {

    @NonFinal
    int value;

    @Override
    public Hexadecimal withHexChar(char hexChar) {
        int value;
        if (hexChar >= '0' && hexChar <= '9') {
            value = hexChar - '0';
        } else if (hexChar >= 'a' && hexChar <= 'f') {
            value = 10 + (hexChar - 'a');
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            value = 10 + (hexChar - 'A');
        } else {
            throw new IllegalArgumentException("Invalid hex character: " + hexChar);
        }
        this.value = (this.value << 4) | value;
        return this;
    }

    @Override
    public int intValue() {
        return value;
    }
}
