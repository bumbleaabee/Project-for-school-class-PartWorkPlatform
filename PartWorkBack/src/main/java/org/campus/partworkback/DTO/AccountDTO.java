package org.campus.partworkback.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private String username;
    private String password;
    private String email;
    private String captchaId;
    private String captchaCode;
}
