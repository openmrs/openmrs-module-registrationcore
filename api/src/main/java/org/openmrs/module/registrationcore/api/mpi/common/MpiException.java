package org.openmrs.module.registrationcore.api.mpi.common;

public class MpiException extends RuntimeException {

    public MpiException(String s) {
        super(s);
    }

    public MpiException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
