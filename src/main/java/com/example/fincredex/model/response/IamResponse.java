package com.example.fincredex.model.response;

import com.example.fincredex.model.Constants.ApiMessage;
import com.example.fincredex.model.dto.UserDTO;
import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IamResponse<P> implements Serializable {
    private String message; //Текстовое сообщение для клиента.
    private P payload; //Основная полезная нагрузка (данные), которые вы хотите вернуть.
    private boolean success; //Флаг успеха операции.

    public static <P extends Serializable> IamResponse<P> success(P payload) {
        return new IamResponse<P>("success", payload, true);
    }

    public static <P> IamResponse<P> createSuccessful(P payload) {
        return new IamResponse<>(StringUtils.EMPTY, payload, true);
    }

    public static <P> IamResponse<P> createSuccessfulWithNewToken(P payload) {
        return new IamResponse<>(ApiMessage.TOKEN_CREATED_OR_UPDATED.getMessage(), payload, true);
    }
}
