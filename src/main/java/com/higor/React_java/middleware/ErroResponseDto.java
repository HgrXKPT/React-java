package com.higor.React_java.middleware;                                                                                   
                                                                                                                               
    import java.time.Instant;                                                                                                  
                                                                                                                               
    public record ErroResponseDto(                                                                                             
        int status,                                                                                                            
        String mensagem,                                                                                                       
        Instant timestamp                                                                                                      
    ) {}
