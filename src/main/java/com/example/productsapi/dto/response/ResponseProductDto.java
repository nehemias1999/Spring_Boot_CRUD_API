package com.example.productsapi.dto.response;

import javax.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProductDto {

    private @Id Long id;
    private String name;
    private String description;
    private Long stock;
    private Double base_price;
    private Double cost_price;

}
