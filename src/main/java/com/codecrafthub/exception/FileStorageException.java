package com.codecrafthub.exception;

/**
 * Represents an error while reading or writing courses.json.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
