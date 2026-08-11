package com.mbugajski.logistics.courier.exception;

public class CourierInvalidStateException extends RuntimeException {
  public CourierInvalidStateException(String message) {
    super(message);
  }
}
