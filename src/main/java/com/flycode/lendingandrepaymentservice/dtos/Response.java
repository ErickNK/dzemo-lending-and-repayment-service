package com.flycode.lendingandrepaymentservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
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

    public static <T> Response<T> withBadRequestError(String error) {
        return new Response<>(
                HttpStatus.BAD_REQUEST.value(),
                null,
                error
        );
    }

    public static <T> Response<T> successResponse(T body) {
        return new Response<>(
                HttpStatus.OK.value(),
                body,
                null
        );
    }
}
