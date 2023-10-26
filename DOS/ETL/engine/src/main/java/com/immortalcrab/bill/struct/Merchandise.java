package com.immortalcrab.bill.struct;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
class Merchandise {

    private String partNumber;
    private String description;
    private List<String> serialNumbers;

    public Optional<String> getPartNumber() {
        return Optional.ofNullable(partNumber);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public List<String> getSerialNumbers() {
        return serialNumbers;
    }

    @Override
    public String toString() {
        return "Merchandise [partNumber=" + partNumber + ", description=" + description
                + ", serialNumber=" + serialNumbers + "]";
    }
}
