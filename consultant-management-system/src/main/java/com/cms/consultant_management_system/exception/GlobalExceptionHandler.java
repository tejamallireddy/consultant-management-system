package com.cms.consultant_management_system.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = {
        com.cms.consultant_management_system.controller.ConsultantController.class,
        com.cms.consultant_management_system.controller.DashboardController.class
})
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Requested a consultant id that doesn't exist. */
    @ExceptionHandler(ConsultantNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ConsultantNotFoundException ex,
                                       HttpServletRequest request) {
        log.warn("Not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return errorView(404, "Consultant Not Found", ex.getMessage());
    }

    /** e.g. /consultants/abc/edit - 'abc' can't bind to Long. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                           HttpServletRequest request) {
        log.warn("Bad parameter at {}: {}", request.getRequestURI(), ex.getMessage());
        return errorView(400, "Invalid Request",
                "The value '" + ex.getValue() + "' is not valid for '" + ex.getName() + "'.");
    }

    /** Unmapped URL. */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandler(NoHandlerFoundException ex) {
        return errorView(404, "Page Not Found",
                "The page you requested does not exist.");
    }

    /** Catch-all. Logs the full stack trace but shows the user a clean page. */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}", request.getRequestURI(), ex);
        return errorView(500, "Something Went Wrong",
                "An unexpected error occurred. Please try again or contact support.");
    }

    private ModelAndView errorView(int status, String title, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("status", status);
        mav.addObject("title", title);
        mav.addObject("message", message);
        return mav;
    }
}