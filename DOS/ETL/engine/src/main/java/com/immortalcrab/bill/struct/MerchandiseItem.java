package com.immortalcrab.bill.struct;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class MerchandiseItem {

    protected String partNumber;
    protected String description;

    @NonNull
    protected List<String> serialNumbers;

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
        return "MerchandiseItem [partNumber=" + partNumber + ", description=" + description
                + ", serialNumber=" + serialNumbers + "]";
    }
}
