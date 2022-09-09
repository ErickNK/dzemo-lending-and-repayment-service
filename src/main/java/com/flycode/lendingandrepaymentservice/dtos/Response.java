package com.flycode.lendingandrepaymentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.lang.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<E> {

    @NonNull
    private Integer status;

    @Nullable
    private E data;

    @Nullable
    private String errorMessage;
}
