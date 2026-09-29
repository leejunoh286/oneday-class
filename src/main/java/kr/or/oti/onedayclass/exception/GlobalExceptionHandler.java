package kr.or.oti.onedayclass.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClassApplicationException.class)
    public String handleClassApplicationException(
            ClassApplicationException e,
            Model model) {

        model.addAttribute("errorMessage", e.getMessage());

        return "error/application-error";
    }
}
