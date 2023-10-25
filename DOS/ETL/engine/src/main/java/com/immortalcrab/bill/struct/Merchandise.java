package com.immortalcrab.bill.struct;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
class Merchandise {

    private String partNumber;
    private String description;
    private List<String> serialNumber;

    @Override
    public String toString() {
        return "Merchandise [partNumber=" + partNumber + ", description=" + description
                + ", serialNumber=" + serialNumber + "]";
    }
}
