package com.immortalcrab.bill.struct;

import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
class Merchandise {

    private @NonNull
    String partNumber;

    private @NonNull
    String description;

    private @NonNull
    List<String> serialNumbers;

    public static Merchandise make() {
        return new Merchandise("", "", new LinkedList<>());
    }

    @Override
    public String toString() {
        return "Merchandise [partNumber=" + partNumber + ", description=" + description
                + ", serialNumber=" + serialNumbers + "]";
    }
}
