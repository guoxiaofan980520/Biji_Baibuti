package com.baibuti.biji.common.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class AuthRespDTO {

    private int id;
    private String username;

    @Setter
    private String token = "";
}