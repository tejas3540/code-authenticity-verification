package com.vericode.vericode_backend.exception;

public class CandidateNotFoundException extends RuntimeException{
        public CandidateNotFoundException(String message){
            super(message);
    }
}